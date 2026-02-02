package com.core.bank.demo.util;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_FIELDS = Set.of("accessToken", "access_token", "refreshToken",
            "refresh_token", "token", "password", "secret");
    private static final String MASKED_VALUE = "******";

    private JsonUtils() {
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static String toJsonMasked(Object object) {
        try {
            String json = MAPPER.writeValueAsString(object);
            JsonNode node = MAPPER.readTree(json);
            if (node.isObject()) {
                maskRecursive((ObjectNode) node);
                return MAPPER.writeValueAsString(node);
            }
            return json;
        } catch (Exception e) {
            log.trace("Could not mask sensitive data: {}", e.getMessage());
            return null;
        }
    }

    public static JsonNode maskSensitiveFields(JsonNode node) {
        if (node == null || !node.isObject()) {
            return node;
        }
        ObjectNode masked = node.deepCopy();
        maskRecursive(masked);
        return masked;
    }

    private static void maskRecursive(ObjectNode node) {
        node.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();

            if (SENSITIVE_FIELDS.contains(fieldName) && value.isTextual()) {
                node.put(fieldName, MASKED_VALUE);
            } else if (value.isObject()) {
                maskRecursive((ObjectNode) value);
            } else if (value.isArray()) {
                value.forEach(item -> {
                    if (item.isObject()) {
                        maskRecursive((ObjectNode) item);
                    }
                });
            }
        });
    }
}
