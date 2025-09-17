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
 * Order ingestion job that reads from Kafka and writes to Iceberg table
 */
public class OrderIngestionJob {
    private static final Logger LOG = LoggerFactory.getLogger(OrderIngestionJob.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Order Ingestion Job");

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

        // Create orders table if it doesn't exist
        tableEnv.executeSql("CREATE TABLE IF NOT EXISTS orders (" +
                "order_id BIGINT," +
                "customer_id BIGINT," +
                "product_name STRING," +
                "quantity INT," +
                "price DOUBLE," +
                "total_amount DOUBLE," +
                "order_date TIMESTAMP(3)," +
                "status STRING" +
                ") WITH (" +
                "'connector'='iceberg'," +
                "'catalog-name'='iceberg_catalog'," +
                "'database-name'='default'," +
                "'table-name'='orders'" +
                ")");

        // Create Kafka source
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("orders")
                .setGroupId("order-ingestion-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Create data stream from Kafka
        DataStream<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Map JSON to RowData
        DataStream<RowData> orderStream = kafkaStream
                .map(new OrderJsonToRowDataMapper())
                .name("Parse Order JSON to RowData");

        // Get table loader
        TableLoader tableLoader = TableLoader.fromCatalog(
                FlinkCatalogFactory.createCatalog("iceberg_catalog", 
                        tableEnv.getConfig().getConfiguration()),
                TableIdentifier.of("default", "orders"));

        // Write to Iceberg table
        FlinkSink.forRowData(orderStream)
                .tableLoader(tableLoader)
                .append();

        // Execute the job
        env.execute("Order Ingestion Job");
    }

    /**
     * Maps order JSON to Flink RowData
     */
    public static class OrderJsonToRowDataMapper implements MapFunction<String, RowData> {
        @Override
        public RowData map(String value) throws Exception {
            try {
                JsonNode jsonNode = OBJECT_MAPPER.readTree(value);
                
                GenericRowData rowData = new GenericRowData(8);
                
                // order_id
                rowData.setField(0, jsonNode.get("order_id").asLong());
                
                // customer_id
                rowData.setField(1, jsonNode.get("customer_id").asLong());
                
                // product_name
                rowData.setField(2, StringData.fromString(jsonNode.get("product_name").asText()));
                
                // quantity
                rowData.setField(3, jsonNode.get("quantity").asInt());
                
                // price
                rowData.setField(4, jsonNode.get("price").asDouble());
                
                // total_amount
                rowData.setField(5, jsonNode.get("total_amount").asDouble());
                
                // order_date
                String orderDateStr = jsonNode.get("order_date").asText();
                LocalDateTime orderDate = LocalDateTime.parse(orderDateStr);
                rowData.setField(6, Instant.parse(orderDate.atOffset(ZoneOffset.UTC).toString()).toEpochMilli() * 1000);
                
                // status
                rowData.setField(7, StringData.fromString(jsonNode.get("status").asText()));
                
                return rowData;
            } catch (Exception e) {
                LOG.error("Failed to parse order JSON: {}", value, e);
                return null;
            }
        }
    }
}
