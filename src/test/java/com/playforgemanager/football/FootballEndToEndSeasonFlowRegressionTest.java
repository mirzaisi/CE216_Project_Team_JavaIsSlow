package com.playforgemanager.football;

import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameDocumentMapper;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballEndToEndSeasonFlowRegressionTest {
    @Test
    void fullFootballSeasonFlowCompletesWithResultsStandingsSaveLoadAndRollover() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason season = FootballRegressionTestSupport.playableSeason(4);
        GameSession session = FootballRegressionTestSupport.sessionWithSeason(sport, season);

        int fixtureCount = season.getLeague().getFixtures().size();
        FootballRegressionTestSupport.playUntilCompleted(season, sport);

        assertTrue(season.isCompleted());
        assertEquals(fixtureCount, FootballRegressionTestSupport.playedFixtureCount(season));
        assertEquals(4, season.getStandings().size());
        assertTrue(season.getStandings().stream().allMatch(row -> row.getPlayed() > 0));
        assertTrue(season.getStandings().stream().mapToInt(FootballStandingRow::getPoints).sum() > 0);

        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(session);
        GameSession restoredSession = new FootballSaveGameRestorer().restore(
                document,
                FootballRegressionTestSupport.footballRegistration(4)
        );
        FootballSeason restoredSeason = (FootballSeason) restoredSession.getCurrentSeason();

        assertTrue(restoredSeason.isCompleted());
        assertEquals(fixtureCount, restoredSeason.getLeague().getFixtures().size());
        assertEquals(fixtureCount, FootballRegressionTestSupport.playedFixtureCount(restoredSeason));
        assertEquals(season.getStandings().get(0).getPoints(), restoredSeason.getStandings().get(0).getPoints());

        FootballSeason nextSeason = restoredSeason.createNextSeason((FootballSport) restoredSession.getActiveSport());
        assertEquals(1, nextSeason.getCurrentWeek());
        assertTrue(FootballRegressionTestSupport.noFixturePlayed(nextSeason));
        assertDoesNotThrow(() -> nextSeason.playCurrentWeek((FootballSport) restoredSession.getActiveSport(), FootballMatch::new));
    }

    @Test
    void weeklyFlowAppliesTrainingBeforeSimulationAndLeavesLineupsValidAfterInjuryHandling() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason season = FootballRegressionTestSupport.playableSeason(4);
        FootballTeam firstTeam = (FootballTeam) season.getLeague().getTeams().get(0);
        FootballPlayer trackedForward = (FootballPlayer) firstTeam.getPlayersByPosition(FootballPosition.FORWARD).get(0);

        assertEquals(FootballTrainingEffect.none(), trackedForward.getWeeklyTrainingEffect());

        season.playCurrentWeek(sport, FootballMatch::new);

        assertTrue(FootballRegressionTestSupport.anyFixturePlayed(season));
        assertTrue(trackedForward.getWeeklyTrainingEffect().attackDelta() > 0);
        assertTrue(firstTeam.getSelectedFootballLineup() == null
                || sport.getFootballRuleset().isValidLineup(firstTeam.getSelectedFootballLineup()));
    }
}
