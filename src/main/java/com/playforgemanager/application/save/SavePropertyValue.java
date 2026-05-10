package com.playforgemanager.application.save;

import java.util.Objects;

public record SavePropertyValue(
        String key,
        String valueType,
        String value
) {

    public SavePropertyValue {
        // Validates and cleans the property key and type before saving.
        key = requireText(key, "Property key cannot be blank.");
        valueType = requireText(valueType, "Property value type cannot be blank.");

        // Stores the property value as text after validation.
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
