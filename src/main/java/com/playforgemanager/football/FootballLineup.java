package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class FootballLineup implements Lineup {
    private final List<FootballPlayer> startingPlayers;
    private final List<FootballPlayer> benchPlayers;

    public FootballLineup(List<FootballPlayer> startingPlayers) {
        this(startingPlayers, List.of());
    }

    public FootballLineup(List<FootballPlayer> startingPlayers, List<FootballPlayer> benchPlayers) {
        this.startingPlayers = copyAndValidate(startingPlayers, "Starting players cannot be null.");
        this.benchPlayers = copyAndValidate(benchPlayers, "Bench players cannot be null.");

        // A football lineup must always contain at least one starter.
        if (this.startingPlayers.isEmpty()) {
            throw new IllegalArgumentException("Lineup must contain at least one starting player.");
        }

        validateNoDuplicatesAcrossLists();
    }

    @Override
    public List<FootballPlayer> getSelectedPlayers() {
        return startingPlayers;
    }

    @Override
    public int size() {
        return startingPlayers.size();
    }

    public List<FootballPlayer> getStartingPlayers() {
        return startingPlayers;
    }

    public List<FootballPlayer> getBenchPlayers() {
        return benchPlayers;
    }

    public List<FootballPlayer> getAllPlayers() {
        List<FootballPlayer> allPlayers = new ArrayList<>(startingPlayers.size() + benchPlayers.size());

        allPlayers.addAll(startingPlayers);
        allPlayers.addAll(benchPlayers);

        return List.copyOf(allPlayers);
    }

    public boolean containsPlayerId(String playerId) {
        // Player id must be valid before checking the lineup.
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id cannot be blank.");
        }

        for (Player player : getAllPlayers()) {
            if (player.getId().equals(playerId)) {
                return true;
            }
        }

        return false;
    }

    private List<FootballPlayer> copyAndValidate(List<FootballPlayer> players, String nullMessage) {
        Objects.requireNonNull(players, nullMessage);

        List<FootballPlayer> validated = new ArrayList<>(players.size());

        // Copies players while rejecting null entries.
        for (FootballPlayer player : players) {
            validated.add(Objects.requireNonNull(player, "Lineup player cannot be null."));
        }

        return List.copyOf(validated);
    }

    private void validateNoDuplicatesAcrossLists() {
        Set<String> ids = new HashSet<>();

        // Checks duplicates among starting players.
        for (FootballPlayer player : startingPlayers) {
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate player in lineup: " + player.getId());
            }
        }

        // Checks duplicates between starters and bench players.
        for (FootballPlayer player : benchPlayers) {
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate player in lineup: " + player.getId());
            }
        }
    }
}
