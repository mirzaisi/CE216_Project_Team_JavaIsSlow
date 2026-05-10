package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveSessionData(
        String sportId,
        String progressionState,
        String controlledTeamId,
        SaveSeasonData season,
        List<SaveFieldPolicy> fieldPolicies
) {

    public SaveSessionData {
        // Validates and cleans the main session identity fields.
        sportId = requireText(sportId, "Sport id cannot be blank.");
        progressionState = requireText(progressionState, "Progression state cannot be blank.");
        controlledTeamId = requireText(controlledTeamId, "Controlled team id cannot be blank.");

        // Every saved session must contain season data.
        Objects.requireNonNull(season, "Season data cannot be null.");

        // Stores field policies as a safe unmodifiable list.
        fieldPolicies = List.copyOf(Objects.requireNonNull(fieldPolicies, "Field policies cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return cleaned;
    }
}
