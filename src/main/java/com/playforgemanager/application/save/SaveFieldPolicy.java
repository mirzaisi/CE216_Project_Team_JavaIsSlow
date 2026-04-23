package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveFieldPolicy(
        String fieldPath,
        PersistenceMode mode,
        String reason
) {
    public SaveFieldPolicy {
        fieldPath = requireText(fieldPath, "Field path cannot be blank.");
        mode = Objects.requireNonNull(mode, "Persistence mode cannot be null.");
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
