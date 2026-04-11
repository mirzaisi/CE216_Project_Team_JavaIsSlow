package com.playforgemanager.football;

import com.playforgemanager.core.Player;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FootballTeam extends Team {
    private final List<FootballCoach> coaches;

    public FootballTeam(String id, String name) {
        super(id, name);
        this.coaches = new ArrayList<>();
    }

    @Override
    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null.");
        if (!(player instanceof FootballPlayer footballPlayer)) {
            throw new IllegalArgumentException("FootballTeam only accepts FootballPlayer instances.");
        }
        super.addPlayer(footballPlayer);
    }

    public boolean removePlayerById(String playerId) {
        boolean removed = super.removePlayer(playerId);
        if (removed && getSelectedFootballLineup() != null && getSelectedFootballLineup().containsPlayerId(playerId)) {
            setSelectedLineup(null);
        }
        return removed;
    }

    public void addCoach(FootballCoach coach) {
        FootballCoach validatedCoach = Objects.requireNonNull(coach, "Coach cannot be null.");
        boolean duplicateId = coaches.stream().anyMatch(existing -> existing.getId().equals(validatedCoach.getId()));
        if (duplicateId) {
            throw new IllegalArgumentException("A coach with the same id is already in the staff.");
        }
        coaches.add(validatedCoach);
    }

    public List<FootballCoach> getCoaches() {
        return Collections.unmodifiableList(coaches);
    }

    public List<FootballPlayer> getFootballPlayers() {
        List<FootballPlayer> players = new ArrayList<>(getRoster().size());
        for (Player player : getRoster()) {
            players.add((FootballPlayer) player);
        }
        return Collections.unmodifiableList(players);
    }

    public List<FootballPlayer> getPlayersByPosition(FootballPosition position) {
        Objects.requireNonNull(position, "Position cannot be null.");
        return getFootballPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .sorted(Comparator.comparing(FootballPlayer::getName))
                .toList();
    }

    public List<FootballPlayer> getAvailablePlayers() {
        return getFootballPlayers().stream()
                .filter(FootballPlayer::isAvailable)
                .sorted(Comparator.comparing((FootballPlayer player) -> player.getPosition().ordinal())
                        .thenComparing(FootballPlayer::getName))
                .toList();
    }

    public void assignLineup(FootballLineup lineup) {
        FootballLineup validatedLineup = Objects.requireNonNull(lineup, "Lineup cannot be null.");
        validateLineupBelongsToRoster(validatedLineup);
        setSelectedLineup(validatedLineup);
    }

    public void assignLineup(FootballLineup lineup, FootballRuleset ruleset) {
        FootballLineup validatedLineup = Objects.requireNonNull(lineup, "Lineup cannot be null.");
        Objects.requireNonNull(ruleset, "Football ruleset cannot be null.");
        validateLineupBelongsToRoster(validatedLineup);
        ruleset.validateLineupOrThrow(validatedLineup);
        setSelectedLineup(validatedLineup);
    }

    public FootballLineup getSelectedFootballLineup() {
        return getSelectedLineup() == null ? null : (FootballLineup) getSelectedLineup();
    }

    public void assignTactic(FootballTactic tactic) {
        setSelectedTactic(Objects.requireNonNull(tactic, "Tactic cannot be null."));
    }

    public FootballTactic getSelectedFootballTactic() {
        return getSelectedTactic() == null ? null : (FootballTactic) getSelectedTactic();
    }

    public void assignTrainingPlan(FootballTrainingPlan trainingPlan) {
        setTrainingPlan(Objects.requireNonNull(trainingPlan, "Training plan cannot be null."));
    }

    public FootballTrainingPlan getSelectedFootballTrainingPlan() {
        return getTrainingPlan() == null ? null : (FootballTrainingPlan) getTrainingPlan();
    }

    private void validateLineupBelongsToRoster(FootballLineup lineup) {
        List<String> rosterIds = getFootballPlayers().stream().map(FootballPlayer::getId).toList();
        boolean invalidSelection = lineup.getAllPlayers().stream().anyMatch(player -> !rosterIds.contains(player.getId()));
        if (invalidSelection) {
            throw new IllegalArgumentException("Lineup contains a player that does not belong to the team roster.");
        }
    }
}
