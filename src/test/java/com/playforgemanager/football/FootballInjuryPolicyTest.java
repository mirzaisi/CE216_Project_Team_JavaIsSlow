package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballInjuryPolicyTest {

    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void postMatchInjuresOnePlayerFromTheLosingSide() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);
        FootballMatch match = matchWithLineups(home, away);
        match.setResult(0, 2);

        int before = countInjured(home);

        policy.applyPostMatch(match);

        assertEquals(before + 1, countInjured(home));
    }

    @Test
    void postMatchDoesNotInjureUnplayedMatch() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);
        FootballMatch match = matchWithLineups(home, away);
        // no setResult - match stays unplayed

        policy.applyPostMatch(match);

        for (FootballPlayer player : home.getFootballPlayers()) {
            assertEquals(0, player.getInjuryMatchesRemaining());
            assertTrue(player.isAvailable());
        }
    }

    @Test
    void injuredPlayerBecomesUnavailableImmediately() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);
        FootballMatch match = matchWithLineups(home, away);
        match.setResult(0, 1);

        policy.applyPostMatch(match);

        boolean anyOut = home.getFootballPlayers().stream().anyMatch(p -> !p.isAvailable());
        assertTrue(anyOut);
    }

    @Test
    void recoverPlayersDecreasesRemainingCountEachCall() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy(3);
        FootballTeam team = teamWithPlayers("home", "Red Hawks", 11);
        FootballPlayer player = team.getFootballPlayers().get(0);
        player.injureForMatches(3);

        assertEquals(3, player.getInjuryMatchesRemaining());

        policy.recoverPlayers(team);
        assertEquals(2, player.getInjuryMatchesRemaining());
        assertFalse(player.isAvailable());

        policy.recoverPlayers(team);
        assertEquals(1, player.getInjuryMatchesRemaining());
        assertFalse(player.isAvailable());

        policy.recoverPlayers(team);
        assertEquals(0, player.getInjuryMatchesRemaining());
        assertTrue(player.isAvailable());
    }

    @Test
    void recoverPlayersDoesNotDecreaseFreshPostMatchInjuryInSameWeek() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy(2);
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);
        FootballMatch match = matchWithLineups(home, away);
        match.setResult(0, 1);

        policy.applyPostMatch(match);
        FootballPlayer injuredPlayer = home.getFootballPlayers().stream()
                .filter(player -> player.getInjuryMatchesRemaining() > 0)
                .findFirst()
                .orElseThrow();

        assertEquals(2, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        policy.recoverPlayers(home);
        assertEquals(2, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        policy.recoverPlayers(home);
        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());
        assertFalse(injuredPlayer.isAvailable());

        policy.recoverPlayers(home);
        assertEquals(0, injuredPlayer.getInjuryMatchesRemaining());
        assertTrue(injuredPlayer.isAvailable());
    }

    @Test
    void recoverPlayersLeavesHealthyPlayersAlone() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam team = teamWithPlayers("home", "Red Hawks", 11);

        policy.recoverPlayers(team);

        for (FootballPlayer player : team.getFootballPlayers()) {
            assertEquals(0, player.getInjuryMatchesRemaining());
            assertTrue(player.isAvailable());
        }
    }

    @Test
    void rulesetRejectsLineupContainingInjuredPlayer() {
        FootballTeam team = teamWithPlayers("home", "Red Hawks", 11);
        team.getFootballPlayers().get(3).injureForMatches(2);

        List<FootballPlayer> starters = new ArrayList<>(team.getFootballPlayers());
        FootballLineup lineup = new FootballLineup(starters);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ruleset.validateLineupOrThrow(lineup)
        );
        assertTrue(ex.getMessage().contains("Unavailable"));
    }

    @Test
    void postMatchClearsStoredLineupOnInjuredTeam() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);

        FootballLineup homeLineup = new FootballLineup(home.getFootballPlayers());
        FootballLineup awayLineup = new FootballLineup(away.getFootballPlayers());
        home.assignLineup(homeLineup, ruleset);
        away.assignLineup(awayLineup, ruleset);

        FootballMatch match = new FootballMatch(home, away);
        FootballTactic tactic = new FootballTactic(
                "Default", "4-3-3", FootballTactic.Mentality.BALANCED, 50, 50
        );
        match.setHomeSetup(homeLineup, tactic);
        match.setAwaySetup(awayLineup, tactic);
        match.setResult(0, 3);

        assertNotNull(home.getSelectedFootballLineup());

        policy.applyPostMatch(match);

        assertNull(home.getSelectedFootballLineup());
        assertNotNull(away.getSelectedFootballLineup());
    }

    @Test
    void drawGivesInjuryToHomeSide() {
        FootballInjuryPolicy policy = new FootballInjuryPolicy();
        FootballTeam home = teamWithPlayers("home", "Red Hawks", 11);
        FootballTeam away = teamWithPlayers("away", "Blue Wolves", 11);
        FootballMatch match = matchWithLineups(home, away);
        match.setResult(1, 1);

        policy.applyPostMatch(match);

        assertEquals(1, countInjured(home));
        assertEquals(0, countInjured(away));
    }

    @Test
    void constructorRejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new FootballInjuryPolicy(0));
        assertThrows(IllegalArgumentException.class, () -> new FootballInjuryPolicy(-1));
    }

    private FootballMatch matchWithLineups(FootballTeam home, FootballTeam away) {
        FootballMatch match = new FootballMatch(home, away);
        FootballTactic tactic = new FootballTactic(
                "Default", "4-3-3", FootballTactic.Mentality.BALANCED, 50, 50
        );
        match.setHomeSetup(new FootballLineup(home.getFootballPlayers()), tactic);
        match.setAwaySetup(new FootballLineup(away.getFootballPlayers()), tactic);
        return match;
    }

    private int countInjured(FootballTeam team) {
        return (int) team.getFootballPlayers().stream()
                .filter(p -> p.getInjuryMatchesRemaining() > 0)
                .count();
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
