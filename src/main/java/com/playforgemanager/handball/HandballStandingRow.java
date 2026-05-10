package com.playforgemanager.handball;

import com.playforgemanager.core.Team;

import java.util.Objects;

public class HandballStandingRow {
    private final Team team;

    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public HandballStandingRow(Team team) {
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

    public int getLosses() {
        return losses;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getPoints() {
        return points;
    }

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    public void recordMatch(int goalsScored, int goalsConceded, HandballRuleset ruleset) {
        Objects.requireNonNull(ruleset, "Handball ruleset cannot be null.");

        played++;
        goalsFor += goalsScored;
        goalsAgainst += goalsConceded;

        HandballRuleset.MatchOutcome outcome = ruleset.determineOutcome(goalsScored, goalsConceded);

        // Updates the result counters based on the match outcome.
        switch (outcome) {
            case WIN -> wins++;
            case DRAW -> draws++;
            case LOSS -> losses++;
        }

        points += ruleset.getPointsForOutcome(outcome);
    }
}
