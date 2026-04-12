package com.playforgemanager.core;

import com.playforgemanager.football.FootballMatch;
import com.playforgemanager.football.FootballTeam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixtureTest {

    @Test
    void constructorStoresWeekHomeAndAwayTeams() {
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");

        Fixture fixture = new Fixture(1, home, away);

        assertEquals(1, fixture.getWeek());
        assertEquals(home, fixture.getHomeTeam());
        assertEquals(away, fixture.getAwayTeam());
        assertFalse(fixture.isPlayed());
    }

    @Test
    void constructorRejectsSameTeamForHomeAndAway() {
        Team team = new FootballTeam("team-1", "Red Hawks");

        assertThrows(IllegalArgumentException.class, () ->
                new Fixture(1, team, team)
        );
    }

    @Test
    void constructorRejectsWeekLessThanOne() {
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");

        assertThrows(IllegalArgumentException.class, () ->
                new Fixture(0, home, away)
        );
    }

    @Test
    void attachPlayedMatchMarksFixtureAsPlayed() {
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");

        Fixture fixture = new Fixture(1, home, away);
        Match match = new FootballMatch(home, away);
        match.setResult(2, 1);

        fixture.attachPlayedMatch(match);

        assertTrue(fixture.isPlayed());
        assertEquals(match, fixture.getPlayedMatch());
    }

    @Test
    void attachPlayedMatchRejectsTeamsThatDoNotMatchFixture() {
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");
        Team other = new FootballTeam("team-3", "Golden Stars");

        Fixture fixture = new Fixture(1, home, away);
        Match wrongMatch = new FootballMatch(home, other);

        assertThrows(IllegalArgumentException.class, () ->
                fixture.attachPlayedMatch(wrongMatch)
        );
    }

    @Test
    void attachPlayedMatchRejectsUnplayedMatch() {
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");

        Fixture fixture = new Fixture(1, home, away);
        Match unplayedMatch = new FootballMatch(home, away);

        assertThrows(IllegalArgumentException.class, () ->
                fixture.attachPlayedMatch(unplayedMatch)
        );
        assertFalse(fixture.isPlayed());
    }
}
