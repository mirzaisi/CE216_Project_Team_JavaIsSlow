package com.playforgemanager.application.setup;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Tactic;

import java.util.List;

public interface TeamSetupAdapter {
    int requiredStarters();
    int benchLimit();
    List<LineupSlotView> describeLineup(GameSession session);
    List<LineupSlotView> describeBench(GameSession session);
    String validateLineup(GameSession session);
    void autoPickLineup(GameSession session);
    List<TacticOptionView> tacticOptions(GameSession session);
    String currentTacticId(GameSession session);
    void applyTactic(GameSession session, String tacticId);
    Lineup currentLineup(GameSession session);
    Tactic currentTactic(GameSession session);
}
