package com.playforgemanager.application.save;

import com.playforgemanager.application.DefaultSportRegistry;
import com.playforgemanager.application.DefaultWeekProgressionRegistry;
import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.application.WeekProgressionService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.football.FootballSeason;
import com.playforgemanager.football.FootballStandingRow;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import com.playforgemanager.infrastructure.JsonSaveGameReader;
import com.playforgemanager.infrastructure.JsonSaveGameWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadGameServiceTest {
    @TempDir
    private Path tempDir;

    @Test
    void loadRestoresPlayableFootballSessionFromSavedFile() throws IOException {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportRegistry sportRegistry = DefaultSportRegistry.create(assetProvider);
        GameSession original = new GameInitializationService(sportRegistry)
                .startNewSession("football", "Loaded League");

        WeekProgressionService progressionService =
                new WeekProgressionService(DefaultWeekProgressionRegistry.create());
        progressionService.advanceOneStep(original);

        Path savePath = tempDir.resolve("round-trip" + SaveGameFormat.FILE_EXTENSION);
        new SaveGameService(new JsonSaveGameWriter()).save(original, savePath);

        GameSession loaded = new LoadGameService(
                new JsonSaveGameReader(),
                sportRegistry,
                DefaultSaveGameRestorationRegistry.create()
        ).load(savePath);

        assertEquals("football", loaded.getSelectedSportId());
        assertEquals(ProgressionState.IN_PROGRESS, loaded.getProgressionState());
        assertEquals(original.getControlledTeam().getId(), loaded.getControlledTeam().getId());
        assertEquals(original.getCurrentSeason().getCurrentWeek(), loaded.getCurrentSeason().getCurrentWeek());
        assertEquals(original.getCurrentSeason().getLeague().getFixtures().size(),
                loaded.getCurrentSeason().getLeague().getFixtures().size());
        assertEquals(playedFixtureCount(original), playedFixtureCount(loaded));
        assertEquals(totalInjuryCount(original), totalInjuryCount(loaded));

        FootballSeason loadedSeason = assertInstanceOf(FootballSeason.class, loaded.getCurrentSeason());
        List<FootballStandingRow> loadedStandings = loadedSeason.getStandings();
        assertEquals(4, loadedStandings.size());
        assertTrue(loadedStandings.stream().anyMatch(row -> row.getPlayed() > 0));

        progressionService.advanceOneStep(loaded);
        assertEquals(3, loaded.getCurrentSeason().getCurrentWeek());
        assertEquals(ProgressionState.IN_PROGRESS, loaded.getProgressionState());
    }

    @Test
    void loadRejectsUnsupportedSaveVersion() throws IOException {
        Path savePath = writeSavedFootballSession();
        String json = Files.readString(savePath).replace("\"formatVersion\": 1", "\"formatVersion\": 999");
        Files.writeString(savePath, json);

        LoadGameService loadService = loadService();

        assertThrows(IllegalArgumentException.class, () -> loadService.load(savePath));
    }

    @Test
    void loadRejectsIncompleteSaveFile() throws IOException {
        Path savePath = tempDir.resolve("incomplete" + SaveGameFormat.FILE_EXTENSION);
        Files.writeString(savePath, "{\"formatId\":\"playforge-save\",\"formatVersion\":1,\"session\":{}}");

        LoadGameService loadService = loadService();

        assertThrows(IllegalArgumentException.class, () -> loadService.load(savePath));
    }

    @Test
    void loadRejectsUnknownSportSave() throws IOException {
        Path savePath = writeSavedFootballSession();
        String json = Files.readString(savePath).replace("\"sportId\": \"football\"", "\"sportId\": \"basketball\"");
        Files.writeString(savePath, json);

        LoadGameService loadService = loadService();

        assertThrows(IllegalArgumentException.class, () -> loadService.load(savePath));
    }

    private Path writeSavedFootballSession() throws IOException {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportRegistry sportRegistry = DefaultSportRegistry.create(assetProvider);
        GameSession session = new GameInitializationService(sportRegistry)
                .startNewSession("football", "Saved League");
        Path savePath = tempDir.resolve("saved" + SaveGameFormat.FILE_EXTENSION);

        new SaveGameService(new JsonSaveGameWriter()).save(session, savePath);
        return savePath;
    }

    private LoadGameService loadService() {
        return new LoadGameService(
                new JsonSaveGameReader(),
                DefaultSportRegistry.create(new InMemoryAssetProvider()),
                DefaultSaveGameRestorationRegistry.create()
        );
    }

    private long playedFixtureCount(GameSession session) {
        return session.getCurrentSeason().getLeague().getFixtures().stream()
                .filter(Fixture::isPlayed)
                .count();
    }

    private int totalInjuryCount(GameSession session) {
        return session.getCurrentSeason().getLeague().getTeams().stream()
                .flatMap(team -> team.getRoster().stream())
                .mapToInt(Player::getInjuryMatchesRemaining)
                .sum();
    }
}
