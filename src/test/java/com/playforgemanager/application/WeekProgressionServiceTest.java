package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.League;
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
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeekProgressionServiceTest {

    @Test
    void advanceOneStepProgressesFootballSessionAndReturnsWeekSummary() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        GameInitializationService initializationService =
                new GameInitializationService(new FootballSportFactory(assetProvider, 4));
        GameSession session = initializationService.startNewSession("Integration League");
        WeekProgressionService progressionService =
                new WeekProgressionService(DefaultWeekProgressionRegistry.create());

        WeekProgressionResult result = progressionService.advanceOneStep(session);

        assertEquals("football", result.getSportId());
        assertEquals(1, result.getPlayedWeek());
        assertEquals(2, result.getCurrentWeekAfterProgression());
        assertEquals(ProgressionState.IN_PROGRESS, result.getProgressionState());
        assertEquals(2, result.getPlayedFixtures().size());
        assertTrue(result.getPlayedFixtures().stream().allMatch(Fixture::isPlayed));
        assertEquals(4, result.getAvailabilityChanges().size());
        assertEquals(4, result.getRankedTeams().size());
        assertFalse(result.isSeasonCompleted());
        assertEquals(ProgressionState.IN_PROGRESS, session.getProgressionState());
    }

    @Test
    void advanceOneStepWorksWithCustomHandballStrategy() {
        FakeHandballTeam home = new FakeHandballTeam("handball-team-1", "Handball A");
        home.addPlayer(new FakeHandballPlayer("handball-player-1", "A1"));
        FakeHandballTeam away = new FakeHandballTeam("handball-team-2", "Handball B");
        away.addPlayer(new FakeHandballPlayer("handball-player-2", "B1"));

        FakeHandballLeague league = new FakeHandballLeague("Handball League");
        league.addTeam(home);
        league.addTeam(away);
        league.addFixture(new Fixture(1, home, away));

        GameSession session = new GameSession(
                new FakeHandballSport(),
                new FakeHandballSeason(league),
                home,
                ProgressionState.READY_TO_START,
                "handball"
        );

        WeekProgressionRegistry registry = new WeekProgressionRegistry()
                .register("handball", new FakeHandballWeekProgressionStrategy());
        WeekProgressionService progressionService = new WeekProgressionService(registry);

        WeekProgressionResult result = progressionService.advanceOneStep(session);

        assertEquals("handball", result.getSportId());
        assertEquals(1, result.getPlayedWeek());
        assertEquals(1, result.getCurrentWeekAfterProgression());
        assertTrue(result.isSeasonCompleted());
        assertEquals(ProgressionState.COMPLETED, result.getProgressionState());
        assertEquals(1, result.getPlayedFixtures().size());
        assertTrue(result.getPlayedFixtures().get(0).isPlayed());
        assertEquals(2, result.getRankedTeams().size());
        assertInstanceOf(FakeHandballSeason.class, session.getCurrentSeason());
    }

    private static class FakeHandballWeekProgressionStrategy implements WeekProgressionStrategy {
        @Override
        public WeekProgressionContext createContext(GameSession session) {
            return new WeekProgressionContext(
                    session.getCurrentSeason().getCurrentWeek(),
                    session.getCurrentSeason().getLeague().getFixtures()
            );
        }

        @Override
        public void prepareMatches(GameSession session, WeekProgressionContext context) {
            for (Fixture fixture : context.getScheduledFixtures()) {
                if (!fixture.isPlayed()) {
                    context.addPreparedMatch(fixture, new FakeHandballMatch(fixture.getHomeTeam(), fixture.getAwayTeam()));
                }
            }
        }

        @Override
        public void simulateMatches(GameSession session, WeekProgressionContext context) {
            for (Fixture fixture : context.getScheduledFixtures()) {
                if (fixture.isPlayed()) {
                    continue;
                }
                Match match = context.getPreparedMatch(fixture);
                match.setResult(24, 22);
                fixture.attachPlayedMatch(match);
            }
        }

        @Override
        public void advanceWeek(GameSession session, WeekProgressionContext context) {
            session.getCurrentSeason().advanceWeek();
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
        public boolean isValidLineup(com.playforgemanager.core.Lineup lineup) {
            return true;
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
