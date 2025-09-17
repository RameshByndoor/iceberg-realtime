package com.example.ingestor.factory;

import com.example.config.IngestionConfig;
import com.example.ingestor.mapper.AppendOnlyMapper;
import com.example.ingestor.mapper.JsonToRowDataMapper;
import com.example.ingestor.table.AppendOnlyTableBuilder;
import com.example.ingestor.table.TableBuilder;
import com.example.ingestor.validator.AppendOnlyValidator;
import com.example.ingestor.validator.ConfigValidator;

/**
 * Simple factory for append-only write mode
 * Creates basic components with minimal overhead
 */
public class AppendOnlyFactory implements IngestionFactory {
    
    @Override
    public JsonToRowDataMapper createMapper(IngestionConfig config) {
        AppendOnlyMapper mapper = new AppendOnlyMapper();
        mapper.initialize(config);
        return mapper;
    }
    
    @Override
    public TableBuilder createTableBuilder(IngestionConfig config) {
        return new AppendOnlyTableBuilder();
    }
    
    @Override
    public ConfigValidator createValidator(IngestionConfig config) {
        return new AppendOnlyValidator();
    }
    
    @Override
    public String getSupportedWriteMode() {
        return "append";
    }
}
