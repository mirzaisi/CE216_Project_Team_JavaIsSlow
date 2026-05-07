package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballInjuryPolicyTest {

    @Test
    void postMatchInjuresOnePlayerFromTheLosingSide() {
        HandballInjuryPolicy policy = new HandballInjuryPolicy();
        HandballTeam home = team("home", "Red Hawks");
        HandballTeam away = team("away", "Blue Wolves");
        HandballMatch match = readyMatch(home, away);
        match.setResult(25, 29);

        int before = countInjured(home);

        policy.applyPostMatch(match);

        assertEquals(before + 1, countInjured(home));
    }

    @Test
    void recoverPlayersDoesNotDecreaseFreshPostMatchInjuryInSameWeek() {
        HandballInjuryPolicy policy = new HandballInjuryPolicy(1);
        HandballTeam home = team("home", "Red Hawks");
        HandballTeam away = team("away", "Blue Wolves");
        HandballMatch match = readyMatch(home, away);
        match.setResult(25, 29);

        policy.applyPostMatch(match);
        HandballPlayer injuredPlayer = home.getHandballPlayers().stream()
                .filter(player -> player.getInjuryMatchesRemaining() > 0)
                .findFirst()
                .orElseThrow();

        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        policy.recoverPlayers(home);
        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());

        policy.recoverPlayers(home);
        assertEquals(0, injuredPlayer.getInjuryMatchesRemaining());
        assertTrue(injuredPlayer.isAvailable());
    }

    @Test
    void postMatchClearsStoredLineupOnInjuredTeam() {
        HandballInjuryPolicy policy = new HandballInjuryPolicy();
        HandballTeam home = team("home", "Red Hawks");
        HandballTeam away = team("away", "Blue Wolves");

        HandballRuleset ruleset = new HandballRuleset();
        home.assignLineup(ruleset.buildLineup(home.getAvailablePlayers()), ruleset);
        away.assignLineup(ruleset.buildLineup(away.getAvailablePlayers()), ruleset);

        HandballMatch match = readyMatch(home, away);
        match.setResult(21, 24);

        assertNotNull(home.getSelectedHandballLineup());

        policy.applyPostMatch(match);

        assertNull(home.getSelectedHandballLineup());
        assertNotNull(away.getSelectedHandballLineup());
    }

    @Test
    void constructorRejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new HandballInjuryPolicy(0));
        assertThrows(IllegalArgumentException.class, () -> new HandballInjuryPolicy(-1));
    }

    private HandballMatch readyMatch(HandballTeam home, HandballTeam away) {
        HandballRuleset ruleset = new HandballRuleset();
        HandballMatch match = new HandballMatch(home, away);
        HandballTactic tactic = new HandballTactic("Balanced", "3-3", HandballTactic.Tempo.BALANCED, 50, 50);
        match.setHomeSetup(ruleset.buildLineup(home.getAvailablePlayers()), tactic);
        match.setAwaySetup(ruleset.buildLineup(away.getAvailablePlayers()), tactic);
        return match;
    }

    private int countInjured(HandballTeam team) {
        return (int) team.getHandballPlayers().stream()
                .filter(player -> player.getInjuryMatchesRemaining() > 0)
                .count();
    }

    private HandballTeam team(String id, String name) {
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
        team.addPlayer(new HandballPlayer(id + "-rb-2", name + " RB 2", HandballPosition.RIGHT_BACK, profile()));
        return team;
    }

    private HandballAttributeProfile profile() {
        return new HandballAttributeProfile(70, 70, 70, 70, 70);
    }
}
