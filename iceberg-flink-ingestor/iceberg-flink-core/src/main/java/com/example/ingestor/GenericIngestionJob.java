package com.example.ingestor;

import com.example.config.ConfigLoader;
import com.example.config.IngestionConfig;
import com.example.ingestor.factory.FactoryRegistry;
import com.example.ingestor.factory.IngestionFactory;
import com.example.ingestor.mapper.JsonToRowDataMapper;
import com.example.ingestor.table.TableBuilder;
import com.example.ingestor.validator.ConfigValidator;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.catalog.TableIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic Flink job for ingesting data from Kafka into Iceberg tables
 * Configuration is provided via JSON file
 */
public class GenericIngestionJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(GenericIngestionJob.class);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            LOG.error("Usage: GenericIngestionJob <config-file-path>");
            System.exit(1);
        }

        String configPath = args[0];
        LOG.info("Starting Generic Ingestion Job with config: {}", configPath);

        // Load configuration
        IngestionConfig config = ConfigLoader.loadFromFile(configPath);
        
        // Get factory for the write mode
        IngestionFactory factory = FactoryRegistry.getFactory(config);
        
        // Validate configuration using factory-specific validator
        ConfigValidator validator = factory.createValidator(config);
        validator.validate(config);

        // Setup AWS region
        setupAwsRegion(config);

        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Create table loader
        TableLoader tableLoader = createTableLoader(config);

        // Create Kafka source
        KafkaSource<String> kafkaSource = createKafkaSource(config);

        // Create data stream
        DataStream<String> kafkaStream = env.fromSource(
                kafkaSource, 
                WatermarkStrategy.noWatermarks(), 
                "Kafka Source"
        );

        // Create mapper using factory
        JsonToRowDataMapper mapper = factory.createMapper(config);
        DataStream<RowData> dataStream = kafkaStream
                .map(mapper)
                .name("Parse JSON to RowData");

        // Create table builder using factory
        TableBuilder tableBuilder = factory.createTableBuilder(config);
        tableBuilder.buildSink(dataStream, tableLoader, config);

        LOG.info("Generic ingestion job started for table: {}.{} with write mode: {}", 
                config.getTable().getNamespace(), 
                config.getTable().getName(),
                config.getTable().getWriteMode());
        
        env.execute("Generic Ingestion Job - " + config.getTable().getName());
    }

    private static void setupAwsRegion(IngestionConfig config) {
        String region = config.getAws() != null ? config.getAws().getRegion() : null;
        if (region == null || region.trim().isEmpty()) {
            region = "us-east-1";
        }
        System.setProperty("aws.region", region);
        LOG.info("AWS region set to: {}", region);
    }

    private static TableLoader createTableLoader(IngestionConfig config) {
        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("uri", config.getIceberg().getCatalogUri());
        catalogProperties.put("warehouse", config.getIceberg().getWarehouse());
        catalogProperties.put("io-impl", config.getIceberg().getIoImpl());

        // Add AWS properties
        if (config.getAws() != null) {
            catalogProperties.put("s3.endpoint", config.getAws().getEndpoint());
            catalogProperties.put("s3.access-key-id", config.getAws().getAccessKeyId());
            catalogProperties.put("s3.secret-access-key", config.getAws().getSecretAccessKey());
            catalogProperties.put("s3.path-style-access", String.valueOf(config.getAws().isPathStyleAccess()));
            catalogProperties.put("s3.region", config.getAws().getRegion());
            catalogProperties.put("aws.region", config.getAws().getRegion());
        }

        // Add custom properties
        if (config.getIceberg().getProperties() != null) {
            catalogProperties.putAll(config.getIceberg().getProperties());
        }

        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();
        TableIdentifier tableId = TableIdentifier.of(
                config.getTable().getNamespace(), 
                config.getTable().getName()
        );

        return TableLoader.fromCatalog(
                CatalogLoader.rest("iceberg", hadoopConf, catalogProperties), 
                tableId
        );
    }

    private static KafkaSource<String> createKafkaSource(IngestionConfig config) {
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(config.getKafka().getBootstrapServers())
                .setTopics(config.getKafka().getTopic())
                .setGroupId(config.getKafka().getGroupId())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets("earliest".equalsIgnoreCase(config.getKafka().getStartingOffset()) ? 
                    OffsetsInitializer.earliest() : OffsetsInitializer.latest())
                .build();

        return kafkaSource;
    }

}
