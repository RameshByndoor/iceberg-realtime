package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to load configuration from JSON files
 */
public class ConfigLoader {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Load configuration from a file path
     */
    public static IngestionConfig loadFromFile(String configPath) throws IOException {
        LOG.info("Loading configuration from file: {}", configPath);
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath);
        }
        return OBJECT_MAPPER.readValue(configFile, IngestionConfig.class);
    }

    /**
     * Load configuration from classpath resource
     */
    public static IngestionConfig loadFromResource(String resourcePath) throws IOException {
        LOG.info("Loading configuration from classpath resource: {}", resourcePath);
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Configuration resource not found: " + resourcePath);
            }
            return OBJECT_MAPPER.readValue(inputStream, IngestionConfig.class);
        }
    }

    /**
     * Load configuration from JSON string
     */
    public static IngestionConfig loadFromString(String jsonConfig) throws IOException {
        LOG.info("Loading configuration from JSON string");
        return OBJECT_MAPPER.readValue(jsonConfig, IngestionConfig.class);
    }

    /**
     * Validate configuration
     */
    public static void validate(IngestionConfig config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
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
        
        LOG.info("Configuration validation passed for table: {}", config.getTable().getName());
    }
}
