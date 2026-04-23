package com.playforgemanager.application;

public record PlayerSummaryView(
        String id,
        String name,
        String roleLabel,
        boolean available,
        int injuryMatchesRemaining,
        boolean selectedForCurrentLineup
) {
}
