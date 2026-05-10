package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveTrainingPlanData(
        String focus,
        int intensity,
        List<SavePropertyValue> properties
) {

    public SaveTrainingPlanData {
        // Validates and cleans the training focus before saving.
        focus = requireText(focus);

        // Training intensity cannot be stored as a negative value.
        if (intensity < 0) {
            throw new IllegalArgumentException("Training intensity cannot be negative.");
        }

        // Stores training plan properties as a safe unmodifiable list.
        properties = List.copyOf(Objects.requireNonNull(properties, "Training plan properties cannot be null."));
    }

    private static String requireText(String value) {
        String cleaned = Objects.requireNonNull(value, "Training focus cannot be blank.").trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Training focus cannot be blank.");
        }

        return cleaned;
    }
}
