package com.playforgemanager.application.setup;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.handball.HandballSportFactory;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamSetupServiceTest {

    @Test
    void footballViewExposesStartersBenchTacticsAndValidation() {
        TeamSetupService service = new TeamSetupService(DefaultTeamSetupRegistry.create());
        GameSession session = newSession("football");

        TeamSetupView view = service.buildView(session);

        assertEquals("football", view.sportId());
        assertEquals(11, view.requiredStarters());
        assertEquals(7, view.benchLimit());
        assertEquals(view.requiredStarters(), view.startingLineup().size());
        assertTrue(view.lineupValid(), "Auto-generated lineup should be valid");
        assertNotNull(view.selectedTacticName());
        assertFalse(view.tacticOptions().isEmpty());
    }

    @Test
    void handballViewExposesStartersBenchTacticsAndValidation() {
        TeamSetupService service = new TeamSetupService(DefaultTeamSetupRegistry.create());
        GameSession session = newSession("handball");

        TeamSetupView view = service.buildView(session);

        assertEquals("handball", view.sportId());
        assertEquals(7, view.requiredStarters());
        assertEquals(9, view.benchLimit());
        assertEquals(view.requiredStarters(), view.startingLineup().size());
        assertTrue(view.lineupValid());
        assertNotNull(view.selectedTacticName());
        assertFalse(view.tacticOptions().isEmpty());
    }

    @Test
    void applyTacticChangesSelectedTacticName() {
        TeamSetupService service = new TeamSetupService(DefaultTeamSetupRegistry.create());
        GameSession session = newSession("football");
        TeamSetupView before = service.buildView(session);
        TacticOptionView other = before.tacticOptions().stream()
                .filter(option -> !option.id().equals(before.selectedTacticId()))
                .findFirst()
                .orElseThrow();

        service.applyTactic(session, other.id());

        TeamSetupView after = service.buildView(session);
        assertEquals(other.id(), after.selectedTacticId());
        assertNotEquals(before.selectedTacticName(), after.selectedTacticName());
    }

    @Test
    void autoPickLineupKeepsLineupValid() {
        TeamSetupService service = new TeamSetupService(DefaultTeamSetupRegistry.create());
        GameSession session = newSession("handball");

        service.autoPickLineup(session);

        TeamSetupView view = service.buildView(session);
        assertTrue(view.lineupValid());
        assertNotNull(service.currentLineup(session));
        assertNotNull(service.currentTactic(session));
    }

    @Test
    void unknownTacticIdIsRejected() {
        TeamSetupService service = new TeamSetupService(DefaultTeamSetupRegistry.create());
        GameSession session = newSession("football");

        assertThrows(IllegalArgumentException.class, () -> service.applyTactic(session, "no-such-tactic"));
    }

    private GameSession newSession(String sportId) {
        AssetProvider assets = new InMemoryAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assets, 4)))
                .register(new SportRegistration("handball", "Handball", new HandballSportFactory(assets, 4)));
        return new GameInitializationService(registry).startNewSession(sportId, "Test League");
    }
}
