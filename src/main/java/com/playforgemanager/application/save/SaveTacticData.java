package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveTacticData(
        String name,
        List<SavePropertyValue> properties
) {

    public SaveTacticData {
        // Validates and cleans the tactic name before saving.
        name = requireText(name);

        // Stores tactic properties as a safe unmodifiable list.
        properties = List.copyOf(Objects.requireNonNull(properties, "Tactic properties cannot be null."));
    }

    private static String requireText(String value) {
        String cleaned = Objects.requireNonNull(value, "Tactic name cannot be blank.").trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Tactic name cannot be blank.");
        }

        return cleaned;
    }
}
