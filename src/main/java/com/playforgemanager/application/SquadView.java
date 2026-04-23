package com.playforgemanager.application;

import java.util.List;

public record SquadView(
        String teamName,
        int totalPlayers,
        int availablePlayers,
        List<PlayerSummaryView> players
) {
}
