package com.example.ingestor.table;

import com.example.config.IngestionConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.flink.TableLoader;

/**
 * Interface for building Iceberg table sinks based on write mode
 */
public interface TableBuilder {
    
    /**
     * Build and configure the Flink sink for the table
     * @param dataStream The input data stream
     * @param tableLoader The Iceberg table loader
     * @param config The ingestion configuration
     */
    void buildSink(DataStream<RowData> dataStream, TableLoader tableLoader, IngestionConfig config);
    
    /**
     * Get the write mode this builder supports
     */
    String getSupportedWriteMode();
}
