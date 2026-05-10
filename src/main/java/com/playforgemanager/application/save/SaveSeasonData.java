package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveSeasonData(
        String leagueName,
        int currentWeek,
        boolean completed,
        List<SaveTeamData> teams,
        List<SaveFixtureData> fixtures
) {

    public SaveSeasonData {
        // Validates and cleans the league name before saving.
        leagueName = requireText(leagueName);

        // The saved season must always point to a valid week number.
        if (currentWeek < 1) {
            throw new IllegalArgumentException("Current week must be at least 1.");
        }

        // Stores teams and fixtures as safe unmodifiable lists.
        teams = List.copyOf(Objects.requireNonNull(teams, "Teams cannot be null."));
        fixtures = List.copyOf(Objects.requireNonNull(fixtures, "Fixtures cannot be null."));
    }

    private static String requireText(String value) {
        String cleaned = Objects.requireNonNull(value, "League name cannot be blank.").trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("League name cannot be blank.");
        }

        return cleaned;
    }
}
