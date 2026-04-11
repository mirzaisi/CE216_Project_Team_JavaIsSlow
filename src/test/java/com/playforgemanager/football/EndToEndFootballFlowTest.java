package com.playforgemanager.football;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndToEndFootballFlowTest {

    @Test
    void fullFlowProducesPlayedFixturesAndConsistentStandings() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportFactory sportFactory = new FootballSportFactory(assetProvider, 4);
        GameInitializationService initializationService = new GameInitializationService(sportFactory);

        GameSession session = initializationService.startNewSession("Integration League");
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());
        assertEquals("Football", session.getActiveSport().getName());
        assertEquals(4, session.getCurrentSeason().getLeague().getTeamCount());

        FootballSeason season = (FootballSeason) session.getCurrentSeason();
        assertFalse(season.getLeague().getFixtures().isEmpty());

        while (!season.isCompleted()) {
            season.playCurrentWeek(session.getActiveSport(), FootballMatch::new);
        }

        session.markCompleted();
        assertEquals(ProgressionState.COMPLETED, session.getProgressionState());

        List<Fixture> fixtures = season.getLeague().getFixtures();
        long playedFixtures = fixtures.stream().filter(Fixture::isPlayed).count();
        assertEquals(fixtures.size(), playedFixtures);

        // every played fixture contributes a row to each side's standings
        int totalPlayed = season.getStandings().stream()
                .mapToInt(FootballStandingRow::getPlayed)
                .sum();
        assertEquals(fixtures.size() * 2, totalPlayed);

        // GF across the league must equal GA across the league
        int totalGoalsFor = season.getStandings().stream()
                .mapToInt(FootballStandingRow::getGoalsFor)
                .sum();
        int totalGoalsAgainst = season.getStandings().stream()
                .mapToInt(FootballStandingRow::getGoalsAgainst)
                .sum();
        assertEquals(totalGoalsFor, totalGoalsAgainst);

        int totalWins = season.getStandings().stream().mapToInt(FootballStandingRow::getWins).sum();
        int totalDraws = season.getStandings().stream().mapToInt(FootballStandingRow::getDraws).sum();
        int totalLosses = season.getStandings().stream().mapToInt(FootballStandingRow::getLosses).sum();

        assertEquals(totalWins, totalLosses);
        assertEquals(0, totalDraws % 2);

        int drawFixtures = totalDraws / 2;
        assertEquals(fixtures.size(), totalWins + drawFixtures);

        // 3 points per win + 1 per team per draw (draws counted twice across teams)
        int totalPoints = season.getStandings().stream()
                .mapToInt(FootballStandingRow::getPoints)
                .sum();
        assertEquals(3 * totalWins + totalDraws, totalPoints);

        assertNotNull(session.getControlledTeam());
        assertTrue(session.getCurrentSeason().isCompleted());
    }
}
