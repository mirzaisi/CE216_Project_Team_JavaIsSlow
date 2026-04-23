package com.playforgemanager.application;

public record FixtureSummaryView(
        int week,
        String homeTeamName,
        String awayTeamName,
        boolean played,
        Integer homeScore,
        Integer awayScore,
        boolean controlledTeamInvolved,
        boolean controlledTeamHome
) {
}
