package com.example.ingestor.mapper;

import com.example.config.IngestionConfig;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.table.data.RowData;

/**
 * Interface for mapping JSON strings to RowData
 */
public interface JsonToRowDataMapper extends MapFunction<String, RowData> {
    
    /**
     * Initialize the mapper with configuration
     */
    void initialize(IngestionConfig config);
    
    /**
     * Get the write mode this mapper supports
     */
    String getSupportedWriteMode();
}
