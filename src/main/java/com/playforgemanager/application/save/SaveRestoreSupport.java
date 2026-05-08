package com.playforgemanager.application.save;

import com.playforgemanager.core.ProgressionState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SaveRestoreSupport {
    private SaveRestoreSupport() {
    }

    public static Map<String, String> propertyMap(List<SavePropertyValue> properties) {
        Map<String, String> values = new LinkedHashMap<>();
        for (SavePropertyValue property : Objects.requireNonNull(properties, "Properties cannot be null.")) {
            values.put(property.key(), property.value());
        }
        return values;
    }

    public static String requireString(Map<String, String> values, String key, String sport) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing saved " + sport + " property: " + key);
        }
        return value;
    }

    public static int requireInt(Map<String, String> values, String key, String sport) {
        try {
            return Integer.parseInt(requireString(values, key, sport));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Saved " + sport + " property must be an integer: " + key,
                    exception
            );
        }
    }

    public static int optionalInt(Map<String, String> values, String key, int defaultValue, String sport) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Saved " + sport + " property must be an integer: " + key,
                    exception
            );
        }
    }

    public static boolean requireBoolean(Map<String, String> values, String key, String sport) {
        String value = requireString(values, key, sport);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(
                    "Saved " + sport + " property must be true or false: " + key
            );
        }
        return Boolean.parseBoolean(value);
    }

    public static boolean optionalBoolean(Map<String, String> values, String key, boolean defaultValue, String sport) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(
                    "Saved " + sport + " property must be true or false: " + key
            );
        }
        return Boolean.parseBoolean(value);
    }

    public static <T extends Enum<T>> T requireEnum(Class<T> enumType, String value, String sport) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Saved " + sport + " property has unsupported value: " + value,
                    exception
            );
        }
    }

    public static ProgressionState requireProgressionState(String value) {
        try {
            return ProgressionState.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported saved progression state: " + value, exception);
        }
    }
}
