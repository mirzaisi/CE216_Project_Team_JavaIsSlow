package com.playforgemanager.handball;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.application.WeekProgressionService;
import com.playforgemanager.application.DefaultWeekProgressionRegistry;
import com.playforgemanager.application.save.DefaultSaveGameRestorationRegistry;
import com.playforgemanager.application.save.LoadGameService;
import com.playforgemanager.application.save.SaveGameFormat;
import com.playforgemanager.application.save.SaveGameService;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Player;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import com.playforgemanager.infrastructure.JsonSaveGameReader;
import com.playforgemanager.infrastructure.JsonSaveGameWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballSaveGameRestorerTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripPreservesLeagueWeekAndTeamState() throws IOException {
        SportRegistry registry = newRegistry();
        GameSession original = new GameInitializationService(registry)
                .startNewSession("handball", "Round Trip Handball");
        new WeekProgressionService(DefaultWeekProgressionRegistry.create()).advanceOneStep(original);

        Path savePath = tempDir.resolve("handball-roundtrip" + SaveGameFormat.FILE_EXTENSION);
        new SaveGameService(new JsonSaveGameWriter()).save(original, savePath);

        GameSession loaded = new LoadGameService(
                new JsonSaveGameReader(),
                registry,
                DefaultSaveGameRestorationRegistry.create()
        ).load(savePath);

        assertEquals(original.getSelectedSportId(), loaded.getSelectedSportId());
        assertEquals(original.getProgressionState(), loaded.getProgressionState());
        assertEquals(original.getControlledTeam().getId(), loaded.getControlledTeam().getId());
        assertEquals(
                original.getCurrentSeason().getCurrentWeek(),
                loaded.getCurrentSeason().getCurrentWeek()
        );
        assertEquals(
                original.getCurrentSeason().getLeague().getTeams().size(),
                loaded.getCurrentSeason().getLeague().getTeams().size()
        );
        assertEquals(
                playedFixtureCount(original),
                playedFixtureCount(loaded)
        );
        assertEquals(
                injuryTotal(original),
                injuryTotal(loaded)
        );
        assertEquals(
                original.getControlledTeam().getSelectedLineup() != null,
                loaded.getControlledTeam().getSelectedLineup() != null
        );
        assertEquals(
                original.getControlledTeam().getSelectedTactic() != null,
                loaded.getControlledTeam().getSelectedTactic() != null
        );
        assertTrue(loaded.getActiveSport() instanceof HandballSport);
    }

    private SportRegistry newRegistry() {
        InMemoryAssetProvider assets = new InMemoryAssetProvider();
        return new SportRegistry()
                .register(new SportRegistration("handball", "Handball", new HandballSportFactory(assets, 4)));
    }

    private long playedFixtureCount(GameSession session) {
        return session.getCurrentSeason().getLeague().getFixtures().stream()
                .filter(Fixture::isPlayed)
                .count();
    }

    private int injuryTotal(GameSession session) {
        return session.getCurrentSeason().getLeague().getTeams().stream()
                .flatMap(team -> team.getRoster().stream())
                .mapToInt(Player::getInjuryMatchesRemaining)
                .sum();
    }
}
