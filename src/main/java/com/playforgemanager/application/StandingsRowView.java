package com.playforgemanager.application;

public record StandingsRowView(
        int rank,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,
        int scoresFor,
        int scoresAgainst,
        int scoreDifference,
        int points
) {
}
