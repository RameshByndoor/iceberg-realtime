package com.example.ingestor.table;

import com.example.config.IngestionConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.flink.TableLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Table builder for upsert write mode
 */
public class UpsertTableBuilder implements TableBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(UpsertTableBuilder.class);
    
    @Override
    public String getSupportedWriteMode() {
        return "upsert";
    }
    
    @Override
    public void buildSink(DataStream<RowData> dataStream, TableLoader tableLoader, IngestionConfig config) {
        LOG.info("Building upsert sink for table: {}.{}", 
                config.getTable().getNamespace(), config.getTable().getName());
        
        FlinkSink.forRowData(dataStream)
                .tableLoader(tableLoader)
                .equalityFieldColumns(config.getTable().getEqualityFields() != null ? 
                    config.getTable().getEqualityFields() : java.util.Arrays.asList())
                .upsert(true)
                .append();
        
        LOG.info("Configured upsert with equality fields: {}", config.getTable().getEqualityFields());
    }
}
