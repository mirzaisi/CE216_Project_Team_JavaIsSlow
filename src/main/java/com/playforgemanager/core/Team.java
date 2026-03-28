package com.playforgemanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class Team {
    private final String id;
    private final String name;
    private final List<Player> roster;
    private Lineup selectedLineup;
    private Tactic selectedTactic;
    private TrainingPlan trainingPlan;

    protected Team(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Team id cannot be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be blank.");
        }
        this.id = id;
        this.name = name;
        this.roster = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Player> getRoster() {
        return Collections.unmodifiableList(roster);
    }

    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null.");
        boolean duplicateId = roster.stream()
                .anyMatch(existing -> existing.getId().equals(player.getId()));
        if (duplicateId) {
            throw new IllegalArgumentException("A player with the same id is already in the roster.");
        }
        roster.add(player);
    }

    public boolean removePlayer(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id cannot be blank.");
        }
        return roster.removeIf(player -> player.getId().equals(playerId));
    }

    public Lineup getSelectedLineup() {
        return selectedLineup;
    }

    public void setSelectedLineup(Lineup selectedLineup) {
        this.selectedLineup = selectedLineup;
    }

    public Tactic getSelectedTactic() {
        return selectedTactic;
    }

    public void setSelectedTactic(Tactic selectedTactic) {
        this.selectedTactic = selectedTactic;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
    }
}