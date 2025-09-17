package com.example.ingestor.factory;

import com.example.config.IngestionConfig;
import com.example.ingestor.mapper.JsonToRowDataMapper;
import com.example.ingestor.mapper.UpsertMapper;
import com.example.ingestor.table.TableBuilder;
import com.example.ingestor.table.UpsertTableBuilder;
import com.example.ingestor.validator.ConfigValidator;
import com.example.ingestor.validator.UpsertValidator;

/**
 * Factory for upsert write mode components
 */
public class UpsertFactory implements IngestionFactory {
    
    @Override
    public JsonToRowDataMapper createMapper(IngestionConfig config) {
        UpsertMapper mapper = new UpsertMapper();
        mapper.initialize(config);
        return mapper;
    }
    
    @Override
    public TableBuilder createTableBuilder(IngestionConfig config) {
        return new UpsertTableBuilder();
    }
    
    @Override
    public ConfigValidator createValidator(IngestionConfig config) {
        return new UpsertValidator();
    }
    
    @Override
    public String getSupportedWriteMode() {
        return "upsert";
    }
}
