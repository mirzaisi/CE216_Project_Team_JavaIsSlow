package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveFixtureData(
        int week,
        String homeTeamId,
        String awayTeamId,
        SavePlayedMatchData playedMatch
) {

    public SaveFixtureData {
        // Fixture weeks start from 1, so zero or negative weeks are invalid.
        if (week < 1) {
            throw new IllegalArgumentException("Fixture week must be at least 1.");
        }

        // Team ids are required because every fixture must connect two teams.
        homeTeamId = requireText(homeTeamId, "Home team id cannot be blank.");
        awayTeamId = requireText(awayTeamId, "Away team id cannot be blank.");
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return cleaned;
    }
}
