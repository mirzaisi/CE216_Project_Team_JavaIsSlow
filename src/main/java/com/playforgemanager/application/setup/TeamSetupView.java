package com.playforgemanager.application.setup;

import java.util.List;

public record TeamSetupView(
        String sportId,
        String sportName,
        String controlledTeamName,
        int requiredStarters,
        int benchLimit,
        boolean lineupValid,
        String lineupValidationMessage,
        List<LineupSlotView> startingLineup,
        List<LineupSlotView> bench,
        String selectedTacticId,
        String selectedTacticName,
        String selectedTacticDescription,
        List<TacticOptionView> tacticOptions
) {
}
