package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveTacticData(
        String name,
        List<SavePropertyValue> properties
) {
    public SaveTacticData {
        name = requireText(name, "Tactic name cannot be blank.");
        properties = List.copyOf(Objects.requireNonNull(properties, "Tactic properties cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
