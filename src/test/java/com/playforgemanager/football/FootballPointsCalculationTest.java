package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FootballPointsCalculationTest {
    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void pointsForResult_usesThreeOneZeroScoring() {
        assertEquals(3, ruleset.getPointsForResult(3, 1));
        assertEquals(1, ruleset.getPointsForResult(2, 2));
        assertEquals(0, ruleset.getPointsForResult(0, 4));
    }

    @Test
    void standingRow_appliesRulesetPointsForWinDrawAndLoss() {
        FootballTeam team = new FootballTeam("TEAM-1", "Team One");
        FootballStandingRow row = new FootballStandingRow(team);

        row.recordMatch(2, 1, ruleset);
        assertEquals(1, row.getPlayed());
        assertEquals(1, row.getWins());
        assertEquals(0, row.getDraws());
        assertEquals(0, row.getLosses());
        assertEquals(3, row.getPoints());

        row.recordMatch(1, 1, ruleset);
        assertEquals(2, row.getPlayed());
        assertEquals(1, row.getWins());
        assertEquals(1, row.getDraws());
        assertEquals(0, row.getLosses());
        assertEquals(4, row.getPoints());

        row.recordMatch(0, 3, ruleset);
        assertEquals(3, row.getPlayed());
        assertEquals(1, row.getWins());
        assertEquals(1, row.getDraws());
        assertEquals(1, row.getLosses());
        assertEquals(4, row.getPoints());
    }
}
