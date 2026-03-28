package com.playforgemanager.core;

import java.util.Objects;

public class Fixture {
    private final int week;
    private final Team homeTeam;
    private final Team awayTeam;
    private Match playedMatch;

    public Fixture(int week, Team homeTeam, Team awayTeam) {
        if (week < 1) {
            throw new IllegalArgumentException("Week must be at least 1.");
        }

        this.homeTeam = Objects.requireNonNull(homeTeam, "Home team cannot be null.");
        this.awayTeam = Objects.requireNonNull(awayTeam, "Away team cannot be null.");

        if (homeTeam == awayTeam) {
            throw new IllegalArgumentException("A team cannot play against itself.");
        }

        this.week = week;
    }

    public int getWeek() {
        return week;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public Match getPlayedMatch() {
        return playedMatch;
    }

    public void attachPlayedMatch(Match match) {
        Objects.requireNonNull(match, "Match cannot be null.");

        if (this.playedMatch != null) {
            throw new IllegalStateException("A played match is already attached to this fixture.");
        }

        if (match.getHomeTeam() != homeTeam || match.getAwayTeam() != awayTeam) {
            throw new IllegalArgumentException("Match teams must match fixture teams.");
        }

        this.playedMatch = match;
    }

    public boolean isPlayed() {
        return playedMatch != null && playedMatch.isPlayed();
    }
}