package com.playforgemanager.application.save;

import java.util.Objects;

public record SavePropertyValue(
        String key,
        String valueType,
        String value
) {
    public SavePropertyValue {
        key = requireText(key, "Property key cannot be blank.");
        valueType = requireText(valueType, "Property value type cannot be blank.");
        value = requireText(value, "Property value cannot be blank.");
    }

    private static String requireText(String text, String message) {
        String cleaned = Objects.requireNonNull(text, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
