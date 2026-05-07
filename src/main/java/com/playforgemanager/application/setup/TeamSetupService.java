package com.playforgemanager.application.setup;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Tactic;

import java.util.List;
import java.util.Objects;

public class TeamSetupService {
    private final TeamSetupRegistry registry;

    public TeamSetupService(TeamSetupRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "Team setup registry cannot be null.");
    }

    public TeamSetupView buildView(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        TeamSetupAdapter adapter = registry.getAdapter(validatedSession.getSelectedSportId());
        List<LineupSlotView> starters = adapter.describeLineup(validatedSession);
        List<LineupSlotView> bench = adapter.describeBench(validatedSession);
        List<TacticOptionView> tactics = adapter.tacticOptions(validatedSession);
        String currentTacticId = adapter.currentTacticId(validatedSession);
        TacticOptionView selectedOption = tactics.stream()
                .filter(option -> option.id().equals(currentTacticId))
                .findFirst()
                .orElse(null);
        String validationMessage = adapter.validateLineup(validatedSession);

        return new TeamSetupView(
                validatedSession.getSelectedSportId(),
                validatedSession.getActiveSport().getName(),
                validatedSession.getControlledTeam().getName(),
                adapter.requiredStarters(),
                adapter.benchLimit(),
                validationMessage == null,
                validationMessage,
                starters,
                bench,
                selectedOption == null ? null : selectedOption.id(),
                selectedOption == null ? null : selectedOption.name(),
                selectedOption == null ? null : selectedOption.description(),
                tactics
        );
    }

    public void autoPickLineup(GameSession session) {
        registry.getAdapter(requireSportId(session)).autoPickLineup(session);
    }

    public void applyTactic(GameSession session, String tacticId) {
        registry.getAdapter(requireSportId(session)).applyTactic(session, tacticId);
    }

    public Lineup currentLineup(GameSession session) {
        return registry.getAdapter(requireSportId(session)).currentLineup(session);
    }

    public Tactic currentTactic(GameSession session) {
        return registry.getAdapter(requireSportId(session)).currentTactic(session);
    }

    private String requireSportId(GameSession session) {
        return Objects.requireNonNull(session, "Game session cannot be null.").getSelectedSportId();
    }
}
