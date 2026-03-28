package com.playforgemanager.core;

import com.playforgemanager.football.BootstrapFootballLeague;
import com.playforgemanager.football.BootstrapFootballSeason;
import com.playforgemanager.football.BootstrapFootballTeam;
import com.playforgemanager.football.FootballSport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSessionTest {

    @Test
    void constructorStoresSportSeasonTeamAndInitialProgressionState() {
        Sport sport = new FootballSport();
        League league = new BootstrapFootballLeague("Test League");
        Team team = new BootstrapFootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new BootstrapFootballSeason(league);

        GameSession session = new GameSession(
                sport,
                season,
                team,
                ProgressionState.READY_TO_START
        );

        assertEquals("Football", session.getActiveSport().getName());
        assertEquals(season, session.getCurrentSeason());
        assertEquals(team, session.getControlledTeam());
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());
    }

    @Test
    void markInProgressChangesStateToInProgress() {
        Sport sport = new FootballSport();
        League league = new BootstrapFootballLeague("Test League");
        Team team = new BootstrapFootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new BootstrapFootballSeason(league);

        GameSession session = new GameSession(
                sport,
                season,
                team,
                ProgressionState.READY_TO_START
        );

        session.markInProgress();

        assertEquals(ProgressionState.IN_PROGRESS, session.getProgressionState());
    }

    @Test
    void markCompletedChangesStateToCompleted() {
        Sport sport = new FootballSport();
        League league = new BootstrapFootballLeague("Test League");
        Team team = new BootstrapFootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new BootstrapFootballSeason(league);

        GameSession session = new GameSession(
                sport,
                season,
                team,
                ProgressionState.READY_TO_START
        );

        session.markCompleted();

        assertEquals(ProgressionState.COMPLETED, session.getProgressionState());
    }

    @Test
    void constructorRejectsNullActiveSport() {
        League league = new BootstrapFootballLeague("Test League");
        Team team = new BootstrapFootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new BootstrapFootballSeason(league);

        assertThrows(NullPointerException.class, () ->
                new GameSession(null, season, team, ProgressionState.READY_TO_START)
        );
    }
}