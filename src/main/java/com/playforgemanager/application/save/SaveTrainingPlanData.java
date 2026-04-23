package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveTrainingPlanData(
        String focus,
        int intensity,
        List<SavePropertyValue> properties
) {
    public SaveTrainingPlanData {
        focus = requireText(focus, "Training focus cannot be blank.");
        if (intensity < 0) {
            throw new IllegalArgumentException("Training intensity cannot be negative.");
        }
        properties = List.copyOf(Objects.requireNonNull(properties, "Training plan properties cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
