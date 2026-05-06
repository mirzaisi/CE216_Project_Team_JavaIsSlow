package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Team;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonRolloverTest {
    private final FootballSport sport = new FootballSport();
    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void completedSeasonCreatesPlayableNextSeasonWithFreshFixturesAndStandings() {
        FootballSeason completedSeason = completedSeasonWithFourTeams();
        int originalFixtureCount = completedSeason.getLeague().getFixtures().size();

        FootballSeason nextSeason = completedSeason.createNextSeason(sport);

        assertNotSame(completedSeason, nextSeason);
        assertNotSame(completedSeason.getLeague(), nextSeason.getLeague());
        assertFalse(nextSeason.isCompleted());
        assertEquals(1, nextSeason.getCurrentWeek());
        assertEquals(originalFixtureCount, nextSeason.getLeague().getFixtures().size());
        assertTrue(nextSeason.getLeague().getFixtures().stream().noneMatch(Fixture::isPlayed));
        assertEquals(4, nextSeason.getStandings().size());
        assertTrue(nextSeason.getStandings().stream().allMatch(row ->
                row.getPlayed() == 0
                        && row.getWins() == 0
                        && row.getDraws() == 0
                        && row.getLosses() == 0
                        && row.getGoalsFor() == 0
                        && row.getGoalsAgainst() == 0
                        && row.getPoints() == 0
        ));

        assertDoesNotThrow(() -> nextSeason.playCurrentWeek(sport, FootballMatch::new));
    }

    @Test
    void rolloverCarriesTeamsPlayersCoachesTacticsAndTrainingPlansButResetsCompetitionState() {
        FootballSeason completedSeason = completedSeasonWithFourTeams();
        FootballTeam originalTeam = (FootballTeam) completedSeason.getLeague().getTeams().get(0);
        FootballTactic originalTactic = originalTeam.getSelectedFootballTactic();
        FootballTrainingPlan originalTrainingPlan = originalTeam.getSelectedFootballTrainingPlan();
        FootballCoach originalCoach = originalTeam.getCoaches().get(0);
        FootballPlayer originalPlayer = originalTeam.getFootballPlayers().get(0);

        FootballSeason nextSeason = completedSeason.createNextSeason(sport);
        FootballTeam carriedTeam = (FootballTeam) nextSeason.getLeague().getTeams().get(0);

        assertSame(originalTeam, carriedTeam);
        assertSame(originalTactic, carriedTeam.getSelectedFootballTactic());
        assertSame(originalTrainingPlan, carriedTeam.getSelectedFootballTrainingPlan());
        assertSame(originalCoach, carriedTeam.getCoaches().get(0));
        assertSame(originalPlayer, carriedTeam.getFootballPlayers().get(0));
        assertNull(carriedTeam.getSelectedFootballLineup());
        assertTrue(nextSeason.getLeague().getFixtures().stream().noneMatch(Fixture::isPlayed));
    }

    @Test
    void rolloverClearsTransientTrainingEffectsInjuriesAvailabilityBlocksAndOldLineups() {
        FootballLeague league = scheduledLeagueWithFourTeams();
        FootballSeason season = new FootballSeason(league);
        FootballTeam team = (FootballTeam) league.getTeams().get(0);
        FootballPlayer injuredPlayer = team.getFootballPlayers().get(0);
        FootballPlayer unavailablePlayer = team.getFootballPlayers().get(1);
        FootballPlayer boostedPlayer = team.getFootballPlayers().get(2);

        injuredPlayer.injureForMatches(3);
        unavailablePlayer.setAvailable(false);
        boostedPlayer.applyWeeklyTrainingEffect(new FootballTrainingEffect(1, 1, 1, 1, 1, false));
        team.assignLineup(ruleset.buildLineup(team.getAvailablePlayers()), ruleset);
        season.restoreProgress(league.getLastScheduledWeek(), true);

        FootballSeason nextSeason = season.createNextSeason(sport);
        FootballTeam carriedTeam = (FootballTeam) nextSeason.getLeague().getTeams().get(0);

        assertNull(carriedTeam.getSelectedFootballLineup());
        assertEquals(0, injuredPlayer.getInjuryMatchesRemaining());
        assertTrue(injuredPlayer.isAvailable());
        assertEquals(0, unavailablePlayer.getInjuryMatchesRemaining());
        assertTrue(unavailablePlayer.isAvailable());
        assertEquals(FootballTrainingEffect.none(), boostedPlayer.getWeeklyTrainingEffect());
        assertDoesNotThrow(() -> nextSeason.playCurrentWeek(sport, FootballMatch::new));
    }

    @Test
    void rolloverServiceCreatesNewSessionWithSameSportAndControlledTeamIdentity() {
        FootballSeason completedSeason = completedSeasonWithFourTeams();
        Team controlledTeam = completedSeason.getLeague().getTeams().get(1);
        GameSession completedSession = new GameSession(
                sport,
                completedSeason,
                controlledTeam,
                ProgressionState.COMPLETED,
                "football"
        );

        GameSession nextSession = new FootballSeasonRolloverService().rollOver(completedSession);

        assertSame(sport, nextSession.getActiveSport());
        assertTrue(nextSession.getCurrentSeason() instanceof FootballSeason);
        assertEquals(ProgressionState.READY_TO_START, nextSession.getProgressionState());
        assertEquals("football", nextSession.getSelectedSportId());
        assertEquals(controlledTeam.getId(), nextSession.getControlledTeam().getId());

        FootballSeason nextSeason = (FootballSeason) nextSession.getCurrentSeason();
        assertDoesNotThrow(() -> nextSeason.playCurrentWeek(sport, FootballMatch::new));
    }

    @Test
    void cannotCreateNextSeasonBeforeCurrentSeasonIsCompleted() {
        FootballSeason activeSeason = new FootballSeason(scheduledLeagueWithFourTeams());

        IllegalStateException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> activeSeason.createNextSeason(sport)
        );

        assertTrue(error.getMessage().contains("completed"));
    }

    private FootballSeason completedSeasonWithFourTeams() {
        FootballSeason season = new FootballSeason(scheduledLeagueWithFourTeams());
        while (!season.isCompleted()) {
            season.playCurrentWeek(sport, FootballMatch::new);
        }
        return season;
    }

    private FootballLeague scheduledLeagueWithFourTeams() {
        FootballLeague league = new FootballLeague("Rollover Test League");
        for (int i = 0; i < 4; i++) {
            league.addTeam(createTeam(i));
        }
        league.addFixtures(new RoundRobinFootballScheduler().generateFixtures(league.getTeams()));
        return league;
    }

    private FootballTeam createTeam(int teamIndex) {
        FootballTeam team = new FootballTeam("team-" + teamIndex, "Team " + teamIndex);
        for (int i = 0; i < 18; i++) {
            FootballPosition position = positionFor(i);
            team.addPlayer(new FootballPlayer(
                    "player-" + teamIndex + "-" + i,
                    "Player " + teamIndex + "-" + i,
                    position,
                    profileFor(position, teamIndex, i)
            ));
        }
        team.addCoach(new FootballCoach(
                "coach-" + teamIndex,
                "Coach " + teamIndex,
                "Head Coach",
                "General Management",
                80
        ));
        team.assignTactic(new FootballTactic(
                "Balanced " + teamIndex,
                "4-3-3",
                FootballTactic.Mentality.BALANCED,
                55,
                55
        ));
        team.assignTrainingPlan(new FootballTrainingPlan("Balanced Development", 55, 50, 50, true));
        team.assignLineup(ruleset.buildLineup(team.getAvailablePlayers()), ruleset);
        return team;
    }

    private FootballPosition positionFor(int squadIndex) {
        if (squadIndex == 0 || squadIndex == 17) {
            return FootballPosition.GOALKEEPER;
        }
        if (squadIndex < 7) {
            return FootballPosition.DEFENDER;
        }
        if (squadIndex < 13) {
            return FootballPosition.MIDFIELDER;
        }
        return FootballPosition.FORWARD;
    }

    private FootballAttributeProfile profileFor(FootballPosition position, int teamIndex, int playerIndex) {
        int modifier = (teamIndex * 4 + playerIndex) % 10;
        return switch (position) {
            case GOALKEEPER -> new FootballAttributeProfile(35 + modifier, 82 + modifier, 70, 62, 55);
            case DEFENDER -> new FootballAttributeProfile(55 + modifier, 78 + modifier, 73, 65, 64);
            case MIDFIELDER -> new FootballAttributeProfile(68 + modifier, 68, 76 + modifier, 80 + modifier, 72);
            case FORWARD -> new FootballAttributeProfile(82 + modifier, 50, 74, 70, 80 + modifier);
        };
    }
}
