package com.example.ingestor.table;

import com.example.config.IngestionConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.flink.TableLoader;

/**
 * Simple table builder for append-only write mode
 * Just creates a basic append sink - no special configuration needed
 */
public class AppendOnlyTableBuilder implements TableBuilder {
    
    @Override
    public String getSupportedWriteMode() {
        return "append";
    }
    
    @Override
    public void buildSink(DataStream<RowData> dataStream, TableLoader tableLoader, IngestionConfig config) {
        // Simple append-only sink - no special configuration needed
        FlinkSink.forRowData(dataStream)
                .tableLoader(tableLoader)
                .append();
    }
}
