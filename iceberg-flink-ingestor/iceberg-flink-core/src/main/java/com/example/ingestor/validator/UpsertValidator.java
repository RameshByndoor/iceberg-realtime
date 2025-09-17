package com.example.ingestor.validator;

import com.example.config.IngestionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for upsert write mode
 */
public class UpsertValidator extends BaseConfigValidator {
    
    private static final Logger LOG = LoggerFactory.getLogger(UpsertValidator.class);
    
    @Override
    public String getSupportedWriteMode() {
        return "upsert";
    }
    
    @Override
    protected void validateWriteModeSpecific(IngestionConfig config) {
        // For upsert mode, we need equality fields
        validateEqualityFields(config);
        validateSchema(config);
        LOG.info("Upsert configuration validation passed");
    }
    
    private void validateEqualityFields(IngestionConfig config) {
        List<String> equalityFields = config.getTable().getEqualityFields();
        
        if (equalityFields == null || equalityFields.isEmpty()) {
            throw new IllegalArgumentException("Upsert mode requires equality fields to be specified");
        }
        
        // Validate that equality fields exist in the schema
        Set<String> schemaFieldNames = config.getTable().getSchema().stream()
                .map(IngestionConfig.FieldConfig::getName)
                .collect(Collectors.toSet());
        
        for (String equalityField : equalityFields) {
            if (!schemaFieldNames.contains(equalityField)) {
                throw new IllegalArgumentException(
                    "Equality field '" + equalityField + "' not found in table schema"
                );
            }
        }
        
        LOG.info("Equality fields validation passed: {}", equalityFields);
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
