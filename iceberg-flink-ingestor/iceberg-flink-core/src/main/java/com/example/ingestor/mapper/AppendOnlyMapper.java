package com.example.ingestor.mapper;

import com.example.config.IngestionConfig;
import org.apache.flink.table.data.RowData;

/**
 * Simple mapper for append-only write mode
 * Just delegates to base implementation - no special logic needed
 */
public class AppendOnlyMapper extends BaseJsonToRowDataMapper {
    
    @Override
    public String getSupportedWriteMode() {
        return "append";
    }
}
