package com.example.ingestor.mapper;

import com.example.config.IngestionConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.table.data.RowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Mapper for upsert write mode
 * Includes additional validation for equality fields
 */
public class UpsertMapper extends BaseJsonToRowDataMapper {
    
    private static final Logger LOG = LoggerFactory.getLogger(UpsertMapper.class);
    private List<String> equalityFields;
    
    @Override
    public void initialize(IngestionConfig config) {
        super.initialize(config);
        this.equalityFields = config.getTable().getEqualityFields();
        
        if (equalityFields == null || equalityFields.isEmpty()) {
            LOG.warn("Upsert mode requires equality fields to be specified");
        }
    }
    
    @Override
    public String getSupportedWriteMode() {
        return "upsert";
    }
    
    @Override
    public RowData map(String jsonString) throws Exception {
        // For upsert, we need to validate equality fields are present
        validateEqualityFields(jsonString);
        LOG.debug("Processing upsert record: {}", jsonString);
        return super.map(jsonString);
    }
    
    /**
     * Validate that all equality fields are present in the JSON
     */
    private void validateEqualityFields(String jsonString) throws Exception {
        if (equalityFields == null || equalityFields.isEmpty()) {
            return;
        }
        
        JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString);
        
        for (String equalityField : equalityFields) {
            if (!jsonNode.has(equalityField)) {
                throw new IllegalArgumentException(
                    "Equality field '" + equalityField + "' is missing from JSON: " + jsonString
                );
            }
        }
    }
}
