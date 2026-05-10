package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveGameDocument(
        String formatId,
        int formatVersion,
        SaveSessionData session
) {

    public SaveGameDocument {
        // Cleans and validates the save format identifier.
        formatId = requireText(formatId);

        // Save format versions must start from 1.
        if (formatVersion < 1) {
            throw new IllegalArgumentException("Format version must be at least 1.");
        }

        // Every save document must contain a session.
        Objects.requireNonNull(session, "Session data cannot be null.");
    }

    private static String requireText(String value) {
        String cleaned = Objects.requireNonNull(value, "Format id cannot be blank.").trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Format id cannot be blank.");
        }

        return cleaned;
    }
}
