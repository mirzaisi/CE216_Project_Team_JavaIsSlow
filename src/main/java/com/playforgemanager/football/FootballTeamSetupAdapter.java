package com.playforgemanager.football;

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

public class FootballTeamSetupAdapter implements TeamSetupAdapter {
    private final FootballRuleset ruleset = new FootballRuleset();
    private final Map<String, FootballTactic> tacticPresets = buildTacticPresets();
    private final Map<String, String> tacticDescriptions = buildTacticDescriptions();

    @Override
    public int requiredStarters() {
        return FootballRuleset.STARTING_LINEUP_SIZE;
    }

    @Override
    public int benchLimit() {
        return FootballRuleset.MAX_BENCH_SIZE;
    }

    @Override
    public List<LineupSlotView> describeLineup(GameSession session) {
        FootballLineup lineup = currentFootballLineup(session);
        if (lineup == null) {
            return List.of();
        }
        return toSlotViews(lineup.getStartingPlayers(), 0);
    }

    @Override
    public List<LineupSlotView> describeBench(GameSession session) {
        FootballLineup lineup = currentFootballLineup(session);
        if (lineup == null) {
            return List.of();
        }
        return toSlotViews(lineup.getBenchPlayers(), lineup.getStartingPlayers().size());
    }

    @Override
    public String validateLineup(GameSession session) {
        FootballLineup lineup = currentFootballLineup(session);
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
        FootballTeam team = controlledTeam(session);
        FootballLineup lineup = ruleset.buildLineup(team.getFootballPlayers());
        team.assignLineup(lineup, ruleset);
    }

    @Override
    public List<TacticOptionView> tacticOptions(GameSession session) {
        String currentId = currentTacticId(session);
        List<TacticOptionView> options = new ArrayList<>(tacticPresets.size());
        for (Map.Entry<String, FootballTactic> entry : tacticPresets.entrySet()) {
            FootballTactic tactic = entry.getValue();
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
        FootballTactic current = currentFootballTactic(session);
        if (current == null) {
            return null;
        }
        for (Map.Entry<String, FootballTactic> entry : tacticPresets.entrySet()) {
            if (matchesPreset(entry.getValue(), current)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void applyTactic(GameSession session, String tacticId) {
        FootballTactic tactic = tacticPresets.get(Objects.requireNonNull(tacticId, "Tactic id cannot be null."));
        if (tactic == null) {
            throw new IllegalArgumentException("Unknown football tactic id: " + tacticId);
        }
        controlledTeam(session).assignTactic(tactic);
    }

    @Override
    public Lineup currentLineup(GameSession session) {
        return currentFootballLineup(session);
    }

    @Override
    public Tactic currentTactic(GameSession session) {
        return currentFootballTactic(session);
    }

    private FootballTeam controlledTeam(GameSession session) {
        Team team = Objects.requireNonNull(session, "Game session cannot be null.").getControlledTeam();
        if (!(team instanceof FootballTeam footballTeam)) {
            throw new IllegalStateException("Football setup adapter requires a FootballTeam.");
        }
        return footballTeam;
    }

    private FootballLineup currentFootballLineup(GameSession session) {
        return controlledTeam(session).getSelectedFootballLineup();
    }

    private FootballTactic currentFootballTactic(GameSession session) {
        return controlledTeam(session).getSelectedFootballTactic();
    }

    private List<LineupSlotView> toSlotViews(List<FootballPlayer> players, int startIndex) {
        List<LineupSlotView> slots = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            FootballPlayer player = players.get(i);
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

    private boolean matchesPreset(FootballTactic preset, FootballTactic current) {
        return preset.getName().equals(current.getName())
                && preset.getFormation().equals(current.getFormation())
                && preset.getMentality() == current.getMentality()
                && preset.getPressingIntensity() == current.getPressingIntensity()
                && preset.getAttackingWidth() == current.getAttackingWidth();
    }

    private Map<String, FootballTactic> buildTacticPresets() {
        Map<String, FootballTactic> presets = new LinkedHashMap<>();
        presets.put(
                "balanced-control",
                new FootballTactic("Balanced Control", "4-2-3-1", FootballTactic.Mentality.BALANCED, 58, 60)
        );
        presets.put(
                "high-press",
                new FootballTactic("High Press", "4-3-3", FootballTactic.Mentality.ATTACKING, 74, 68)
        );
        presets.put(
                "compact-counter",
                new FootballTactic("Compact Counter", "4-4-2", FootballTactic.Mentality.DEFENSIVE, 50, 45)
        );
        presets.put(
                "possession-shape",
                new FootballTactic("Possession Shape", "4-3-3", FootballTactic.Mentality.BALANCED, 54, 72)
        );
        return presets;
    }

    private Map<String, String> buildTacticDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        descriptions.put("balanced-control", "Patient build-up with a 4-2-3-1, even pressing and width.");
        descriptions.put("high-press", "Aggressive 4-3-3 with high pressing and attacking intent.");
        descriptions.put("compact-counter", "4-4-2 with deep block and quick counters.");
        descriptions.put("possession-shape", "4-3-3 with wider shape, looking to keep the ball.");
        return descriptions;
    }
}
