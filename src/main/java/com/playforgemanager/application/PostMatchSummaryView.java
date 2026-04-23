package com.playforgemanager.application;

import com.playforgemanager.core.ProgressionState;

import java.util.List;

public record PostMatchSummaryView(
        String sportId,
        int weekNumber,
        String controlledTeamName,
        String opponentTeamName,
        boolean controlledTeamHome,
        int controlledTeamScore,
        int opponentScore,
        String outcomeLabel,
        int controlledTeamRankAfterMatch,
        ProgressionState progressionState,
        List<TeamAvailabilityChange> availabilityChanges,
        FixtureSummaryView fixture
) {
}
