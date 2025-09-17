package com.example.ingestor.validator;

import com.example.config.IngestionConfig;

/**
 * Interface for validating ingestion configuration
 */
public interface ConfigValidator {
    
    /**
     * Validate the configuration
     * @param config The configuration to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validate(IngestionConfig config);
    
    /**
     * Get the write mode this validator supports
     */
    String getSupportedWriteMode();
}
