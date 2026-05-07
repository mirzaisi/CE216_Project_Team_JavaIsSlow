package com.playforgemanager.application.setup;

public record LineupSlotView(
        int slotIndex,
        String roleLabel,
        String playerId,
        String playerName,
        boolean available
) {
}
