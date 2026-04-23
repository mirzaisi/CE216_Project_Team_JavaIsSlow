package com.playforgemanager.application.save;

import java.util.Objects;

public record SavePlayedMatchData(
        int homeScore,
        int awayScore,
        SaveLineupData homeLineup,
        SaveLineupData awayLineup,
        SaveTacticData homeTactic,
        SaveTacticData awayTactic
) {
    public SavePlayedMatchData {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Match scores cannot be negative.");
        }
        homeLineup = Objects.requireNonNull(homeLineup, "Home lineup cannot be null.");
        awayLineup = Objects.requireNonNull(awayLineup, "Away lineup cannot be null.");
        homeTactic = Objects.requireNonNull(homeTactic, "Home tactic cannot be null.");
        awayTactic = Objects.requireNonNull(awayTactic, "Away tactic cannot be null.");
    }
}
