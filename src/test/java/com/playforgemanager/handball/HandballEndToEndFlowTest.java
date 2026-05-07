package com.playforgemanager.handball;

import com.playforgemanager.application.DefaultMatchProcessingRegistry;
import com.playforgemanager.application.DefaultSportRegistry;
import com.playforgemanager.application.DefaultWeekProgressionRegistry;
import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.MatchProcessingResult;
import com.playforgemanager.application.MatchProcessingService;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.application.WeekProgressionResult;
import com.playforgemanager.application.WeekProgressionService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballEndToEndFlowTest {

    @Test
    void defaultRegistryStartsRealHandballSession() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportRegistry registry = DefaultSportRegistry.create(assetProvider);
        GameInitializationService initializationService = new GameInitializationService(registry);

        GameSession session = initializationService.startNewSession("handball", "Handball League");

        assertEquals("handball", session.getSelectedSportId());
        assertInstanceOf(HandballSport.class, session.getActiveSport());
        assertInstanceOf(HandballSeason.class, session.getCurrentSeason());
        assertInstanceOf(HandballTeam.class, session.getControlledTeam());
        assertEquals(4, session.getCurrentSeason().getLeague().getTeamCount());
        assertEquals(12, session.getCurrentSeason().getLeague().getFixtures().size());
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());

        HandballTeam controlledTeam = (HandballTeam) session.getControlledTeam();
        assertNotNull(controlledTeam.getSelectedHandballLineup());
        assertNotNull(controlledTeam.getSelectedHandballTactic());
        assertNotNull(controlledTeam.getSelectedHandballTrainingPlan());
    }

    @Test
    void handballSessionCanPlayControlledMatchAndCompleteSeasonThroughSharedServices() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportRegistry registry = DefaultSportRegistry.create(assetProvider);
        GameInitializationService initializationService = new GameInitializationService(registry);
        GameSession session = initializationService.startNewSession("handball", "Handball League");

        HandballTeam controlledTeam = (HandballTeam) session.getControlledTeam();
        MatchProcessingService matchProcessingService =
                new MatchProcessingService(DefaultMatchProcessingRegistry.create());

        MatchProcessingResult controlledMatch = matchProcessingService.playControlledMatch(
                session,
                controlledTeam.getSelectedHandballLineup(),
                controlledTeam.getSelectedHandballTactic()
        );

        assertEquals("handball", controlledMatch.getSportId());
        assertTrue(controlledMatch.getFixture().isPlayed());
        assertEquals(ProgressionState.IN_PROGRESS, controlledMatch.getProgressionState());
        assertTrue(controlledMatch.getControlledTeamScore() >= 0);
        assertTrue(controlledMatch.getOpponentScore() >= 0);

        WeekProgressionService progressionService =
                new WeekProgressionService(DefaultWeekProgressionRegistry.create());

        WeekProgressionResult firstWeekSummary = progressionService.advanceOneStep(session);

        assertEquals("handball", firstWeekSummary.getSportId());
        assertEquals(1, firstWeekSummary.getPlayedWeek());
        assertEquals(2, firstWeekSummary.getCurrentWeekAfterProgression());
        assertFalse(firstWeekSummary.isSeasonCompleted());

        while (!session.getCurrentSeason().isCompleted()) {
            progressionService.advanceOneStep(session);
        }

        HandballSeason completedSeason = (HandballSeason) session.getCurrentSeason();
        assertTrue(completedSeason.isCompleted());
        assertEquals(ProgressionState.COMPLETED, session.getProgressionState());
        assertTrue(completedSeason.getLeague().getFixtures().stream().allMatch(fixture -> fixture.isPlayed()));
        assertEquals(4, completedSeason.getStandings().size());
    }
}
