package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveGameDocument(
        String formatId,
        int formatVersion,
        SaveSessionData session
) {
    public SaveGameDocument {
        formatId = requireText(formatId, "Format id cannot be blank.");
        if (formatVersion < 1) {
            throw new IllegalArgumentException("Format version must be at least 1.");
        }
        session = Objects.requireNonNull(session, "Session data cannot be null.");
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
