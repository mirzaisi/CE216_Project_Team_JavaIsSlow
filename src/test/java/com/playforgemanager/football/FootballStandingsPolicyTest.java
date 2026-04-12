package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FootballStandingsPolicyTest {

    @Test
    void recordMatchUpdatesCalculatedTableForPlayedMatch() {
        FootballRuleset ruleset = new FootballSport().getFootballRuleset();
        FootballStandingsPolicy standingsPolicy = new FootballStandingsPolicy(ruleset);
        League league = buildLeague();

        Match match = new FootballMatch(league.getTeams().get(0), league.getTeams().get(1));
        match.setResult(3, 0);

        standingsPolicy.recordMatch(league, match);

        List<FootballStandingRow> table = standingsPolicy.calculateTable(league);
        FootballStandingRow redHawks = findRow(table, "Red Hawks");
        FootballStandingRow blueWolves = findRow(table, "Blue Wolves");

        assertEquals(1, redHawks.getPlayed());
        assertEquals(3, redHawks.getPoints());
        assertEquals(1, redHawks.getWins());
        assertEquals(3, redHawks.getGoalDifference());

        assertEquals(1, blueWolves.getPlayed());
        assertEquals(0, blueWolves.getPoints());
        assertEquals(1, blueWolves.getLosses());
        assertEquals(-3, blueWolves.getGoalDifference());
    }

    @Test
    void calculateTableUpdatesStandingsAfterOnePlayedMatch() {
        FootballRuleset ruleset = new FootballSport().getFootballRuleset();
        FootballStandingsPolicy standingsPolicy = new FootballStandingsPolicy(ruleset);
        League league = buildLeague();

        Fixture fixture = new Fixture(1, league.getTeams().get(0), league.getTeams().get(1));
        Match match = new FootballMatch(fixture.getHomeTeam(), fixture.getAwayTeam());
        match.setResult(2, 1);
        fixture.attachPlayedMatch(match);
        league.addFixture(fixture);

        List<FootballStandingRow> table = standingsPolicy.calculateTable(league);

        FootballStandingRow leader = findRow(table, "Red Hawks");
        FootballStandingRow blueWolves = findRow(table, "Blue Wolves");

        assertEquals("Red Hawks", leader.getTeam().getName());
        assertEquals(3, leader.getPoints());
        assertEquals(1, leader.getWins());
        assertEquals(1, leader.getGoalDifference());

        assertEquals(0, blueWolves.getPoints());
        assertEquals(1, blueWolves.getLosses());
        assertEquals(-1, blueWolves.getGoalDifference());
    }

    @Test
    void calculateTableSortsByPointsThenGoalDifferenceThenGoalsFor() {
        FootballRuleset ruleset = new FootballSport().getFootballRuleset();
        FootballStandingsPolicy standingsPolicy = new FootballStandingsPolicy(ruleset);
        League league = buildLeague();

        Fixture first = new Fixture(1, league.getTeams().get(0), league.getTeams().get(1));
        Match firstMatch = new FootballMatch(first.getHomeTeam(), first.getAwayTeam());
        firstMatch.setResult(2, 0);
        first.attachPlayedMatch(firstMatch);

        Fixture second = new Fixture(1, league.getTeams().get(2), league.getTeams().get(3));
        Match secondMatch = new FootballMatch(second.getHomeTeam(), second.getAwayTeam());
        secondMatch.setResult(1, 0);
        second.attachPlayedMatch(secondMatch);

        league.addFixture(first);
        league.addFixture(second);

        List<FootballStandingRow> table = standingsPolicy.calculateTable(league);

        assertEquals("Red Hawks", table.get(0).getTeam().getName());
        assertEquals("Golden Stars", table.get(1).getTeam().getName());
    }

    @Test
    void calculateTableDoesNotDoubleCountRecordedFixtureMatch() {
        FootballRuleset ruleset = new FootballSport().getFootballRuleset();
        FootballStandingsPolicy standingsPolicy = new FootballStandingsPolicy(ruleset);
        League league = buildLeague();

        Fixture fixture = new Fixture(1, league.getTeams().get(0), league.getTeams().get(1));
        Match match = new FootballMatch(fixture.getHomeTeam(), fixture.getAwayTeam());
        match.setResult(2, 1);
        fixture.attachPlayedMatch(match);
        league.addFixture(fixture);

        standingsPolicy.recordMatch(league, match);
        standingsPolicy.recordMatch(league, match);

        List<FootballStandingRow> table = standingsPolicy.calculateTable(league);
        FootballStandingRow redHawks = findRow(table, "Red Hawks");
        FootballStandingRow blueWolves = findRow(table, "Blue Wolves");

        assertEquals(1, redHawks.getPlayed());
        assertEquals(3, redHawks.getPoints());
        assertEquals(2, redHawks.getGoalsFor());
        assertEquals(1, redHawks.getGoalsAgainst());

        assertEquals(1, blueWolves.getPlayed());
        assertEquals(0, blueWolves.getPoints());
        assertEquals(1, blueWolves.getGoalsFor());
        assertEquals(2, blueWolves.getGoalsAgainst());
    }

    private League buildLeague() {
        FootballLeague league = new FootballLeague("Test League");
        league.addTeam(new FootballTeam("team-1", "Red Hawks"));
        league.addTeam(new FootballTeam("team-2", "Blue Wolves"));
        league.addTeam(new FootballTeam("team-3", "Golden Stars"));
        league.addTeam(new FootballTeam("team-4", "Iron Lions"));
        return league;
    }

    private FootballStandingRow findRow(List<FootballStandingRow> table, String teamName) {
        return table.stream()
                .filter(row -> row.getTeam().getName().equals(teamName))
                .findFirst()
                .orElseThrow();
    }
}
