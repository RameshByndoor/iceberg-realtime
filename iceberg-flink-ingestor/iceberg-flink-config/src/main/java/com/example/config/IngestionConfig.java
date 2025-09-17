package com.example.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for table ingestion
 */
public class IngestionConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("table")
    private TableConfig table;
    
    @JsonProperty("kafka")
    private KafkaConfig kafka;
    
    @JsonProperty("iceberg")
    private IcebergConfig iceberg;
    
    @JsonProperty("aws")
    private AwsConfig aws;

    // Getters and Setters
    public TableConfig getTable() { return table; }
    public void setTable(TableConfig table) { this.table = table; }
    
    public KafkaConfig getKafka() { return kafka; }
    public void setKafka(KafkaConfig kafka) { this.kafka = kafka; }
    
    public IcebergConfig getIceberg() { return iceberg; }
    public void setIceberg(IcebergConfig iceberg) { this.iceberg = iceberg; }
    
    public AwsConfig getAws() { return aws; }
    public void setAws(AwsConfig aws) { this.aws = aws; }

    public static class TableConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("namespace")
        private String namespace = "default";
        
        @JsonProperty("schema")
        private List<FieldConfig> schema;
        
        @JsonProperty("write_mode")
        private String writeMode = "append"; // append, upsert
        
        @JsonProperty("equality_fields")
        private List<String> equalityFields;
        
        @JsonProperty("partition_fields")
        private List<String> partitionFields;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
        
        public List<FieldConfig> getSchema() { return schema; }
        public void setSchema(List<FieldConfig> schema) { this.schema = schema; }
        
        public String getWriteMode() { return writeMode; }
        public void setWriteMode(String writeMode) { this.writeMode = writeMode; }
        
        public List<String> getEqualityFields() { return equalityFields; }
        public void setEqualityFields(List<String> equalityFields) { this.equalityFields = equalityFields; }
        
        public List<String> getPartitionFields() { return partitionFields; }
        public void setPartitionFields(List<String> partitionFields) { this.partitionFields = partitionFields; }
    }

    public static class FieldConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("required")
        private boolean required = false;
        
        @JsonProperty("mapping")
        private String mapping; // JSON field mapping

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public String getMapping() { return mapping; }
        public void setMapping(String mapping) { this.mapping = mapping; }
    }

    public static class KafkaConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonProperty("bootstrap_servers")
        private String bootstrapServers;
        
        @JsonProperty("topic")
        private String topic;
        
        @JsonProperty("group_id")
        private String groupId;
        
        @JsonProperty("starting_offset")
        private String startingOffset = "latest";
        
        @JsonProperty("properties")
        private Map<String, String> properties;

        // Getters and Setters
        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        
        public String getStartingOffset() { return startingOffset; }
        public void setStartingOffset(String startingOffset) { this.startingOffset = startingOffset; }
        
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }

    public static class IcebergConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonProperty("catalog_uri")
        private String catalogUri;
        
        @JsonProperty("warehouse")
        private String warehouse;
        
        @JsonProperty("io_impl")
        private String ioImpl;
        
        @JsonProperty("properties")
        private Map<String, String> properties;

        // Getters and Setters
        public String getCatalogUri() { return catalogUri; }
        public void setCatalogUri(String catalogUri) { this.catalogUri = catalogUri; }
        
        public String getWarehouse() { return warehouse; }
        public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
        
        public String getIoImpl() { return ioImpl; }
        public void setIoImpl(String ioImpl) { this.ioImpl = ioImpl; }
        
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }

    public static class AwsConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonProperty("region")
        private String region;
        
        @JsonProperty("access_key_id")
        private String accessKeyId;
        
        @JsonProperty("secret_access_key")
        private String secretAccessKey;
        
        @JsonProperty("endpoint")
        private String endpoint;
        
        @JsonProperty("path_style_access")
        private boolean pathStyleAccess = true;

        // Getters and Setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
        
        public String getSecretAccessKey() { return secretAccessKey; }
        public void setSecretAccessKey(String secretAccessKey) { this.secretAccessKey = secretAccessKey; }
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        
        public boolean isPathStyleAccess() { return pathStyleAccess; }
        public void setPathStyleAccess(boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }
    }
}
