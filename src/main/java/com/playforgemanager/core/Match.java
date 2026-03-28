package com.playforgemanager.core;

import java.util.Objects;

public abstract class Match {
    private final Team homeTeam;
    private final Team awayTeam;

    private Lineup homeLineup;
    private Lineup awayLineup;
    private Tactic homeTactic;
    private Tactic awayTactic;

    private boolean played;
    private int homeScore;
    private int awayScore;

    protected Match(Team homeTeam, Team awayTeam) {
        this.homeTeam = Objects.requireNonNull(homeTeam, "Home team cannot be null.");
        this.awayTeam = Objects.requireNonNull(awayTeam, "Away team cannot be null.");

        if (homeTeam == awayTeam) {
            throw new IllegalArgumentException("Home and away teams must be different.");
        }

        this.played = false;
        this.homeScore = 0;
        this.awayScore = 0;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public Lineup getHomeLineup() {
        return homeLineup;
    }

    public Lineup getAwayLineup() {
        return awayLineup;
    }

    public Tactic getHomeTactic() {
        return homeTactic;
    }

    public Tactic getAwayTactic() {
        return awayTactic;
    }

    public boolean isPlayed() {
        return played;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setHomeSetup(Lineup lineup, Tactic tactic) {
        this.homeLineup = Objects.requireNonNull(lineup, "Home lineup cannot be null.");
        this.homeTactic = Objects.requireNonNull(tactic, "Home tactic cannot be null.");
    }

    public void setAwaySetup(Lineup lineup, Tactic tactic) {
        this.awayLineup = Objects.requireNonNull(lineup, "Away lineup cannot be null.");
        this.awayTactic = Objects.requireNonNull(tactic, "Away tactic cannot be null.");
    }

    public void setResult(int homeScore, int awayScore) {
        if (played) {
            throw new IllegalStateException("Match result is already set.");
        }
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative.");
        }

        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.played = true;
    }
}