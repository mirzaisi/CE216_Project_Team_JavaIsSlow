package com.playforgemanager.football;

import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class FootballStandingRow {
    private final Team team;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public FootballStandingRow(Team team) {
        this.team = Objects.requireNonNull(team, "Team cannot be null.");
    }

    public Team getTeam() {
        return team;
    }

    public int getPlayed() {
        return played;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public int getPoints() {
        return points;
    }

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    public void recordMatch(int goalsScored, int goalsConceded, Ruleset ruleset) {
        Objects.requireNonNull(ruleset, "Ruleset cannot be null.");

        if (goalsScored < 0 || goalsConceded < 0) {
            throw new IllegalArgumentException("Goals cannot be negative.");
        }

        played++;
        goalsFor += goalsScored;
        goalsAgainst += goalsConceded;

        if (goalsScored > goalsConceded) {
            wins++;
            points += ruleset.getWinPoints();
        } else if (goalsScored < goalsConceded) {
            losses++;
            points += ruleset.getLossPoints();
        } else {
            draws++;
            points += ruleset.getDrawPoints();
        }
    }
}
