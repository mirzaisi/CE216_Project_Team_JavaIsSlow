package com.playforgemanager.core;

import com.playforgemanager.football.BootstrapFootballLeague;
import com.playforgemanager.football.BootstrapFootballMatch;
import com.playforgemanager.football.BootstrapFootballSeason;
import com.playforgemanager.football.BootstrapFootballTeam;
import com.playforgemanager.football.FootballSport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixtureTest {

    @Test
    void constructorStoresWeekHomeAndAwayTeams() {
        Team home = new BootstrapFootballTeam("team-1", "Red Hawks");
        Team away = new BootstrapFootballTeam("team-2", "Blue Wolves");

        Fixture fixture = new Fixture(1, home, away);

        assertEquals(1, fixture.getWeek());
        assertEquals(home, fixture.getHomeTeam());
        assertEquals(away, fixture.getAwayTeam());
        assertFalse(fixture.isPlayed());
    }

    @Test
    void constructorRejectsSameTeamForHomeAndAway() {
        Team team = new BootstrapFootballTeam("team-1", "Red Hawks");

        assertThrows(IllegalArgumentException.class, () ->
                new Fixture(1, team, team)
        );
    }

    @Test
    void constructorRejectsWeekLessThanOne() {
        Team home = new BootstrapFootballTeam("team-1", "Red Hawks");
        Team away = new BootstrapFootballTeam("team-2", "Blue Wolves");

        assertThrows(IllegalArgumentException.class, () ->
                new Fixture(0, home, away)
        );
    }

    @Test
    void attachPlayedMatchMarksFixtureAsPlayed() {
        Team home = new BootstrapFootballTeam("team-1", "Red Hawks");
        Team away = new BootstrapFootballTeam("team-2", "Blue Wolves");

        Fixture fixture = new Fixture(1, home, away);
        Match match = new BootstrapFootballMatch(home, away);
        match.setResult(2, 1);

        fixture.attachPlayedMatch(match);

        assertTrue(fixture.isPlayed());
        assertEquals(match, fixture.getPlayedMatch());
    }

    @Test
    void attachPlayedMatchRejectsTeamsThatDoNotMatchFixture() {
        Team home = new BootstrapFootballTeam("team-1", "Red Hawks");
        Team away = new BootstrapFootballTeam("team-2", "Blue Wolves");
        Team other = new BootstrapFootballTeam("team-3", "Golden Stars");

        Fixture fixture = new Fixture(1, home, away);
        Match wrongMatch = new BootstrapFootballMatch(home, other);

        assertThrows(IllegalArgumentException.class, () ->
                fixture.attachPlayedMatch(wrongMatch)
        );
    }
}