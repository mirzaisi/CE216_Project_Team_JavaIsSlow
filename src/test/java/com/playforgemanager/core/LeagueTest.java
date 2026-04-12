package com.playforgemanager.core;

import com.playforgemanager.football.FootballLeague;
import com.playforgemanager.football.FootballTeam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeagueTest {

    @Test
    void addTeamRejectsDuplicateTeamIds() {
        League league = new FootballLeague("Test League");
        league.addTeam(new FootballTeam("team-1", "Red Hawks"));

        assertThrows(IllegalArgumentException.class, () ->
                league.addTeam(new FootballTeam("team-1", "Blue Wolves"))
        );
    }

    @Test
    void addFixtureRejectsTeamsOutsideLeague() {
        League league = new FootballLeague("Test League");
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");
        Team outsider = new FootballTeam("team-3", "Golden Stars");
        league.addTeam(home);
        league.addTeam(away);

        Fixture fixture = new Fixture(1, home, outsider);

        assertThrows(IllegalArgumentException.class, () -> league.addFixture(fixture));
    }

    @Test
    void addFixtureAcceptsMemberTeams() {
        League league = new FootballLeague("Test League");
        Team home = new FootballTeam("team-1", "Red Hawks");
        Team away = new FootballTeam("team-2", "Blue Wolves");
        league.addTeam(home);
        league.addTeam(away);

        league.addFixture(new Fixture(1, home, away));

        assertEquals(1, league.getFixtures().size());
    }
}
