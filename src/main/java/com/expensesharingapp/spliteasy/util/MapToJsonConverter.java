package com.expensesharingapp.spliteasy.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA converter to store Map<Long, BigDecimal> as JSON in DB.
 */
@Converter
public class MapToJsonConverter implements AttributeConverter<Map<Long, BigDecimal>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<Long, BigDecimal> attribute) {
        if (attribute == null || attribute.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<Long, BigDecimal> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<Long, BigDecimal>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
