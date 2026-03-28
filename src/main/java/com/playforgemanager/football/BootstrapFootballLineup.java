package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BootstrapFootballLineup implements Lineup {
    private final List<Player> selectedPlayers;

    public BootstrapFootballLineup(List<Player> selectedPlayers) {
        Objects.requireNonNull(selectedPlayers, "Selected players cannot be null.");

        if (selectedPlayers.isEmpty()) {
            throw new IllegalArgumentException("Lineup cannot be empty.");
        }

        Set<String> ids = new HashSet<>();
        for (Player player : selectedPlayers) {
            Objects.requireNonNull(player, "Lineup player cannot be null.");
            if (!ids.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate player in lineup: " + player.getId());
            }
        }

        this.selectedPlayers = List.copyOf(selectedPlayers);
    }

    @Override
    public List<Player> getSelectedPlayers() {
        return selectedPlayers;
    }

    @Override
    public int size() {
        return selectedPlayers.size();
    }
}