package com.playforgemanager.application.save;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public record SaveGameResult(
        Path savePath,
        String formatId,
        int formatVersion,
        String sportId,
        String controlledTeamId,
        Instant savedAt
) {
    public SaveGameResult {
        savePath = Objects.requireNonNull(savePath, "Save path cannot be null.");
        formatId = requireText(formatId, "Format id cannot be blank.");
        if (formatVersion < 1) {
            throw new IllegalArgumentException("Format version must be at least 1.");
        }
        sportId = requireText(sportId, "Sport id cannot be blank.");
        controlledTeamId = requireText(controlledTeamId, "Controlled team id cannot be blank.");
        savedAt = Objects.requireNonNull(savedAt, "Saved timestamp cannot be null.");
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
