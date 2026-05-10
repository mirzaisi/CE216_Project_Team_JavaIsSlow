package com.playforgemanager.football;

import com.playforgemanager.core.Coach;
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

        // Clears the selected lineup if the removed player was part of it.
        if (removed && getSelectedFootballLineup() != null && getSelectedFootballLineup().containsPlayerId(playerId)) {
            setSelectedLineup(null);
        }

        return removed;
    }

    public void addCoach(FootballCoach coach) {
        FootballCoach validatedCoach = Objects.requireNonNull(coach, "Coach cannot be null.");
        String coachId = validatedCoach.getId();
        boolean duplicateId = false;

        // Prevents adding two coaches with the same id.
        for (Coach existing : coaches) {
            if (existing.getId().equals(coachId)) {
                duplicateId = true;
                break;
            }
        }

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

        // Converts the generic roster into football players.
        for (Player player : getRoster()) {
            players.add((FootballPlayer) player);
        }

        return Collections.unmodifiableList(players);
    }

    public List<FootballPlayer> getPlayersByPosition(FootballPosition position) {
        Objects.requireNonNull(position, "Position cannot be null.");

        List<FootballPlayer> matchingPlayers = new ArrayList<>();

        // Collects players matching the requested football position.
        for (FootballPlayer player : getFootballPlayers()) {
            if (player.getPosition() == position) {
                matchingPlayers.add(player);
            }
        }

        matchingPlayers.sort(Comparator.comparing(Player::getName));

        return List.copyOf(matchingPlayers);
    }

    public List<FootballPlayer> getAvailablePlayers() {
        List<FootballPlayer> availablePlayers = new ArrayList<>();

        // Collects only players who are currently available for selection.
        for (FootballPlayer player : getFootballPlayers()) {
            if (player.isAvailable()) {
                availablePlayers.add(player);
            }
        }

        availablePlayers.sort(Comparator
                .comparingInt((FootballPlayer player) -> player.getPosition().ordinal())
                .thenComparing(Player::getName));

        return List.copyOf(availablePlayers);
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
        List<String> rosterIds = new ArrayList<>();

        // Builds a list of valid roster player ids.
        for (FootballPlayer player : getFootballPlayers()) {
            rosterIds.add(player.getId());
        }

        boolean invalidSelection = false;

        // Checks that every selected lineup player belongs to this team.
        for (FootballPlayer player : lineup.getAllPlayers()) {
            if (!rosterIds.contains(player.getId())) {
                invalidSelection = true;
                break;
            }
        }

        if (invalidSelection) {
            throw new IllegalArgumentException("Lineup contains a player that does not belong to the team roster.");
        }
    }
}
