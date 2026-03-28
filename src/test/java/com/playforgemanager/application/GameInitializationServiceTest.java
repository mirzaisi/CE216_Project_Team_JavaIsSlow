package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.football.FootballSportFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameInitializationServiceTest {

    @Test
    void startNewSessionCreatesAttachedWorldThroughAbstractions() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportFactory sportFactory = new FootballSportFactory(assetProvider, 4);
        GameInitializationService service = new GameInitializationService(sportFactory);

        GameSession session = service.startNewSession("Starter League");

        assertNotNull(session.getActiveSport());
        assertNotNull(session.getCurrentSeason());
        assertNotNull(session.getControlledTeam());
        assertEquals("Football", session.getActiveSport().getName());
        assertEquals("Starter League", session.getCurrentSeason().getLeague().getName());
        assertEquals("Red Hawks", session.getControlledTeam().getName());
        assertEquals(4, session.getCurrentSeason().getLeague().getTeamCount());
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());
    }

    private static class FakeAssetProvider implements AssetProvider {
        @Override
        public List<String> getMaleNames() {
            return List.of("Ali", "Mert");
        }

        @Override
        public List<String> getFemaleNames() {
            return List.of("Ece", "Zeynep");
        }

        @Override
        public List<String> getTeamNames() {
            return List.of("Red Hawks", "Blue Wolves", "Golden Stars", "Iron Lions");
        }

        @Override
        public List<String> getLogoReferences() {
            return List.of("logo1", "logo2", "logo3", "logo4");
        }
    }
}