package com.playforgemanager.application.setup;

import java.util.Objects;

public record TacticOptionView(
        String id,
        String name,
        String description,
        boolean selected
) {

    public TacticOptionView {
        // Validates and cleans the tactic option identity fields.
        id = require(id, "Tactic option id cannot be blank.");
        name = require(name, "Tactic option name cannot be blank.");

        // Keeps the description safe for display even when no text is provided.
        description = description == null ? "" : description;
    }

    private static String require(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return cleaned;
    }
}
