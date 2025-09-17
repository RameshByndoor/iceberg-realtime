package com.example.ingestor.factory;

import com.example.config.IngestionConfig;
import com.example.ingestor.mapper.JsonToRowDataMapper;
import com.example.ingestor.table.TableBuilder;
import com.example.ingestor.validator.ConfigValidator;

/**
 * Factory interface for creating ingestion components based on write mode
 */
public interface IngestionFactory {
    
    /**
     * Create a JSON to RowData mapper based on configuration
     */
    JsonToRowDataMapper createMapper(IngestionConfig config);
    
    /**
     * Create a table builder based on configuration
     */
    TableBuilder createTableBuilder(IngestionConfig config);
    
    /**
     * Create a config validator based on configuration
     */
    ConfigValidator createValidator(IngestionConfig config);
    
    /**
     * Get the write mode this factory supports
     */
    String getSupportedWriteMode();
}
