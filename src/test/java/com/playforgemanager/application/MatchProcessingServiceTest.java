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
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;
import com.playforgemanager.football.FootballLineup;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.football.FootballTactic;
import com.playforgemanager.football.FootballTeam;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchProcessingServiceTest {

    @Test
    void playControlledMatchProcessesFootballFixtureAndReturnsUiFriendlyResult() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        GameInitializationService initializationService =
                new GameInitializationService(new FootballSportFactory(assetProvider, 4));
        GameSession session = initializationService.startNewSession("Integration League");
        FootballTeam controlledTeam = (FootballTeam) session.getControlledTeam();
        FootballLineup selectedLineup = controlledTeam.getSelectedFootballLineup();
        FootballTactic selectedTactic = new FootballTactic(
                "Control",
                "4-3-3",
                FootballTactic.Mentality.BALANCED,
                60,
                57
        );

        MatchProcessingService service = new MatchProcessingService(DefaultMatchProcessingRegistry.create());

        MatchProcessingResult result = service.playControlledMatch(session, selectedLineup, selectedTactic);

        assertEquals("football", result.getSportId());
        assertEquals(1, result.getWeekNumber());
        assertEquals(controlledTeam.getName(), result.getControlledTeamName());
        assertEquals(ProgressionState.IN_PROGRESS, result.getProgressionState());
        assertTrue(result.getControlledTeamScore() >= 0);
        assertTrue(result.getOpponentScore() >= 0);
        assertEquals(2, result.getAvailabilityChanges().size());
        assertTrue(result.getControlledTeamRankAfterMatch() >= 1);
        assertEquals(4, result.getRankedTeamsAfterMatch().size());
        assertNotNull(result.getFixture().getPlayedMatch());

        Match playedMatch = result.getFixture().getPlayedMatch();
        if (result.isControlledTeamHome()) {
            assertSame(selectedLineup, playedMatch.getHomeLineup());
            assertSame(selectedTactic, playedMatch.getHomeTactic());
        } else {
            assertSame(selectedLineup, playedMatch.getAwayLineup());
            assertSame(selectedTactic, playedMatch.getAwayTactic());
        }
    }

    @Test
    void playControlledMatchWorksWithCustomHandballStrategyWithoutFootballTypes() {
        FakeHandballTeam home = new FakeHandballTeam("handball-team-1", "Handball A");
        FakeHandballPlayer homePlayer = new FakeHandballPlayer("handball-player-1", "A1");
        home.addPlayer(homePlayer);

        FakeHandballTeam away = new FakeHandballTeam("handball-team-2", "Handball B");
        FakeHandballPlayer awayPlayer = new FakeHandballPlayer("handball-player-2", "B1");
        away.addPlayer(awayPlayer);

        FakeHandballLeague league = new FakeHandballLeague("Handball League");
        league.addTeam(home);
        league.addTeam(away);
        Fixture fixture = new Fixture(1, home, away);
        league.addFixture(fixture);

        GameSession session = new GameSession(
                new FakeHandballSport(),
                new FakeHandballSeason(league),
                home,
                ProgressionState.READY_TO_START,
                "handball"
        );

        MatchProcessingRegistry registry = new MatchProcessingRegistry()
                .register("handball", new FakeHandballMatchProcessingStrategy());
        MatchProcessingService service = new MatchProcessingService(registry);

        MatchProcessingResult result = service.playControlledMatch(
                session,
                new FakeHandballLineup(List.of(homePlayer)),
                new FakeHandballTactic("Fast Break")
        );

        assertEquals("handball", result.getSportId());
        assertEquals("Handball A", result.getControlledTeamName());
        assertEquals("Handball B", result.getOpponentTeamName());
        assertEquals(26, result.getControlledTeamScore());
        assertEquals(23, result.getOpponentScore());
        assertEquals(1, result.getControlledTeamRankAfterMatch());
        assertEquals(2, result.getAvailabilityChanges().size());
        assertEquals(ProgressionState.IN_PROGRESS, result.getProgressionState());
        assertTrue(result.getFixture().isPlayed());
    }

    private static class FakeHandballMatchProcessingStrategy implements MatchProcessingStrategy {
        @Override
        public Match processMatch(GameSession session, Fixture fixture) {
            Match match = new FakeHandballMatch(fixture.getHomeTeam(), fixture.getAwayTeam());
            match.setHomeSetup(fixture.getHomeTeam().getSelectedLineup(), fixture.getHomeTeam().getSelectedTactic());
            match.setAwaySetup(
                    new FakeHandballLineup(fixture.getAwayTeam().getRoster()),
                    new FakeHandballTactic("Balanced")
            );
            match.setResult(26, 23);
            fixture.attachPlayedMatch(match);
            return match;
        }
    }

    private static class FakeHandballSport implements Sport {
        private final Ruleset ruleset = new FakeHandballRuleset();
        private final Scheduler scheduler = teams -> List.of();
        private final StandingsPolicy standingsPolicy = new FakeHandballStandingsPolicy();
        private final MatchEngine matchEngine = (match, ruleset) -> { };
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

    private static class FakeHandballTeam extends Team {
        protected FakeHandballTeam(String id, String name) {
            super(id, name);
        }
    }

    private static class FakeHandballPlayer extends Player {
        protected FakeHandballPlayer(String id, String name) {
            super(id, name);
        }
    }

    private static class FakeHandballMatch extends Match {
        protected FakeHandballMatch(Team homeTeam, Team awayTeam) {
            super(homeTeam, awayTeam);
        }
    }

    private static class FakeHandballLineup implements Lineup {
        private final List<? extends Player> selectedPlayers;

        private FakeHandballLineup(List<? extends Player> selectedPlayers) {
            this.selectedPlayers = List.copyOf(selectedPlayers);
        }

        @Override
        public List<? extends Player> getSelectedPlayers() {
            return selectedPlayers;
        }

        @Override
        public int size() {
            return selectedPlayers.size();
        }
    }

    private static class FakeHandballTactic implements Tactic {
        private final String name;

        private FakeHandballTactic(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
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
