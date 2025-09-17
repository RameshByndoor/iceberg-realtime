package com.example.ingestor.validator;

import com.example.config.IngestionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for configuration validators
 */
public abstract class BaseConfigValidator implements ConfigValidator {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseConfigValidator.class);
    
    @Override
    public void validate(IngestionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        validateBasicConfig(config);
        validateWriteModeSpecific(config);
    }
    
    /**
     * Validate basic configuration that applies to all write modes
     */
    protected void validateBasicConfig(IngestionConfig config) {
        if (config.getTable() == null) {
            throw new IllegalArgumentException("Table configuration is required");
        }
        
        if (config.getTable().getName() == null || config.getTable().getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Table name is required");
        }
        
        if (config.getKafka() == null) {
            throw new IllegalArgumentException("Kafka configuration is required");
        }
        
        if (config.getKafka().getTopic() == null || config.getKafka().getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Kafka topic is required");
        }
        
        if (config.getIceberg() == null) {
            throw new IllegalArgumentException("Iceberg configuration is required");
        }
        
        if (config.getIceberg().getCatalogUri() == null || config.getIceberg().getCatalogUri().trim().isEmpty()) {
            throw new IllegalArgumentException("Iceberg catalog URI is required");
        }
        
        if (config.getTable().getSchema() == null || config.getTable().getSchema().isEmpty()) {
            throw new IllegalArgumentException("Table schema is required");
        }
        
        LOG.info("Basic configuration validation passed for table: {}", config.getTable().getName());
    }
    
    /**
     * Validate write mode specific configuration
     * Override in subclasses for specific validation logic
     */
    protected abstract void validateWriteModeSpecific(IngestionConfig config);
}
