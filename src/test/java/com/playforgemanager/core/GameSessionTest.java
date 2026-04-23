package com.playforgemanager.core;

import com.playforgemanager.football.FootballLeague;
import com.playforgemanager.football.FootballSeason;
import com.playforgemanager.football.FootballSport;
import com.playforgemanager.football.FootballTeam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSessionTest {

    @Test
    void constructorStoresSportSeasonTeamAndInitialProgressionState() {
        Sport sport = new FootballSport();
        League league = new FootballLeague("Test League");
        Team team = new FootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new FootballSeason(league);

        GameSession session = new GameSession(
                sport,
                season,
                team,
                ProgressionState.READY_TO_START
        );

        assertEquals("Football", session.getActiveSport().getName());
        assertEquals("football", session.getSelectedSportId());
        assertEquals(season, session.getCurrentSeason());
        assertEquals(team, session.getControlledTeam());
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());
    }

    @Test
    void markInProgressChangesStateToInProgress() {
        Sport sport = new FootballSport();
        League league = new FootballLeague("Test League");
        Team team = new FootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new FootballSeason(league);

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
        League league = new FootballLeague("Test League");
        Team team = new FootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new FootballSeason(league);

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
        League league = new FootballLeague("Test League");
        Team team = new FootballTeam("team-1", "Red Hawks");
        league.addTeam(team);
        Season season = new FootballSeason(league);

        assertThrows(NullPointerException.class, () ->
                new GameSession(null, season, team, ProgressionState.READY_TO_START)
        );
    }
}
