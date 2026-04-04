package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonTest {

    @Test
    void seasonStartsAtWeekOneAndHasGeneratedFixtures() {
        FootballSeason season = buildSeason();

        assertEquals(1, season.getCurrentWeek());
        assertFalse(season.isCompleted());
        assertFalse(season.getCurrentWeekFixtures().isEmpty());
    }

    @Test
    void playCurrentWeekSimulatesMatchesAndAdvancesSeason() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        season.playCurrentWeek(sport, BootstrapFootballMatch::new);

        assertEquals(2, season.getCurrentWeek());
        assertTrue(season.getLeague().getFixtures().stream().anyMatch(fixture -> fixture.isPlayed()));
    }

    @Test
    void playCurrentWeekSkipsAlreadyPlayedFixtureInsteadOfReplayingIt() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        var firstFixture = season.getCurrentWeekFixtures().get(0);
        var manualMatch = new BootstrapFootballMatch(firstFixture.getHomeTeam(), firstFixture.getAwayTeam());
        manualMatch.setResult(1, 1);
        firstFixture.attachPlayedMatch(manualMatch);

        season.playCurrentWeek(sport, BootstrapFootballMatch::new);

        assertEquals(1, firstFixture.getPlayedMatch().getHomeScore());
        assertEquals(1, firstFixture.getPlayedMatch().getAwayScore());
    }

    @Test
    void seasonMarksCompletedAfterAllWeeksArePlayed() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        while (!season.isCompleted()) {
            season.playCurrentWeek(sport, BootstrapFootballMatch::new);
        }

        assertTrue(season.isCompleted());
        assertTrue(season.canCreateNextSeason());
        assertNotNull(season.createNextSeasonStub());
    }

    private FootballSeason buildSeason() {
        FootballLeague league = new FootballLeague("Integration League");
        addTeam(league, "team-1", "Red Hawks");
        addTeam(league, "team-2", "Blue Wolves");
        addTeam(league, "team-3", "Golden Stars");
        addTeam(league, "team-4", "Iron Lions");

        List<Fixture> fixtures = new RoundRobinFootballScheduler().generateFixtures(league.getTeams());
        league.addFixtures(fixtures);

        return new FootballSeason(league);
    }

    private void addTeam(FootballLeague league, String id, String name) {
        BootstrapFootballTeam team = new BootstrapFootballTeam(id, name);

        for (int i = 0; i < 18; i++) {
            team.addPlayer(new BootstrapFootballPlayer(id + "-player-" + i, name + " Player " + i));
        }

        league.addTeam(team);
    }
}
