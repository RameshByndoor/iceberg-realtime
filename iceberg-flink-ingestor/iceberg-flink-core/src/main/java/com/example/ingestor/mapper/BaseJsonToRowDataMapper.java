package com.example.ingestor.mapper;

import com.example.config.IngestionConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.table.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Base implementation for JSON to RowData mappers
 */
public abstract class BaseJsonToRowDataMapper implements JsonToRowDataMapper {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseJsonToRowDataMapper.class);
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    protected IngestionConfig config;
    protected List<IngestionConfig.FieldConfig> fields;
    
    @Override
    public void initialize(IngestionConfig config) {
        this.config = config;
        this.fields = config.getTable().getSchema();
    }
    
    @Override
    public RowData map(String jsonString) throws Exception {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString);
            GenericRowData rowData = new GenericRowData(fields.size());

            for (int i = 0; i < fields.size(); i++) {
                IngestionConfig.FieldConfig field = fields.get(i);
                String fieldName = field.getMapping() != null ? field.getMapping() : field.getName();
                
                JsonNode valueNode = jsonNode.get(fieldName);
                Object value = convertValue(valueNode, field);
                rowData.setField(i, value);
            }

            return rowData;
        } catch (Exception e) {
            LOG.error("Error parsing JSON: {}", jsonString, e);
            throw e;
        }
    }
    
    /**
     * Convert JSON value to appropriate Flink type
     */
    protected Object convertValue(JsonNode valueNode, IngestionConfig.FieldConfig field) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        String type = field.getType().toLowerCase();
        
        switch (type) {
            case "long":
            case "bigint":
                return valueNode.asLong();
            case "int":
            case "integer":
                return valueNode.asInt();
            case "double":
            case "float":
                return valueNode.asDouble();
            case "string":
            case "varchar":
                return StringData.fromString(valueNode.asText());
            case "boolean":
                return valueNode.asBoolean();
            case "timestamp":
            case "timestamptz":
                return TimestampData.fromLocalDateTime(
                        LocalDateTime.parse(valueNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
            default:
                LOG.warn("Unknown field type: {}, treating as string", type);
                return StringData.fromString(valueNode.asText());
        }
    }
}
