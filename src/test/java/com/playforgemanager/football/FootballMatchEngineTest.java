package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballMatchEngineTest {

    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void simulatePersistsResultOnTheMatch() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(match, ruleset);

        assertTrue(match.isPlayed());
        assertTrue(match.getHomeScore() >= 0);
        assertTrue(match.getAwayScore() >= 0);
    }

    @Test
    void simulatedScoresStayWithinSaneBounds() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(match, ruleset);

        // generous upper bound - generator maxes around 5-6
        assertTrue(match.getHomeScore() <= 10);
        assertTrue(match.getAwayScore() <= 10);
    }

    @Test
    void simulateIsDeterministicForTheSameTeamsAndSameEngine() {
        FootballMatchEngine engine = new FootballMatchEngine();

        FootballMatch first = buildReadyMatch("Red Hawks", "Blue Wolves");
        FootballMatch second = buildReadyMatch("Red Hawks", "Blue Wolves");

        engine.simulate(first, ruleset);
        engine.simulate(second, ruleset);

        assertEquals(first.getHomeScore(), second.getHomeScore());
        assertEquals(first.getAwayScore(), second.getAwayScore());
    }

    @Test
    void differentSeedOffsetCanProduceDifferentScoresForTheSameMatch() {
        FootballMatchEngine defaultEngine = new FootballMatchEngine();
        FootballMatchEngine variantEngine = new FootballMatchEngine(12345L);

        FootballMatch baseline = buildReadyMatch("Red Hawks", "Blue Wolves");
        FootballMatch variant = buildReadyMatch("Red Hawks", "Blue Wolves");

        defaultEngine.simulate(baseline, ruleset);
        variantEngine.simulate(variant, ruleset);

        boolean scoresDiffer = baseline.getHomeScore() != variant.getHomeScore()
                || baseline.getAwayScore() != variant.getAwayScore();
        assertTrue(scoresDiffer);
    }

    @Test
    void alreadyPlayedMatchIsNotReSimulated() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");
        match.setResult(7, 7);

        engine.simulate(match, ruleset);

        assertTrue(match.isPlayed());
        assertEquals(7, match.getHomeScore());
        assertEquals(7, match.getAwayScore());
    }

    @Test
    void simulateRejectsMatchWithoutHomeLineup() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballTeam home = teamWithPlayers("home", "Home FC", 11);
        FootballTeam away = teamWithPlayers("away", "Away FC", 11);
        FootballMatch match = new FootballMatch(home, away);
        match.setAwaySetup(
                new FootballLineup(away.getFootballPlayers()),
                new FootballTactic("Default", "4-3-3", FootballTactic.Mentality.BALANCED, 50, 50)
        );

        assertThrows(IllegalArgumentException.class, () -> engine.simulate(match, ruleset));
        assertFalse(match.isPlayed());
    }

    @Test
    void simulateRejectsLineupsWithWrongSize() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballTeam home = teamWithPlayers("home", "Home FC", 11);
        FootballTeam away = teamWithPlayers("away", "Away FC", 11);
        FootballMatch match = new FootballMatch(home, away);

        FootballLineup tooSmall = new FootballLineup(home.getFootballPlayers().subList(0, 10));
        FootballLineup full = new FootballLineup(away.getFootballPlayers());
        FootballTactic tactic = new FootballTactic(
                "Default", "4-3-3", FootballTactic.Mentality.BALANCED, 50, 50
        );
        match.setHomeSetup(tooSmall, tactic);
        match.setAwaySetup(full, tactic);

        assertThrows(IllegalArgumentException.class, () -> engine.simulate(match, ruleset));
        assertFalse(match.isPlayed());
    }

    @Test
    void engineDoesNotRetainStateBetweenSimulations() {
        // simulate A, then B, then A again with a fresh match - A's result
        // must be the same both times
        FootballMatchEngine engine = new FootballMatchEngine();

        FootballMatch firstA = buildReadyMatch("Red Hawks", "Blue Wolves");
        engine.simulate(firstA, ruleset);

        FootballMatch matchB = buildReadyMatch("Golden Stars", "Iron Lions");
        engine.simulate(matchB, ruleset);
        assertNotNull(matchB);

        FootballMatch secondA = buildReadyMatch("Red Hawks", "Blue Wolves");
        engine.simulate(secondA, ruleset);

        assertEquals(firstA.getHomeScore(), secondA.getHomeScore());
        assertEquals(firstA.getAwayScore(), secondA.getAwayScore());
    }

    @Test
    void engineAcceptsValidLineupsWithoutThrowing() {
        FootballMatchEngine engine = new FootballMatchEngine();
        FootballMatch match = buildReadyMatch("Red Hawks", "Blue Wolves");

        assertDoesNotThrow(() -> engine.simulate(match, ruleset));
    }

    private FootballMatch buildReadyMatch(String homeName, String awayName) {
        FootballTeam home = teamWithPlayers("home-" + homeName, homeName, 11);
        FootballTeam away = teamWithPlayers("away-" + awayName, awayName, 11);
        FootballMatch match = new FootballMatch(home, away);

        FootballLineup homeLineup = new FootballLineup(home.getFootballPlayers());
        FootballLineup awayLineup = new FootballLineup(away.getFootballPlayers());
        FootballTactic tactic = new FootballTactic(
                "Default", "4-3-3", FootballTactic.Mentality.BALANCED, 50, 50
        );
        match.setHomeSetup(homeLineup, tactic);
        match.setAwaySetup(awayLineup, tactic);
        return match;
    }

    private FootballTeam teamWithPlayers(String id, String name, int count) {
        FootballTeam team = new FootballTeam(id, name);
        List<FootballPosition> positions = List.of(
                FootballPosition.GOALKEEPER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.FORWARD,
                FootballPosition.FORWARD
        );
        for (int i = 0; i < count; i++) {
            team.addPlayer(new FootballPlayer(
                    id + "-player-" + i,
                    name + " Player " + i,
                    positions.get(i % positions.size()),
                    new FootballAttributeProfile(70, 70, 70, 70, 70)
            ));
        }
        return team;
    }
}
