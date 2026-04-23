package com.playforgemanager.application.save;

import java.util.Objects;

public record SaveFixtureData(
        int week,
        String homeTeamId,
        String awayTeamId,
        SavePlayedMatchData playedMatch
) {
    public SaveFixtureData {
        if (week < 1) {
            throw new IllegalArgumentException("Fixture week must be at least 1.");
        }
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
