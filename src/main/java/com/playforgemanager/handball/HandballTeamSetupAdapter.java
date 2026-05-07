package com.playforgemanager.handball;

import com.playforgemanager.application.setup.LineupSlotView;
import com.playforgemanager.application.setup.TacticOptionView;
import com.playforgemanager.application.setup.TeamSetupAdapter;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HandballTeamSetupAdapter implements TeamSetupAdapter {
    private final HandballRuleset ruleset = new HandballRuleset();
    private final Map<String, HandballTactic> tacticPresets = buildTacticPresets();
    private final Map<String, String> tacticDescriptions = buildTacticDescriptions();

    @Override
    public int requiredStarters() {
        return HandballRuleset.STARTING_LINEUP_SIZE;
    }

    @Override
    public int benchLimit() {
        return HandballRuleset.MAX_BENCH_SIZE;
    }

    @Override
    public List<LineupSlotView> describeLineup(GameSession session) {
        HandballLineup lineup = currentHandballLineup(session);
        if (lineup == null) {
            return List.of();
        }
        return toSlotViews(lineup.getStartingPlayers(), 0);
    }

    @Override
    public List<LineupSlotView> describeBench(GameSession session) {
        HandballLineup lineup = currentHandballLineup(session);
        if (lineup == null) {
            return List.of();
        }
        return toSlotViews(lineup.getBenchPlayers(), lineup.getStartingPlayers().size());
    }

    @Override
    public String validateLineup(GameSession session) {
        HandballLineup lineup = currentHandballLineup(session);
        if (lineup == null) {
            return "No lineup selected.";
        }
        try {
            ruleset.validateLineupOrThrow(lineup);
            return null;
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public void autoPickLineup(GameSession session) {
        HandballTeam team = controlledTeam(session);
        HandballLineup lineup = ruleset.buildLineup(team.getHandballPlayers());
        team.assignLineup(lineup, ruleset);
    }

    @Override
    public List<TacticOptionView> tacticOptions(GameSession session) {
        String currentId = currentTacticId(session);
        List<TacticOptionView> options = new ArrayList<>(tacticPresets.size());
        for (Map.Entry<String, HandballTactic> entry : tacticPresets.entrySet()) {
            HandballTactic tactic = entry.getValue();
            options.add(new TacticOptionView(
                    entry.getKey(),
                    tactic.getName(),
                    tacticDescriptions.getOrDefault(entry.getKey(), ""),
                    entry.getKey().equals(currentId)
            ));
        }
        return List.copyOf(options);
    }

    @Override
    public String currentTacticId(GameSession session) {
        HandballTactic current = currentHandballTactic(session);
        if (current == null) {
            return null;
        }
        for (Map.Entry<String, HandballTactic> entry : tacticPresets.entrySet()) {
            if (matchesPreset(entry.getValue(), current)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void applyTactic(GameSession session, String tacticId) {
        HandballTactic tactic = tacticPresets.get(Objects.requireNonNull(tacticId, "Tactic id cannot be null."));
        if (tactic == null) {
            throw new IllegalArgumentException("Unknown handball tactic id: " + tacticId);
        }
        controlledTeam(session).assignTactic(tactic);
    }

    @Override
    public Lineup currentLineup(GameSession session) {
        return currentHandballLineup(session);
    }

    @Override
    public Tactic currentTactic(GameSession session) {
        return currentHandballTactic(session);
    }

    private HandballTeam controlledTeam(GameSession session) {
        Team team = Objects.requireNonNull(session, "Game session cannot be null.").getControlledTeam();
        if (!(team instanceof HandballTeam handballTeam)) {
            throw new IllegalStateException("Handball setup adapter requires a HandballTeam.");
        }
        return handballTeam;
    }

    private HandballLineup currentHandballLineup(GameSession session) {
        return controlledTeam(session).getSelectedHandballLineup();
    }

    private HandballTactic currentHandballTactic(GameSession session) {
        return controlledTeam(session).getSelectedHandballTactic();
    }

    private List<LineupSlotView> toSlotViews(List<HandballPlayer> players, int startIndex) {
        List<LineupSlotView> slots = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            HandballPlayer player = players.get(i);
            slots.add(new LineupSlotView(
                    startIndex + i,
                    player.getPosition().name(),
                    player.getId(),
                    player.getName(),
                    player.isAvailable()
            ));
        }
        return List.copyOf(slots);
    }

    private boolean matchesPreset(HandballTactic preset, HandballTactic current) {
        return preset.getName().equals(current.getName())
                && preset.getShape().equals(current.getShape())
                && preset.getTempo() == current.getTempo()
                && preset.getPressureLevel() == current.getPressureLevel()
                && preset.getTransitionSpeed() == current.getTransitionSpeed();
    }

    private Map<String, HandballTactic> buildTacticPresets() {
        Map<String, HandballTactic> presets = new LinkedHashMap<>();
        presets.put(
                "balanced-33",
                new HandballTactic("Balanced Build-Up", "3-3", HandballTactic.Tempo.BALANCED, 58, 60)
        );
        presets.put(
                "fast-break-33",
                new HandballTactic("Fast Break", "3-3", HandballTactic.Tempo.FAST_BREAK, 68, 82)
        );
        presets.put(
                "compact-60",
                new HandballTactic("Compact Six-Zero", "6-0", HandballTactic.Tempo.CONTROLLED, 64, 42)
        );
        presets.put(
                "pressure-51",
                new HandballTactic("Balanced Pressure", "5-1", HandballTactic.Tempo.BALANCED, 72, 56)
        );
        return presets;
    }

    private Map<String, String> buildTacticDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        descriptions.put("balanced-33", "Standard 3-3 build-up. Even tempo and pressure.");
        descriptions.put("fast-break-33", "Push the tempo and exploit transitions on every turnover.");
        descriptions.put("compact-60", "Six-zero defense, slow tempo, frustrate the opponent.");
        descriptions.put("pressure-51", "Aggressive 5-1 with high pressure on the playmaker.");
        return descriptions;
    }
}
