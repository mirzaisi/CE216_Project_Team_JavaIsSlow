package com.playforgemanager.application;

import com.playforgemanager.application.save.DefaultSaveGameRestorationRegistry;
import com.playforgemanager.application.save.LoadGameService;
import com.playforgemanager.application.save.SaveGameFormat;
import com.playforgemanager.application.save.SaveGameService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.infrastructure.JsonSaveGameReader;
import com.playforgemanager.infrastructure.JsonSaveGameWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedArchitecturePersistenceTest {
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Set<Path> ALLOWED_SPORT_WIRING_FILES = Set.of(
            sourcePath("com", "playforgemanager", "application", "DefaultSportRegistry.java"),
            sourcePath("com", "playforgemanager", "application", "DefaultWeekProgressionRegistry.java"),
            sourcePath("com", "playforgemanager", "application", "DefaultMatchProcessingRegistry.java"),
            sourcePath("com", "playforgemanager", "application", "save", "DefaultSaveGameRestorationRegistry.java")
    );

    @TempDir
    private Path tempDir;

    @Test
    void footballAndHandballStartThroughSameInitializationService() {
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(new TestAssets(), 4)))
                .register(new SportRegistration("handball", "Handball", new FakeHandballSportFactory()));
        GameInitializationService service = new GameInitializationService(registry);

        GameSession football = service.startNewSession("football", "Shared Football League");
        GameSession handball = service.startNewSession("handball", "Shared Handball League");

        assertEquals("football", football.getSelectedSportId());
        assertEquals("Football", football.getActiveSport().getName());
        assertEquals(4, football.getCurrentSeason().getLeague().getTeamCount());
        assertFalse(football.getCurrentSeason().getLeague().getFixtures().isEmpty());

        assertEquals("handball", handball.getSelectedSportId());
        assertEquals("Handball", handball.getActiveSport().getName());
        assertEquals(2, handball.getCurrentSeason().getLeague().getTeamCount());
        assertEquals(1, handball.getCurrentSeason().getLeague().getFixtures().size());
        assertEquals(ProgressionState.READY_TO_START, handball.getProgressionState());
    }

    @Test
    void saveLoadRoundTripPreservesSharedSessionState() throws IOException {
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(new TestAssets(), 4)));
        GameSession original = new GameInitializationService(registry)
                .startNewSession("football", "Round Trip League");

        new WeekProgressionService(DefaultWeekProgressionRegistry.create()).advanceOneStep(original);
        Path savePath = tempDir.resolve("shared-round-trip" + SaveGameFormat.FILE_EXTENSION);
        new SaveGameService(new JsonSaveGameWriter()).save(original, savePath);

        GameSession loaded = new LoadGameService(
                new JsonSaveGameReader(),
                registry,
                DefaultSaveGameRestorationRegistry.create()
        ).load(savePath);

        assertEquals(original.getSelectedSportId(), loaded.getSelectedSportId());
        assertEquals(original.getProgressionState(), loaded.getProgressionState());
        assertEquals(original.getControlledTeam().getId(), loaded.getControlledTeam().getId());
        assertEquals(original.getCurrentSeason().getCurrentWeek(), loaded.getCurrentSeason().getCurrentWeek());
        assertEquals(original.getCurrentSeason().isCompleted(), loaded.getCurrentSeason().isCompleted());
        assertEquals(teamIds(original), teamIds(loaded));
        assertEquals(fixtureKeys(original), fixtureKeys(loaded));
        assertEquals(playedFixtureCount(original), playedFixtureCount(loaded));
        assertEquals(injuryTotal(original), injuryTotal(loaded));
        assertEquals(
                original.getControlledTeam().getSelectedLineup() != null,
                loaded.getControlledTeam().getSelectedLineup() != null
        );
        assertEquals(
                original.getControlledTeam().getSelectedTactic() != null,
                loaded.getControlledTeam().getSelectedTactic() != null
        );
        assertEquals(
                original.getControlledTeam().getTrainingPlan() != null,
                loaded.getControlledTeam().getTrainingPlan() != null
        );
    }

    @Test
    void invalidSaveFilesAreRejectedWithoutReturningSession() throws IOException {
        Path invalidJson = tempDir.resolve("broken" + SaveGameFormat.FILE_EXTENSION);
        Files.writeString(invalidJson, "{ definitely not valid json");

        Path incompleteSave = tempDir.resolve("missing-session" + SaveGameFormat.FILE_EXTENSION);
        Files.writeString(incompleteSave, "{\"formatId\":\"playforge-save\",\"formatVersion\":1}");

        LoadGameService loadService = new LoadGameService(
                new JsonSaveGameReader(),
                DefaultSportRegistry.create(new TestAssets()),
                DefaultSaveGameRestorationRegistry.create()
        );

        assertThrows(IllegalArgumentException.class, () -> loadService.load(invalidJson));
        assertThrows(IllegalArgumentException.class, () -> loadService.load(incompleteSave));
    }

    @Test
    void sharedApplicationAndMainLayersDoNotImportConcreteSportsOutsideWiring() throws IOException {
        List<Path> violations;
        try (Stream<Path> sourceFiles = Files.walk(SOURCE_ROOT.resolve(Path.of("com", "playforgemanager")))) {
            violations = sourceFiles
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isSharedOrUiFacingFile)
                    .filter(path -> !ALLOWED_SPORT_WIRING_FILES.contains(path.normalize()))
                    .filter(this::importsConcreteSportPackage)
                    .toList();
        }

        assertTrue(violations.isEmpty(), "Unexpected concrete sport imports: " + violations);
    }

    private boolean isSharedOrUiFacingFile(Path path) {
        Path normalized = path.normalize();
        return normalized.startsWith(SOURCE_ROOT.resolve(Path.of("com", "playforgemanager", "application")).normalize())
                || normalized.startsWith(SOURCE_ROOT.resolve(Path.of("com", "playforgemanager", "main")).normalize());
    }

    private boolean importsConcreteSportPackage(Path path) {
        try {
            String source = Files.readString(path);
            return source.contains("import com.playforgemanager.football.")
                    || source.contains("import com.playforgemanager.handball.");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read source file: " + path, exception);
        }
    }

    private Set<String> teamIds(GameSession session) {
        Set<String> ids = new HashSet<>();
        for (Team team : session.getCurrentSeason().getLeague().getTeams()) {
            ids.add(team.getId());
        }
        return ids;
    }

    private Set<String> fixtureKeys(GameSession session) {
        Set<String> keys = new HashSet<>();
        for (Fixture fixture : session.getCurrentSeason().getLeague().getFixtures()) {
            keys.add(fixture.getWeek() + ":" + fixture.getHomeTeam().getId() + ":" + fixture.getAwayTeam().getId());
        }
        return keys;
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

    private static Path sourcePath(String first, String... more) {
        return SOURCE_ROOT.resolve(Path.of(first, more)).normalize();
    }

    private static class TestAssets implements AssetProvider {
        @Override
        public List<String> getMaleNames() {
            return List.of("Ali", "Mert", "Can", "Efe");
        }

        @Override
        public List<String> getFemaleNames() {
            return List.of("Ece", "Zeynep", "Ada", "Elif");
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

    private static class FakeHandballSportFactory implements SportFactory {
        @Override
        public String getSportName() {
            return "Handball";
        }

        @Override
        public Sport createSport() {
            return new FakeHandballSport();
        }

        @Override
        public League createLeague(String leagueName) {
            FakeHandballLeague league = new FakeHandballLeague(leagueName);
            FakeHandballTeam home = new FakeHandballTeam("handball-team-1", "Handball A");
            FakeHandballTeam away = new FakeHandballTeam("handball-team-2", "Handball B");
            home.addPlayer(new FakeHandballPlayer("handball-player-1", "A Player"));
            away.addPlayer(new FakeHandballPlayer("handball-player-2", "B Player"));
            league.addTeam(home);
            league.addTeam(away);
            return league;
        }

        @Override
        public Season createSeason(League league) {
            return new FakeHandballSeason(league);
        }
    }

    private static class FakeHandballSport implements Sport {
        private final Ruleset ruleset = new FakeHandballRuleset();
        private final Scheduler scheduler = teams -> List.of(new Fixture(1, teams.get(0), teams.get(1)));
        private final StandingsPolicy standingsPolicy = new FakeHandballStandingsPolicy();
        private final MatchEngine matchEngine = (match, ruleset) -> {
        };
        private final InjuryPolicy injuryPolicy = new FakeHandballInjuryPolicy();

        @Override
        public String getName() {
            return "Handball";
        }

        @Override
        public Ruleset getRuleset() {
            return ruleset;
        }

        @Override
        public Scheduler getScheduler() {
            return scheduler;
        }

        @Override
        public StandingsPolicy getStandingsPolicy() {
            return standingsPolicy;
        }

        @Override
        public MatchEngine getMatchEngine() {
            return matchEngine;
        }

        @Override
        public InjuryPolicy getInjuryPolicy() {
            return injuryPolicy;
        }
    }

    private static class FakeHandballLeague extends League {
        FakeHandballLeague(String name) {
            super(name);
        }
    }

    private static class FakeHandballSeason extends Season {
        FakeHandballSeason(League league) {
            super(league);
        }

        @Override
        protected void doAdvanceWeek() {
            markCompleted();
        }
    }

    private static class FakeHandballTeam extends Team {
        FakeHandballTeam(String id, String name) {
            super(id, name);
        }
    }

    private static class FakeHandballPlayer extends Player {
        FakeHandballPlayer(String id, String name) {
            super(id, name);
        }
    }

    private static class FakeHandballRuleset implements Ruleset {
        @Override
        public int getWinPoints() {
            return 2;
        }

        @Override
        public int getDrawPoints() {
            return 1;
        }

        @Override
        public int getLossPoints() {
            return 0;
        }

        @Override
        public int getStartingLineupSize() {
            return 7;
        }

        @Override
        public int getBenchSize() {
            return 7;
        }

        @Override
        public boolean allowsUnlimitedSubstitutions() {
            return true;
        }

        @Override
        public boolean isValidLineup(Lineup lineup) {
            return lineup != null;
        }
    }

    private static class FakeHandballStandingsPolicy implements StandingsPolicy {
        @Override
        public void recordMatch(League league, Match match) {
        }

        @Override
        public List<Team> rankTeams(League league) {
            return league.getTeams();
        }
    }

    private static class FakeHandballInjuryPolicy implements InjuryPolicy {
        @Override
        public void applyPostMatch(Match match) {
        }

        @Override
        public void recoverPlayers(Team team) {
        }
    }
}
