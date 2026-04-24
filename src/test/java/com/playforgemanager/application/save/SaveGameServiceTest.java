package com.playforgemanager.application.save;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.SportRegistry;
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
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;
import com.playforgemanager.football.FootballLeague;
import com.playforgemanager.football.FootballMatch;
import com.playforgemanager.football.FootballPlayer;
import com.playforgemanager.football.FootballPosition;
import com.playforgemanager.football.FootballSeason;
import com.playforgemanager.football.FootballSport;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.football.FootballTeam;
import com.playforgemanager.infrastructure.JsonSaveGameWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveGameServiceTest {
    @TempDir
    private Path tempDir;

    @Test
    void saveWritesFootballSessionAsVersionedJsonFile() throws IOException {
        SaveGameService service = new SaveGameService(new JsonSaveGameWriter());
        GameSession session = newFootballSession();
        Path savePath = tempDir.resolve("football-save" + SaveGameFormat.FILE_EXTENSION);

        SaveGameResult result = service.save(session, savePath);

        String json = Files.readString(savePath);
        assertEquals(savePath.toAbsolutePath().normalize(), result.savePath());
        assertEquals(SaveGameFormat.FORMAT_ID, result.formatId());
        assertEquals("football", result.sportId());
        assertTrue(json.contains("\"formatId\": \"playforge-save\""));
        assertTrue(json.contains("\"formatVersion\": 1"));
        assertTrue(json.contains("\"sportId\": \"football\""));
        assertTrue(json.contains("\"controlledTeamId\": \"football-team-1\""));
        assertTrue(json.contains("\"leagueName\": \"Save League\""));
        assertTrue(json.contains("\"fixtures\""));
        assertTrue(json.contains("\"selectedLineup\""));
        assertTrue(json.contains("\"attributeProfile.attack\""));
    }

    @Test
    void saveUsesSamePathForNonFootballSportSession() throws IOException {
        SaveGameService service = new SaveGameService(new JsonSaveGameWriter());
        GameSession session = newFakeHandballSession();
        Path savePath = tempDir.resolve("handball-save" + SaveGameFormat.FILE_EXTENSION);

        SaveGameResult result = service.save(session, savePath);

        String json = Files.readString(savePath);
        assertEquals("handball", result.sportId());
        assertTrue(json.contains("\"sportId\": \"handball\""));
        assertTrue(json.contains("\"controlledTeamId\": \"handball-team-1\""));
        assertTrue(json.contains("\"homeTeamId\": \"handball-team-1\""));
        assertTrue(json.contains("\"awayTeamId\": \"handball-team-2\""));
    }

    @Test
    void saveRejectsPlayedMatchWithoutStoredSetupBeforeWritingFile() {
        SaveGameService service = new SaveGameService(new JsonSaveGameWriter());
        Path savePath = tempDir.resolve("broken-save" + SaveGameFormat.FILE_EXTENSION);
        GameSession session = sessionWithIncompletePlayedMatch();

        assertThrows(IllegalStateException.class, () -> service.save(session, savePath));
        assertFalse(Files.exists(savePath));
    }

    private GameSession newFootballSession() {
        SportRegistry registry = new SportRegistry().register(new SportRegistration(
                "football",
                "Football",
                new FootballSportFactory(new FakeAssetProvider(), 4)
        ));
        return new GameInitializationService(registry).startNewSession("football", "Save League");
    }

    private GameSession sessionWithIncompletePlayedMatch() {
        FootballSport sport = new FootballSport();
        FootballLeague league = new FootballLeague("Broken League");
        FootballTeam homeTeam = new FootballTeam("football-team-1", "Home");
        FootballTeam awayTeam = new FootballTeam("football-team-2", "Away");

        homeTeam.addPlayer(new FootballPlayer(
                "football-player-1",
                "Home Player",
                FootballPosition.GOALKEEPER,
                new com.playforgemanager.football.FootballAttributeProfile(50, 50, 50, 50, 50)
        ));
        awayTeam.addPlayer(new FootballPlayer(
                "football-player-2",
                "Away Player",
                FootballPosition.GOALKEEPER,
                new com.playforgemanager.football.FootballAttributeProfile(50, 50, 50, 50, 50)
        ));

        league.addTeam(homeTeam);
        league.addTeam(awayTeam);

        Fixture fixture = new Fixture(1, homeTeam, awayTeam);
        FootballMatch match = new FootballMatch(homeTeam, awayTeam);
        match.setResult(1, 0);
        fixture.attachPlayedMatch(match);
        league.addFixture(fixture);

        return new GameSession(
                sport,
                new FootballSeason(league),
                homeTeam,
                ProgressionState.IN_PROGRESS,
                "football"
        );
    }

    private GameSession newFakeHandballSession() {
        FakeHandballSport sport = new FakeHandballSport();
        FakeHandballLeague league = new FakeHandballLeague("Handball Save League");
        FakeHandballTeam homeTeam = new FakeHandballTeam("handball-team-1", "Handball A");
        FakeHandballTeam awayTeam = new FakeHandballTeam("handball-team-2", "Handball B");

        homeTeam.addPlayer(new FakeHandballPlayer("handball-player-1", "A Player"));
        awayTeam.addPlayer(new FakeHandballPlayer("handball-player-2", "B Player"));

        league.addTeam(homeTeam);
        league.addTeam(awayTeam);
        league.addFixture(new Fixture(1, homeTeam, awayTeam));

        return new GameSession(
                sport,
                new FakeHandballSeason(league),
                homeTeam,
                ProgressionState.READY_TO_START,
                "handball"
        );
    }

    private static class FakeAssetProvider implements AssetProvider {
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

    private static class FakeHandballSport implements Sport {
        private final Ruleset ruleset = new FakeHandballRuleset();
        private final Scheduler scheduler = teams -> List.of();
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
