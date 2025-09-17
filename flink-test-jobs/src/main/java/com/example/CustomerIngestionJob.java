package com.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.FlinkCatalogFactory;
import org.apache.iceberg.flink.FlinkCatalog;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.api.common.functions.MapFunction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Customer ingestion job that reads from Kafka and writes to Iceberg table
 */
public class CustomerIngestionJob {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerIngestionJob.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Customer Ingestion Job");

        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Configure Iceberg catalog
        tableEnv.executeSql("CREATE CATALOG iceberg_catalog WITH (" +
                "'type'='iceberg'," +
                "'catalog-impl'='org.apache.iceberg.rest.RESTCatalog'," +
                "'uri'='http://iceberg-rest:8181'," +
                "'io-impl'='org.apache.iceberg.aws.s3.S3FileIO'," +
                "'warehouse'='s3://warehouse/'," +
                "'s3.endpoint'='http://minio:9000'," +
                "'s3.access-key-id'='minioadmin'," +
                "'s3.secret-access-key'='minioadmin'," +
                "'s3.path-style-access'='true'" +
                ")");

        tableEnv.executeSql("USE CATALOG iceberg_catalog");

        // Create customers table if it doesn't exist
        tableEnv.executeSql("CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id BIGINT," +
                "name STRING," +
                "email STRING," +
                "phone STRING," +
                "address STRING," +
                "created_at TIMESTAMP(3)," +
                "updated_at TIMESTAMP(3)" +
                ") WITH (" +
                "'connector'='iceberg'," +
                "'catalog-name'='iceberg_catalog'," +
                "'database-name'='default'," +
                "'table-name'='customers'" +
                ")");

        // Create Kafka source
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("customers")
                .setGroupId("customer-ingestion-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Create data stream from Kafka
        DataStream<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Map JSON to RowData
        DataStream<RowData> customerStream = kafkaStream
                .map(new CustomerJsonToRowDataMapper())
                .name("Parse Customer JSON to RowData");

        // Get table loader
        TableLoader tableLoader = TableLoader.fromCatalog(
                FlinkCatalogFactory.createCatalog("iceberg_catalog", 
                        tableEnv.getConfig().getConfiguration()),
                TableIdentifier.of("default", "customers"));

        // Write to Iceberg table
        FlinkSink.forRowData(customerStream)
                .tableLoader(tableLoader)
                .append();

        // Execute the job
        env.execute("Customer Ingestion Job");
    }

    /**
     * Maps customer JSON to Flink RowData
     */
    public static class CustomerJsonToRowDataMapper implements MapFunction<String, RowData> {
        @Override
        public RowData map(String value) throws Exception {
            try {
                JsonNode jsonNode = OBJECT_MAPPER.readTree(value);
                
                GenericRowData rowData = new GenericRowData(7);
                
                // customer_id
                rowData.setField(0, jsonNode.get("customer_id").asLong());
                
                // name
                rowData.setField(1, StringData.fromString(jsonNode.get("name").asText()));
                
                // email
                rowData.setField(2, StringData.fromString(jsonNode.get("email").asText()));
                
                // phone
                rowData.setField(3, StringData.fromString(jsonNode.get("phone").asText()));
                
                // address
                rowData.setField(4, StringData.fromString(jsonNode.get("address").asText()));
                
                // created_at
                String createdAtStr = jsonNode.get("created_at").asText();
                LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
                rowData.setField(5, Instant.parse(createdAt.atOffset(ZoneOffset.UTC).toString()).toEpochMilli() * 1000);
                
                // updated_at
                String updatedAtStr = jsonNode.get("updated_at").asText();
                LocalDateTime updatedAt = LocalDateTime.parse(updatedAtStr);
                rowData.setField(6, Instant.parse(updatedAt.atOffset(ZoneOffset.UTC).toString()).toEpochMilli() * 1000);
                
                return rowData;
            } catch (Exception e) {
                LOG.error("Failed to parse customer JSON: {}", value, e);
                return null;
            }
        }
    }
}
