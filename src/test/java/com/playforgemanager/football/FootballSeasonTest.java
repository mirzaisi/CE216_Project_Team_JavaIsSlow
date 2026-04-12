package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonTest {

    @Test
    void seasonStartsAtWeekOneAndHasGeneratedFixtures() {
        FootballSeason season = buildSeason();

        assertEquals(1, season.getCurrentWeek());
        assertFalse(season.isCompleted());
        assertFalse(season.getCurrentWeekFixtures().isEmpty());
    }

    @Test
    void playCurrentWeekSimulatesMatchesAndAdvancesSeason() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        season.playCurrentWeek(sport, FootballMatch::new);

        assertEquals(2, season.getCurrentWeek());
        assertTrue(season.getLeague().getFixtures().stream().anyMatch(fixture -> fixture.isPlayed()));
    }

    @Test
    void playCurrentWeekSkipsAlreadyPlayedFixtureInsteadOfReplayingIt() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        var firstFixture = season.getCurrentWeekFixtures().get(0);
        var manualMatch = new FootballMatch(firstFixture.getHomeTeam(), firstFixture.getAwayTeam());
        manualMatch.setResult(1, 1);
        firstFixture.attachPlayedMatch(manualMatch);

        season.playCurrentWeek(sport, FootballMatch::new);

        assertEquals(1, firstFixture.getPlayedMatch().getHomeScore());
        assertEquals(1, firstFixture.getPlayedMatch().getAwayScore());
    }

    @Test
    void injuryDurationIsNotDecrementedInTheSamePlayedWeek() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        season.playCurrentWeek(sport, FootballMatch::new);

        FootballPlayer injuredPlayer = season.getLeague().getTeams().stream()
                .map(FootballTeam.class::cast)
                .flatMap(team -> team.getFootballPlayers().stream())
                .filter(player -> player.getInjuryMatchesRemaining() > 0)
                .findFirst()
                .orElseThrow();

        assertEquals(2, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        season.playCurrentWeek(sport, FootballMatch::new);

        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        season.playCurrentWeek(sport, FootballMatch::new);

        assertEquals(0, injuredPlayer.getInjuryMatchesRemaining());
        assertTrue(injuredPlayer.isAvailable());
    }

    @Test
    void autoResolvedLineupAfterInjurySatisfiesFootballPolicy() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();
        FootballTeam team = (FootballTeam) season.getLeague().getTeams().get(0);
        FootballPlayer injuredGoalkeeper = team.getPlayersByPosition(FootballPosition.GOALKEEPER).get(0);
        injuredGoalkeeper.injureForMatches(2);

        season.playCurrentWeek(sport, FootballMatch::new);

        Fixture fixture = season.getLeague().getFixtures().stream()
                .filter(candidate -> candidate.getWeek() == 1)
                .filter(candidate -> candidate.getHomeTeam() == team || candidate.getAwayTeam() == team)
                .findFirst()
                .orElseThrow();
        FootballLineup lineup = fixture.getHomeTeam() == team
                ? (FootballLineup) fixture.getPlayedMatch().getHomeLineup()
                : (FootballLineup) fixture.getPlayedMatch().getAwayLineup();

        assertFalse(lineup.containsPlayerId(injuredGoalkeeper.getId()));
        assertEquals(FootballRuleset.REQUIRED_GOALKEEPERS, countPosition(lineup, FootballPosition.GOALKEEPER));
        assertEquals(FootballRuleset.REQUIRED_DEFENDERS, countPosition(lineup, FootballPosition.DEFENDER));
        assertEquals(FootballRuleset.REQUIRED_MIDFIELDERS, countPosition(lineup, FootballPosition.MIDFIELDER));
        assertEquals(FootballRuleset.REQUIRED_FORWARDS, countPosition(lineup, FootballPosition.FORWARD));
    }

    @Test
    void seasonMarksCompletedAfterAllWeeksArePlayed() {
        FootballSport sport = new FootballSport();
        FootballSeason season = buildSeason();

        while (!season.isCompleted()) {
            season.playCurrentWeek(sport, FootballMatch::new);
        }

        assertTrue(season.isCompleted());
        assertTrue(season.canCreateNextSeason());
        assertNotNull(season.createNextSeasonStub());
    }

    private FootballSeason buildSeason() {
        FootballLeague league = new FootballLeague("Integration League");
        addTeam(league, "team-1", "Red Hawks");
        addTeam(league, "team-2", "Blue Wolves");
        addTeam(league, "team-3", "Golden Stars");
        addTeam(league, "team-4", "Iron Lions");

        List<Fixture> fixtures = new RoundRobinFootballScheduler().generateFixtures(league.getTeams());
        league.addFixtures(fixtures);

        return new FootballSeason(league);
    }

    private void addTeam(FootballLeague league, String id, String name) {
        FootballTeam team = new FootballTeam(id, name);

        for (int i = 0; i < 19; i++) {
            team.addPlayer(new FootballPlayer(
                    id + "-player-" + i,
                    name + " Player " + i,
                    positionForIndex(i),
                    new FootballAttributeProfile(70, 70, 70, 70, 70)
            ));
        }

        league.addTeam(team);
    }

    private long countPosition(FootballLineup lineup, FootballPosition position) {
        return lineup.getStartingPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .count();
    }

    private FootballPosition positionForIndex(int index) {
        if (index == 0 || index == 17 || index == 18) {
            return FootballPosition.GOALKEEPER;
        }
        if (index <= 6) {
            return FootballPosition.DEFENDER;
        }
        if (index <= 12) {
            return FootballPosition.MIDFIELDER;
        }
        return FootballPosition.FORWARD;
    }
}
