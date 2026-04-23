package com.playforgemanager.application;

import com.playforgemanager.core.ProgressionState;

public record TeamOverviewView(
        String sportId,
        String sportName,
        String leagueName,
        String controlledTeamName,
        int currentWeek,
        boolean seasonCompleted,
        ProgressionState progressionState,
        int currentRank,
        int leagueSize,
        int rosterSize,
        int availablePlayers,
        Integer selectedLineupSize,
        String selectedTacticName,
        String trainingFocus,
        FixtureSummaryView nextFixture
) {
}
