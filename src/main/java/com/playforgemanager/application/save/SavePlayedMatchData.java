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
        // Match scores must always be valid saved result values.
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Match scores cannot be negative.");
        }

        // A played match must preserve both teams' lineups.
        Objects.requireNonNull(homeLineup, "Home lineup cannot be null.");
        Objects.requireNonNull(awayLineup, "Away lineup cannot be null.");

        // A played match must also preserve both teams' tactics.
        Objects.requireNonNull(homeTactic, "Home tactic cannot be null.");
        Objects.requireNonNull(awayTactic, "Away tactic cannot be null.");
    }
}
