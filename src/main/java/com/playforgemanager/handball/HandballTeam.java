package com.playforgemanager.handball;

import com.playforgemanager.core.Player;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HandballTeam extends Team {
    private final List<HandballCoach> coaches;

    public HandballTeam(String id, String name) {
        super(id, name);
        this.coaches = new ArrayList<>();
    }

    @Override
    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null.");
        if (!(player instanceof HandballPlayer handballPlayer)) {
            throw new IllegalArgumentException("HandballTeam only accepts HandballPlayer instances.");
        }
        super.addPlayer(handballPlayer);
    }

    public boolean removePlayerById(String playerId) {
        boolean removed = super.removePlayer(playerId);
        if (removed && getSelectedHandballLineup() != null && getSelectedHandballLineup().containsPlayerId(playerId)) {
            setSelectedLineup(null);
        }
        return removed;
    }

    public void addCoach(HandballCoach coach) {
        HandballCoach validatedCoach = Objects.requireNonNull(coach, "Coach cannot be null.");
        boolean duplicateId = coaches.stream().anyMatch(existing -> existing.getId().equals(validatedCoach.getId()));
        if (duplicateId) {
            throw new IllegalArgumentException("A coach with the same id is already in the staff.");
        }
        coaches.add(validatedCoach);
    }

    public List<HandballCoach> getCoaches() {
        return Collections.unmodifiableList(coaches);
    }

    public List<HandballPlayer> getHandballPlayers() {
        List<HandballPlayer> players = new ArrayList<>(getRoster().size());
        for (Player player : getRoster()) {
            players.add((HandballPlayer) player);
        }
        return Collections.unmodifiableList(players);
    }

    public List<HandballPlayer> getPlayersByPosition(HandballPosition position) {
        Objects.requireNonNull(position, "Position cannot be null.");
        return getHandballPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .sorted(Comparator.comparing(HandballPlayer::getName))
                .toList();
    }

    public List<HandballPlayer> getAvailablePlayers() {
        return getHandballPlayers().stream()
                .filter(HandballPlayer::isAvailable)
                .sorted(Comparator.comparing((HandballPlayer player) -> player.getPosition().ordinal())
                        .thenComparing(HandballPlayer::getName))
                .toList();
    }

    public void assignLineup(HandballLineup lineup) {
        HandballLineup validatedLineup = Objects.requireNonNull(lineup, "Lineup cannot be null.");
        validateLineupBelongsToRoster(validatedLineup);
        setSelectedLineup(validatedLineup);
    }

    public void assignLineup(HandballLineup lineup, HandballRuleset ruleset) {
        HandballLineup validatedLineup = Objects.requireNonNull(lineup, "Lineup cannot be null.");
        Objects.requireNonNull(ruleset, "Handball ruleset cannot be null.");
        validateLineupBelongsToRoster(validatedLineup);
        ruleset.validateLineupOrThrow(validatedLineup);
        setSelectedLineup(validatedLineup);
    }

    public HandballLineup getSelectedHandballLineup() {
        return getSelectedLineup() == null ? null : (HandballLineup) getSelectedLineup();
    }

    public void assignTactic(HandballTactic tactic) {
        setSelectedTactic(Objects.requireNonNull(tactic, "Tactic cannot be null."));
    }

    public HandballTactic getSelectedHandballTactic() {
        return getSelectedTactic() == null ? null : (HandballTactic) getSelectedTactic();
    }

    public void assignTrainingPlan(HandballTrainingPlan trainingPlan) {
        setTrainingPlan(Objects.requireNonNull(trainingPlan, "Training plan cannot be null."));
    }

    public HandballTrainingPlan getSelectedHandballTrainingPlan() {
        return getTrainingPlan() == null ? null : (HandballTrainingPlan) getTrainingPlan();
    }

    private void validateLineupBelongsToRoster(HandballLineup lineup) {
        List<String> rosterIds = getHandballPlayers().stream().map(HandballPlayer::getId).toList();
        boolean invalidSelection = lineup.getAllPlayers().stream().anyMatch(player -> !rosterIds.contains(player.getId()));
        if (invalidSelection) {
            throw new IllegalArgumentException("Lineup contains a player that does not belong to the team roster.");
        }
    }
}
