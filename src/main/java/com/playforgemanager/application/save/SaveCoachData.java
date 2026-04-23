package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveCoachData(
        String id,
        String name,
        String role,
        List<SavePropertyValue> properties
) {
    public SaveCoachData {
        id = requireText(id, "Coach id cannot be blank.");
        name = requireText(name, "Coach name cannot be blank.");
        role = requireText(role, "Coach role cannot be blank.");
        properties = List.copyOf(Objects.requireNonNull(properties, "Coach properties cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
