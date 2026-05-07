package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballMatchEngineTest {
    private final HandballRuleset ruleset = new HandballRuleset();

    @Test
    void simulatePersistsResultOnTheMatch() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(match, ruleset);

        assertTrue(match.isPlayed());
        assertTrue(match.getHomeScore() >= 0);
        assertTrue(match.getAwayScore() >= 0);
    }

    @Test
    void simulatedScoresStayWithinSaneBounds() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(match, ruleset);

        assertTrue(match.getHomeScore() <= 45);
        assertTrue(match.getAwayScore() <= 45);
    }

    @Test
    void simulateIsDeterministicForTheSameTeamsAndSameEngine() {
        HandballMatchEngine engine = new HandballMatchEngine();

        HandballMatch first = buildReadyMatch("Red Hawks", "Blue Wolves");
        HandballMatch second = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(first, ruleset);
        engine.simulate(second, ruleset);

        assertEquals(first.getHomeScore(), second.getHomeScore());
        assertEquals(first.getAwayScore(), second.getAwayScore());
    }

    @Test
    void alreadyPlayedMatchIsNotReSimulated() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");
        match.setResult(30, 30);

        engine.simulate(match, ruleset);

        assertTrue(match.isPlayed());
        assertEquals(30, match.getHomeScore());
        assertEquals(30, match.getAwayScore());
    }

    @Test
    void simulateRejectsMatchWithoutHomeLineup() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballTeam home = team("home", "Home HB", 70, 70, 70, 70, 70);
        HandballTeam away = team("away", "Away HB", 70, 70, 70, 70, 70);
        HandballMatch match = new HandballMatch(home, away);
        match.setAwaySetup(
                ruleset.buildLineup(away.getAvailablePlayers()),
                new HandballTactic("Balanced", "3-3", HandballTactic.Tempo.BALANCED, 50, 50)
        );

        assertThrows(IllegalArgumentException.class, () -> engine.simulate(match, ruleset));
        assertFalse(match.isPlayed());
    }

    @Test
    void simulateRejectsLineupsWithWrongSize() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballTeam home = team("home", "Home HB", 70, 70, 70, 70, 70);
        HandballTeam away = team("away", "Away HB", 70, 70, 70, 70, 70);
        HandballMatch match = new HandballMatch(home, away);

        HandballLineup tooSmall = new HandballLineup(home.getHandballPlayers().subList(0, 6));
        HandballLineup full = ruleset.buildLineup(away.getAvailablePlayers());
        HandballTactic tactic = new HandballTactic("Balanced", "3-3", HandballTactic.Tempo.BALANCED, 50, 50);
        match.setHomeSetup(tooSmall, tactic);
        match.setAwaySetup(full, tactic);

        assertThrows(IllegalArgumentException.class, () -> engine.simulate(match, ruleset));
        assertFalse(match.isPlayed());
    }

    @Test
    void engineAcceptsValidLineupsWithoutThrowing() {
        HandballMatchEngine engine = new HandballMatchEngine();
        HandballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        assertDoesNotThrow(() -> engine.simulate(match, ruleset));
    }

    @Test
    void strongerLineupProducesBetterDeterministicHomeOutputAgainstSameOpponent() {
        HandballTeam strongHome = team("home", "Home HB", 92, 88, 90, 91, 89);
        HandballTeam weakHome = team("home", "Home HB", 38, 36, 41, 39, 37);
        HandballTeam sameAwayForStrong = team("away", "Away HB", 60, 60, 60, 60, 60);
        HandballTeam sameAwayForWeak = team("away", "Away HB", 60, 60, 60, 60, 60);

        HandballMatch strongMatch = readyMatch(strongHome, sameAwayForStrong);
        HandballMatch weakMatch = readyMatch(weakHome, sameAwayForWeak);

        HandballMatchEngine engine = new HandballMatchEngine(0L);
        engine.simulate(strongMatch, ruleset);
        engine.simulate(weakMatch, ruleset);

        assertTrue(strongMatch.getHomeScore() > weakMatch.getHomeScore());
    }

    private HandballMatch buildReadyMatch(String homeName, String awayName) {
        HandballTeam home = team("home-" + homeName, homeName, 70, 70, 70, 70, 70);
        HandballTeam away = team("away-" + awayName, awayName, 70, 70, 70, 70, 70);
        return readyMatch(home, away);
    }

    private HandballMatch readyMatch(HandballTeam home, HandballTeam away) {
        HandballMatch match = new HandballMatch(home, away);
        HandballTactic tactic = new HandballTactic("Balanced", "3-3", HandballTactic.Tempo.BALANCED, 55, 55);
        match.setHomeSetup(ruleset.buildLineup(home.getAvailablePlayers()), tactic);
        match.setAwaySetup(ruleset.buildLineup(away.getAvailablePlayers()), tactic);
        return match;
    }

    private HandballTeam team(
            String idPrefix,
            String name,
            int shooting,
            int defense,
            int passing,
            int speed,
            int reflexes
    ) {
        HandballTeam team = new HandballTeam(idPrefix, name);
        List<HandballPosition> positions = List.of(
                HandballPosition.GOALKEEPER,
                HandballPosition.LEFT_WING,
                HandballPosition.RIGHT_WING,
                HandballPosition.LEFT_BACK,
                HandballPosition.CENTER_BACK,
                HandballPosition.RIGHT_BACK,
                HandballPosition.PIVOT,
                HandballPosition.GOALKEEPER,
                HandballPosition.LEFT_WING,
                HandballPosition.RIGHT_BACK
        );

        for (int i = 0; i < positions.size(); i++) {
            team.addPlayer(new HandballPlayer(
                    idPrefix + "-player-" + i,
                    name + " Player " + i,
                    positions.get(i),
                    new HandballAttributeProfile(shooting, defense, passing, speed, reflexes)
            ));
        }
        return team;
    }
}
