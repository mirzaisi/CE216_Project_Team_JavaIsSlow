package com.playforgemanager.handball;

import com.playforgemanager.core.Lineup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class HandballLineup implements Lineup {
    private final List<HandballPlayer> startingPlayers;
    private final List<HandballPlayer> benchPlayers;

    public HandballLineup(List<HandballPlayer> startingPlayers) {
        this(startingPlayers, List.of());
    }

    public HandballLineup(List<HandballPlayer> startingPlayers, List<HandballPlayer> benchPlayers) {
        this.startingPlayers = copyAndValidate(startingPlayers, "Starting players cannot be null.");
        this.benchPlayers = copyAndValidate(benchPlayers, "Bench players cannot be null.");

        // A handball lineup must always contain at least one starter.
        if (this.startingPlayers.isEmpty()) {
            throw new IllegalArgumentException("Lineup must contain at least one starting player.");
        }

        validateNoDuplicatesAcrossLists();
    }

    @Override
    public List<HandballPlayer> getSelectedPlayers() {
        return startingPlayers;
    }

    @Override
    public int size() {
        return startingPlayers.size();
    }

    public List<HandballPlayer> getStartingPlayers() {
        return startingPlayers;
    }

    public List<HandballPlayer> getBenchPlayers() {
        return benchPlayers;
    }

    public List<HandballPlayer> getAllPlayers() {
        List<HandballPlayer> allPlayers = new ArrayList<>(startingPlayers.size() + benchPlayers.size());

        allPlayers.addAll(startingPlayers);
        allPlayers.addAll(benchPlayers);

        return List.copyOf(allPlayers);
    }

    public boolean containsPlayerId(String playerId) {
        // Player id must be valid before checking the lineup.
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id cannot be blank.");
        }

        for (HandballPlayer player : getAllPlayers()) {
            if (player.getId().equals(playerId)) {
                return true;
            }
        }

        return false;
    }

    private List<HandballPlayer> copyAndValidate(List<HandballPlayer> players, String nullMessage) {
        Objects.requireNonNull(players, nullMessage);

        List<HandballPlayer> validated = new ArrayList<>(players.size());

        // Copies players while rejecting null entries.
        for (HandballPlayer player : players) {
            validated.add(Objects.requireNonNull(player, "Lineup player cannot be null."));
        }

        return List.copyOf(validated);
    }

    private void validateNoDuplicatesAcrossLists() {
        Set<String> ids = new HashSet<>();

        // Checks duplicates among starting players.
        for (HandballPlayer player : startingPlayers) {
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate player in lineup: " + player.getId());
            }
        }

        // Checks duplicates between starters and bench players.
        for (HandballPlayer player : benchPlayers) {
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate player in lineup: " + player.getId());
            }
        }
    }
}
