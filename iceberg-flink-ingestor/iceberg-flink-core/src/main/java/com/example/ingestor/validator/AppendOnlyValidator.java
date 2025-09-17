package com.example.ingestor.validator;

import com.example.config.IngestionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for append-only write mode
 */
public class AppendOnlyValidator extends BaseConfigValidator {
    
    private static final Logger LOG = LoggerFactory.getLogger(AppendOnlyValidator.class);
    
    @Override
    public String getSupportedWriteMode() {
        return "append";
    }
    
    @Override
    protected void validateWriteModeSpecific(IngestionConfig config) {
        // For append-only mode, we don't need equality fields
        // Just ensure the schema is valid
        validateSchema(config);
        LOG.info("Append-only configuration validation passed");
    }
    
    private void validateSchema(IngestionConfig config) {
        for (IngestionConfig.FieldConfig field : config.getTable().getSchema()) {
            if (field.getName() == null || field.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Field name cannot be empty");
            }
            if (field.getType() == null || field.getType().trim().isEmpty()) {
                throw new IllegalArgumentException("Field type cannot be empty for field: " + field.getName());
            }
        }
    }
}
