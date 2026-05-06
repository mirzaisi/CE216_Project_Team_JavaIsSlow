package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonRolloverRegressionTest {
    @Test
    void completedSeasonRollsOverToFreshPlayableSeason() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason completedSeason = FootballRegressionTestSupport.playableSeason(4);
        FootballRegressionTestSupport.playUntilCompleted(completedSeason, sport);

        int completedFixtureCount = completedSeason.getLeague().getFixtures().size();
        FootballSeason nextSeason = completedSeason.createNextSeason(sport);

        assertFalse(nextSeason.isCompleted());
        assertEquals(1, nextSeason.getCurrentWeek());
        assertEquals(completedFixtureCount, nextSeason.getLeague().getFixtures().size());
        assertTrue(FootballRegressionTestSupport.noFixturePlayed(nextSeason));
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
    void rolloverClearsOnlyTransientFootballState() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason completedSeason = FootballRegressionTestSupport.playableSeason(4);
        FootballTeam team = (FootballTeam) completedSeason.getLeague().getTeams().get(0);
        FootballTactic tactic = team.getSelectedFootballTactic();
        FootballTrainingPlan trainingPlan = team.getSelectedFootballTrainingPlan();
        FootballCoach coach = (FootballCoach) team.getCoaches().get(0);
        FootballPlayer boostedPlayer = (FootballPlayer) team.getFootballPlayers().get(0);

        boostedPlayer.applyWeeklyTrainingEffect(new FootballTrainingEffect(2, 1, 1, 1, 1, false));
        boostedPlayer.injureForMatches(2);
        completedSeason.restoreProgress(FootballRegressionTestSupport.lastScheduledWeek(completedSeason), true);

        FootballSeason nextSeason = completedSeason.createNextSeason(sport);
        FootballTeam carriedTeam = (FootballTeam) nextSeason.getLeague().getTeams().get(0);

        assertEquals(tactic, carriedTeam.getSelectedFootballTactic());
        assertEquals(trainingPlan, carriedTeam.getSelectedFootballTrainingPlan());
        assertEquals(coach, carriedTeam.getCoaches().get(0));
        assertNull(carriedTeam.getSelectedFootballLineup());
        assertEquals(FootballTrainingEffect.none(), boostedPlayer.getWeeklyTrainingEffect());
        assertEquals(0, boostedPlayer.getInjuryMatchesRemaining());
        assertTrue(boostedPlayer.isAvailable());
    }
}
