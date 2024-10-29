package org.example.couponcore.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@UtilityClass
public class JacksonUtils {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public <T> T toModel(String value, Class<T> valueType) {
        return parseValue(value, valueType);
    }

    public <T> T toModelOrNull(String value, Class<T> valueType) {
        return parseValueOrNull(value, valueType);
    }

    public <T> T toModelOrNull(byte[] value, Class<T> valueType) {
        return parseValueOrNull(value, valueType);
    }

    public <T> String toString(T t) {
        return convertToString(t);
    }

    public <T> String toString(List<T> list) {
        return convertListToString(list);
    }

    public <T> String toStringOrEmpty(T t) {
        return convertToStringOrEmpty(t);
    }

    private <T> T parseValue(String value, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(value, valueType);
        } catch (IOException e) {
            log.error("JacksonUtils parseValue exception: {}", e.getMessage());
            throw new RuntimeException("Failed to parse value", e);
        }
    }

    private <T> T parseValueOrNull(String value, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(value, valueType);
        } catch (IOException e) {
            log.warn("JacksonUtils parseValueOrNull exception: {}", e.getMessage());
            return null;
        }
    }

    private <T> T parseValueOrNull(byte[] value, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(value, valueType);
        } catch (IOException e) {
            log.warn("JacksonUtils parseValueOrNull exception (byte[]): {}", e.getMessage());
            return null;
        }
    }

    private <T> String convertToString(T t) {
        try {
            if (t instanceof String) {
                return (String) t;
            }
            return OBJECT_MAPPER.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.error("JacksonUtils convertToString exception: {}", e.getMessage());
            throw new RuntimeException("Failed to convert to string", e);
        }
    }

    private <T> String convertToStringOrEmpty(T t) {
        try {
            if (t instanceof String) {
                return (String) t;
            }
            return OBJECT_MAPPER.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.warn("JacksonUtils convertToStringOrEmpty exception: {}", e.getMessage());
            return "";
        }
    }

    private <T> String convertListToString(List<T> list) {
        if (list == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            stringBuilder.append(item instanceof String ? "\"" + item + "\"" : convertToString(item));
            if (i < list.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
