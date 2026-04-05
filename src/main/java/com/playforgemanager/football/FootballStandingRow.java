package com.playforgemanager.football;

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

    public Team getTeam() { return team; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getPoints() { return points; }
    public int getGoalDifference() { return goalsFor - goalsAgainst; }

    public void recordMatch(int goalsScored, int goalsConceded, FootballRuleset ruleset) {
        Objects.requireNonNull(ruleset, "Football ruleset cannot be null.");
        played++;
        goalsFor += goalsScored;
        goalsAgainst += goalsConceded;

        FootballRuleset.MatchOutcome outcome = ruleset.determineOutcome(goalsScored, goalsConceded);
        switch (outcome) {
            case WIN -> wins++;
            case DRAW -> draws++;
            case LOSS -> losses++;
        }
        points += ruleset.getPointsForOutcome(outcome);
    }
}
