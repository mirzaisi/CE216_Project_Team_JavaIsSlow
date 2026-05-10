package com.playforgemanager.application.setup;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Tactic;

import java.util.List;

public interface TeamSetupAdapter {
    int requiredStarters();

    int benchLimit();

    // Returns the current starter lineup as display-ready slots.
    List<LineupSlotView> describeLineup(GameSession session);

    // Returns the current bench players as display-ready slots.
    List<LineupSlotView> describeBench(GameSession session);

    // Returns validation feedback for the current lineup setup.
    String validateLineup(GameSession session);

    // Automatically selects a valid lineup for the current team.
    void autoPickLineup(GameSession session);

    // Lists all tactic choices available for the current sport/team.
    List<TacticOptionView> tacticOptions(GameSession session);

    String currentTacticId(GameSession session);

    void applyTactic(GameSession session, String tacticId);

    Lineup currentLineup(GameSession session);

    Tactic currentTactic(GameSession session);
}
