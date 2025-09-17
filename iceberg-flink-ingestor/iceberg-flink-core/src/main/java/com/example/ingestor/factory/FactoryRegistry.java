package com.example.ingestor.factory;

import com.example.config.IngestionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for managing ingestion factories
 */
public class FactoryRegistry {
    
    private static final Logger LOG = LoggerFactory.getLogger(FactoryRegistry.class);
    private static final Map<String, IngestionFactory> FACTORIES = new HashMap<>();
    
    static {
        // Register built-in factories
        registerFactory(new AppendOnlyFactory());
        registerFactory(new UpsertFactory());
        
        // Load additional factories from classpath
        loadFactoriesFromClasspath();
    }
    
    /**
     * Get factory for the specified write mode
     */
    public static IngestionFactory getFactory(String writeMode) {
        IngestionFactory factory = FACTORIES.get(writeMode.toLowerCase());
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for write mode: " + writeMode);
        }
        return factory;
    }
    
    /**
     * Get factory for the specified configuration
     */
    public static IngestionFactory getFactory(IngestionConfig config) {
        return getFactory(config.getTable().getWriteMode());
    }
    
    /**
     * Register a factory
     */
    public static void registerFactory(IngestionFactory factory) {
        String writeMode = factory.getSupportedWriteMode().toLowerCase();
        FACTORIES.put(writeMode, factory);
        LOG.info("Registered factory for write mode: {}", writeMode);
    }
    
    /**
     * Load additional factories from classpath using ServiceLoader
     */
    private static void loadFactoriesFromClasspath() {
        try {
            ServiceLoader<IngestionFactory> loader = ServiceLoader.load(IngestionFactory.class);
            for (IngestionFactory factory : loader) {
                registerFactory(factory);
            }
        } catch (Exception e) {
            LOG.warn("Failed to load additional factories from classpath", e);
        }
    }
    
    /**
     * Get all supported write modes
     */
    public static String[] getSupportedWriteModes() {
        return FACTORIES.keySet().toArray(new String[0]);
    }
}
