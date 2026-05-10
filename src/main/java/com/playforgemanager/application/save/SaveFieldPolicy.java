package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveFieldPolicy(
        String fieldPath,
        PersistenceMode mode,
        String reason
) {

    public SaveFieldPolicy {
        // Validates and cleans the field path before saving it.
        fieldPath = requireText(fieldPath, "Field path cannot be blank.");

        // Ensures every field policy has a valid persistence mode.
        Objects.requireNonNull(mode, "Persistence mode cannot be null.");

        // Explains why this field has the selected persistence behavior.
        reason = requireText(reason, "Reason cannot be blank.");
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return cleaned;
    }
}
