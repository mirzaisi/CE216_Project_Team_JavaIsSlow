package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SavePlayerData(
        String id,
        String name,
        boolean available,
        int injuryMatchesRemaining,
        List<SavePropertyValue> properties
) {

    public SavePlayerData {
        // Validates and cleans the basic player identity fields.
        id = requireText(id, "Player id cannot be blank.");
        name = requireText(name, "Player name cannot be blank.");

        // Injury countdown cannot go below zero in saved data.
        if (injuryMatchesRemaining < 0) {
            throw new IllegalArgumentException("Injury matches remaining cannot be negative.");
        }

        // Stores player properties as a safe unmodifiable list.
        properties = List.copyOf(Objects.requireNonNull(properties, "Player properties cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return cleaned;
    }
}
