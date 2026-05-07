package com.playforgemanager.handball;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandballStandingsPolicyTest {

    @Test
    void recordMatchUpdatesCalculatedTableForPlayedMatch() {
        HandballRuleset ruleset = new HandballRuleset();
        HandballStandingsPolicy standingsPolicy = new HandballStandingsPolicy(ruleset);
        League league = buildLeague();

        Match match = new HandballMatch(league.getTeams().get(0), league.getTeams().get(1));
        match.setResult(31, 26);

        standingsPolicy.recordMatch(league, match);

        List<HandballStandingRow> table = standingsPolicy.calculateTable(league);
        HandballStandingRow redHawks = findRow(table, "Red Hawks");
        HandballStandingRow blueWolves = findRow(table, "Blue Wolves");

        assertEquals(1, redHawks.getPlayed());
        assertEquals(2, redHawks.getPoints());
        assertEquals(1, redHawks.getWins());
        assertEquals(5, redHawks.getGoalDifference());

        assertEquals(1, blueWolves.getPlayed());
        assertEquals(0, blueWolves.getPoints());
        assertEquals(1, blueWolves.getLosses());
        assertEquals(-5, blueWolves.getGoalDifference());
    }

    @Test
    void calculateTableUpdatesStandingsAfterOnePlayedMatch() {
        HandballRuleset ruleset = new HandballRuleset();
        HandballStandingsPolicy standingsPolicy = new HandballStandingsPolicy(ruleset);
        League league = buildLeague();

        Fixture fixture = new Fixture(1, league.getTeams().get(0), league.getTeams().get(1));
        Match match = new HandballMatch(fixture.getHomeTeam(), fixture.getAwayTeam());
        match.setResult(28, 27);
        fixture.attachPlayedMatch(match);
        league.addFixture(fixture);

        List<HandballStandingRow> table = standingsPolicy.calculateTable(league);

        HandballStandingRow leader = findRow(table, "Red Hawks");
        HandballStandingRow blueWolves = findRow(table, "Blue Wolves");

        assertEquals("Red Hawks", leader.getTeam().getName());
        assertEquals(2, leader.getPoints());
        assertEquals(1, leader.getWins());
        assertEquals(1, leader.getGoalDifference());

        assertEquals(0, blueWolves.getPoints());
        assertEquals(1, blueWolves.getLosses());
        assertEquals(-1, blueWolves.getGoalDifference());
    }

    @Test
    void calculateTableSortsByPointsThenGoalDifferenceThenGoalsFor() {
        HandballRuleset ruleset = new HandballRuleset();
        HandballStandingsPolicy standingsPolicy = new HandballStandingsPolicy(ruleset);
        League league = buildLeague();

        Fixture first = new Fixture(1, league.getTeams().get(0), league.getTeams().get(1));
        Match firstMatch = new HandballMatch(first.getHomeTeam(), first.getAwayTeam());
        firstMatch.setResult(33, 29);
        first.attachPlayedMatch(firstMatch);

        Fixture second = new Fixture(1, league.getTeams().get(2), league.getTeams().get(3));
        Match secondMatch = new HandballMatch(second.getHomeTeam(), second.getAwayTeam());
        secondMatch.setResult(30, 28);
        second.attachPlayedMatch(secondMatch);

        league.addFixture(first);
        league.addFixture(second);

        List<HandballStandingRow> table = standingsPolicy.calculateTable(league);

        assertEquals("Red Hawks", table.get(0).getTeam().getName());
        assertEquals("Golden Stars", table.get(1).getTeam().getName());
    }

    private League buildLeague() {
        HandballLeague league = new HandballLeague("Test League");
        league.addTeam(new HandballTeam("team-1", "Red Hawks"));
        league.addTeam(new HandballTeam("team-2", "Blue Wolves"));
        league.addTeam(new HandballTeam("team-3", "Golden Stars"));
        league.addTeam(new HandballTeam("team-4", "Iron Lions"));
        return league;
    }

    private HandballStandingRow findRow(List<HandballStandingRow> table, String teamName) {
        return table.stream()
                .filter(row -> row.getTeam().getName().equals(teamName))
                .findFirst()
                .orElseThrow();
    }
}
