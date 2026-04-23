package com.playforgemanager.application;

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
import com.playforgemanager.football.FootballSportFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameInitializationServiceTest {

    @Test
    void startNewSessionCreatesSessionWithExpectedAttachedObjects() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assetProvider, 4)));
        GameInitializationService service = new GameInitializationService(registry);

        GameSession session = service.startNewSession("football", "Starter League");

        assertEquals("Football", session.getActiveSport().getName());
        assertEquals("football", session.getSelectedSportId());
        assertEquals("Starter League", session.getCurrentSeason().getLeague().getName());
        assertEquals("Red Hawks", session.getControlledTeam().getName());
        assertEquals(4, session.getCurrentSeason().getLeague().getTeamCount());
        assertEquals(ProgressionState.READY_TO_START, session.getProgressionState());
    }

    @Test
    void startNewSessionUsesFirstGeneratedTeamAsControlledTeam() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assetProvider, 4)));
        GameInitializationService service = new GameInitializationService(registry);

        GameSession session = service.startNewSession("football", "Starter League");

        assertEquals(
                session.getCurrentSeason().getLeague().getTeams().get(0),
                session.getControlledTeam()
        );
    }

    @Test
    void startNewSessionRejectsBlankLeagueName() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assetProvider, 4)));
        GameInitializationService service = new GameInitializationService(registry);

        assertThrows(IllegalArgumentException.class, () ->
                service.startNewSession("football", "   ")
        );
    }

    @Test
    void startNewSessionUsesRegistryChoiceToResolveDifferentSportFactory() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assetProvider, 4)))
                .register(new SportRegistration("handball", "Handball", new FakeHandballSportFactory()));
        GameInitializationService service = new GameInitializationService(registry);

        GameSession session = service.startNewSession("handball", "Starter League");

        assertEquals("Handball", session.getActiveSport().getName());
        assertEquals("handball", session.getSelectedSportId());
        assertEquals("Starter League", session.getCurrentSeason().getLeague().getName());
        assertEquals(2, session.getCurrentSeason().getLeague().getTeamCount());
        assertEquals(1, session.getCurrentSeason().getLeague().getFixtures().size());
        assertEquals("Handball A", session.getControlledTeam().getName());
        assertInstanceOf(FakeHandballSeason.class, session.getCurrentSeason());
    }

    @Test
    void startNewSessionRejectsUnknownSportChoice() {
        AssetProvider assetProvider = new FakeAssetProvider();
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(assetProvider, 4)));
        GameInitializationService service = new GameInitializationService(registry);

        assertThrows(IllegalArgumentException.class, () ->
                service.startNewSession("basketball", "Starter League")
        );
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
            FakeHandballTeam firstTeam = new FakeHandballTeam("handball-team-1", "Handball A");
            firstTeam.addPlayer(new FakeHandballPlayer("handball-player-1", "A Player 1"));
            firstTeam.addPlayer(new FakeHandballPlayer("handball-player-2", "A Player 2"));

            FakeHandballTeam secondTeam = new FakeHandballTeam("handball-team-2", "Handball B");
            secondTeam.addPlayer(new FakeHandballPlayer("handball-player-3", "B Player 1"));
            secondTeam.addPlayer(new FakeHandballPlayer("handball-player-4", "B Player 2"));

            league.addTeam(firstTeam);
            league.addTeam(secondTeam);
            return league;
        }

        @Override
        public Season createSeason(League league) {
            return new FakeHandballSeason(league);
        }
    }

    private static class FakeHandballSport implements Sport {
        private final Ruleset ruleset = new FakeHandballRuleset();
        private final Scheduler scheduler = new FakeHandballScheduler();
        private final StandingsPolicy standingsPolicy = new FakeHandballStandingsPolicy();
        private final MatchEngine matchEngine = new FakeHandballMatchEngine();
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
        protected FakeHandballLeague(String name) {
            super(name);
        }
    }

    private static class FakeHandballSeason extends Season {
        protected FakeHandballSeason(League league) {
            super(league);
        }

        @Override
        protected void doAdvanceWeek() {
            markCompleted();
        }
    }

    private static class FakeHandballTeam extends com.playforgemanager.core.Team {
        protected FakeHandballTeam(String id, String name) {
            super(id, name);
        }
    }

    private static class FakeHandballPlayer extends Player {
        protected FakeHandballPlayer(String id, String name) {
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

    private static class FakeHandballScheduler implements Scheduler {
        @Override
        public List<Fixture> generateFixtures(List<? extends com.playforgemanager.core.Team> teams) {
            return List.of(new Fixture(1, teams.get(0), teams.get(1)));
        }
    }

    private static class FakeHandballStandingsPolicy implements StandingsPolicy {
        @Override
        public void recordMatch(League league, Match match) {
        }

        @Override
        public List<com.playforgemanager.core.Team> rankTeams(League league) {
            return league.getTeams();
        }
    }

    private static class FakeHandballMatchEngine implements MatchEngine {
        @Override
        public void simulate(Match match, Ruleset ruleset) {
        }
    }

    private static class FakeHandballInjuryPolicy implements InjuryPolicy {
        @Override
        public void applyPostMatch(Match match) {
        }

        @Override
        public void recoverPlayers(com.playforgemanager.core.Team team) {
        }
    }
}
