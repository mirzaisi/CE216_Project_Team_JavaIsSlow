package com.playforgemanager.handball;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Team;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballSeasonTest {

    @Test
    void seasonStartsAtWeekOneAndHasGeneratedFixtures() {
        HandballSeason season = buildSeason();

        assertEquals(1, season.getCurrentWeek());
        assertFalse(season.isCompleted());
        assertFalse(season.getCurrentWeekFixtures().isEmpty());
    }

    @Test
    void playCurrentWeekSimulatesMatchesAndAdvancesSeason() {
        HandballSeason season = buildSeason();

        season.playCurrentWeek(sport(), HandballMatch::new);

        assertEquals(2, season.getCurrentWeek());
        assertTrue(season.getLeague().getFixtures().stream().anyMatch(Fixture::isPlayed));
    }

    @Test
    void playCurrentWeekSkipsAlreadyPlayedFixtureInsteadOfReplayingIt() {
        HandballSeason season = buildSeason();

        Fixture firstFixture = season.getCurrentWeekFixtures().get(0);
        HandballMatch manualMatch = new HandballMatch(firstFixture.getHomeTeam(), firstFixture.getAwayTeam());
        manualMatch.setResult(28, 28);
        firstFixture.attachPlayedMatch(manualMatch);

        season.playCurrentWeek(sport(), HandballMatch::new);

        assertEquals(28, firstFixture.getPlayedMatch().getHomeScore());
        assertEquals(28, firstFixture.getPlayedMatch().getAwayScore());
    }

    @Test
    void seasonMarksCompletedAfterAllWeeksArePlayed() {
        HandballSeason season = buildSeason();

        while (!season.isCompleted()) {
            season.playCurrentWeek(sport(), HandballMatch::new);
        }

        assertTrue(season.isCompleted());
        assertNotNull(season.getStandings());
    }

    @Test
    void autoResolvedLineupAfterInjuryStillSatisfiesHandballPolicy() {
        HandballSeason season = buildSeason();
        HandballTeam team = (HandballTeam) season.getLeague().getTeams().get(0);
        HandballPlayer injuredGoalkeeper = team.getPlayersByPosition(HandballPosition.GOALKEEPER).get(0);
        injuredGoalkeeper.injureForMatches(1);

        season.playCurrentWeek(sport(), HandballMatch::new);

        Fixture fixture = season.getLeague().getFixtures().stream()
                .filter(candidate -> candidate.getWeek() == 1)
                .filter(candidate -> candidate.getHomeTeam() == team || candidate.getAwayTeam() == team)
                .findFirst()
                .orElseThrow();

        HandballLineup lineup = fixture.getHomeTeam() == team
                ? (HandballLineup) fixture.getPlayedMatch().getHomeLineup()
                : (HandballLineup) fixture.getPlayedMatch().getAwayLineup();

        assertFalse(lineup.containsPlayerId(injuredGoalkeeper.getId()));
        assertEquals(1, countPosition(lineup, HandballPosition.GOALKEEPER));
        assertEquals(1, countPosition(lineup, HandballPosition.PIVOT));
    }

    private HandballSeason buildSeason() {
        HandballLeague league = new HandballLeague("Integration League");
        addTeam(league, "team-1", "Red Hawks");
        addTeam(league, "team-2", "Blue Wolves");
        addTeam(league, "team-3", "Golden Stars");
        addTeam(league, "team-4", "Iron Lions");

        List<Fixture> fixtures = new RoundRobinHandballScheduler().generateFixtures(league.getTeams());
        league.addFixtures(fixtures);

        return new HandballSeason(league);
    }

    private Sport sport() {
        HandballRuleset ruleset = new HandballRuleset();
        HandballStandingsPolicy standingsPolicy = new HandballStandingsPolicy(ruleset);
        MatchEngine engine = new HandballMatchEngine();
        HandballInjuryPolicy injuryPolicy = new HandballInjuryPolicy();

        return new Sport() {
            @Override
            public String getName() {
                return "Handball";
            }

            @Override
            public HandballRuleset getRuleset() {
                return ruleset;
            }

            @Override
            public RoundRobinHandballScheduler getScheduler() {
                return new RoundRobinHandballScheduler();
            }

            @Override
            public HandballStandingsPolicy getStandingsPolicy() {
                return standingsPolicy;
            }

            @Override
            public MatchEngine getMatchEngine() {
                return engine;
            }

            @Override
            public HandballInjuryPolicy getInjuryPolicy() {
                return injuryPolicy;
            }
        };
    }

    private void addTeam(HandballLeague league, String id, String name) {
        HandballTeam team = new HandballTeam(id, name);
        team.addPlayer(new HandballPlayer(id + "-gk-1", name + " GK 1", HandballPosition.GOALKEEPER, profile()));
        team.addPlayer(new HandballPlayer(id + "-lw-1", name + " LW 1", HandballPosition.LEFT_WING, profile()));
        team.addPlayer(new HandballPlayer(id + "-rw-1", name + " RW 1", HandballPosition.RIGHT_WING, profile()));
        team.addPlayer(new HandballPlayer(id + "-lb-1", name + " LB 1", HandballPosition.LEFT_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-cb-1", name + " CB 1", HandballPosition.CENTER_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-rb-1", name + " RB 1", HandballPosition.RIGHT_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-pv-1", name + " PV 1", HandballPosition.PIVOT, profile()));
        team.addPlayer(new HandballPlayer(id + "-gk-2", name + " GK 2", HandballPosition.GOALKEEPER, profile()));
        team.addPlayer(new HandballPlayer(id + "-lw-2", name + " LW 2", HandballPosition.LEFT_WING, profile()));
        team.addPlayer(new HandballPlayer(id + "-rw-2", name + " RW 2", HandballPosition.RIGHT_WING, profile()));
        team.addPlayer(new HandballPlayer(id + "-lb-2", name + " LB 2", HandballPosition.LEFT_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-cb-2", name + " CB 2", HandballPosition.CENTER_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-rb-2", name + " RB 2", HandballPosition.RIGHT_BACK, profile()));
        team.addPlayer(new HandballPlayer(id + "-pv-2", name + " PV 2", HandballPosition.PIVOT, profile()));
        league.addTeam(team);
    }

    private long countPosition(HandballLineup lineup, HandballPosition position) {
        return lineup.getStartingPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .count();
    }

    private HandballAttributeProfile profile() {
        return new HandballAttributeProfile(70, 70, 70, 70, 70);
    }
}
