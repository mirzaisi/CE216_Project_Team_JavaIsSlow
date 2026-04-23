package com.playforgemanager.application;

import java.util.List;

public record StandingsView(
        String sportId,
        String leagueName,
        int currentWeek,
        List<StandingsRowView> rows
) {
}
