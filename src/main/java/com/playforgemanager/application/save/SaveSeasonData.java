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
        leagueName = requireText(leagueName, "League name cannot be blank.");
        if (currentWeek < 1) {
            throw new IllegalArgumentException("Current week must be at least 1.");
        }
        teams = List.copyOf(Objects.requireNonNull(teams, "Teams cannot be null."));
        fixtures = List.copyOf(Objects.requireNonNull(fixtures, "Fixtures cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
