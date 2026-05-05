package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballTrainingInjuryIntegrationTest {
    private final FootballRuleset ruleset = new FootballRuleset();
    private final FootballTrainingEffectService service = new FootballTrainingEffectService();

    @Test
    void highIntensityTrainingInjuresLowestStaminaPlayerAndClearsInvalidSelectedLineup() {
        FootballTeam team = createFullTeam("HIGH", true);
        FootballPlayer vulnerableMidfielder = findPlayer(team, "HIGH-M1");
        team.assignLineup(ruleset.buildLineup(team.getAvailablePlayers()), ruleset);
        team.assignTrainingPlan(new FootballTrainingPlan("Fitness Overload", 90, 70, 40, false));

        service.applyWeeklyTraining(team, ruleset, new FootballInjuryPolicy());

        assertEquals(2, vulnerableMidfielder.getInjuryMatchesRemaining());
        assertFalse(vulnerableMidfielder.isAvailable());
        assertNull(team.getSelectedFootballLineup());
    }

    @Test
    void recoveryTrainingCanReturnAnInjuredPlayerToASelectableState() {
        FootballTeam team = createFullTeam("REC", false);
        FootballPlayer injuredMidfielder = findPlayer(team, "REC-M1");
        injuredMidfielder.injureForMatches(1);
        team.addCoach(new FootballCoach("REC-C1", "Recovery Coach", "Head Coach", "Recovery and Rehab", 90));
        team.assignTrainingPlan(new FootballTrainingPlan("Recovery", 30, 25, 25, true));

        service.applyWeeklyTraining(team, ruleset, new FootballInjuryPolicy());

        assertEquals(0, injuredMidfielder.getInjuryMatchesRemaining());
        assertTrue(injuredMidfielder.isAvailable());
        assertDoesNotThrow(() -> ruleset.validateLineupOrThrow(ruleset.buildLineup(team.getAvailablePlayers())));
    }

    @Test
    void unavailableButNotInjuredPlayerDoesNotReceiveBoostAndStaysUnavailable() {
        FootballTeam team = createFullTeam("ABS", false);
        FootballPlayer absentForward = findPlayer(team, "ABS-F1");
        int baseAttack = absentForward.getAttributeProfile().getAttack();
        absentForward.setAvailable(false);
        team.assignTrainingPlan(new FootballTrainingPlan("Attacking Play", 85, 70, 70, false));

        service.applyWeeklyTraining(team, ruleset, new FootballInjuryPolicy());

        assertFalse(absentForward.isAvailable());
        assertEquals(baseAttack, absentForward.getEffectiveAttributeProfile().getAttack());
    }

    @Test
    void selectedLineupWithAlreadyInjuredPlayerIsRemovedBeforeTheNextMatch() {
        FootballTeam team = createFullTeam("PRE", false);
        FootballLineup selectedLineup = ruleset.buildLineup(team.getAvailablePlayers());
        FootballPlayer selectedStarter = selectedLineup.getStartingPlayers().get(0);
        team.assignLineup(selectedLineup, ruleset);
        selectedStarter.injureForMatches(2);
        team.assignTrainingPlan(new FootballTrainingPlan("Balanced", 50, 50, 50, true));

        service.applyWeeklyTraining(team, ruleset, new FootballInjuryPolicy());

        assertNull(team.getSelectedFootballLineup());
        assertFalse(selectedStarter.isAvailable());
    }

    private FootballTeam createFullTeam(String prefix, boolean lowFirstMidfielderStamina) {
        FootballTeam team = new FootballTeam(prefix + "-TEAM", prefix + " FC");
        team.addPlayer(player(prefix + "-GK1", FootballPosition.GOALKEEPER, 70));
        team.addPlayer(player(prefix + "-GK2", FootballPosition.GOALKEEPER, 70));

        for (int i = 1; i <= 5; i++) {
            team.addPlayer(player(prefix + "-D" + i, FootballPosition.DEFENDER, 70));
        }

        for (int i = 1; i <= 5; i++) {
            int stamina = lowFirstMidfielderStamina && i == 1 ? 5 : 70;
            team.addPlayer(player(prefix + "-M" + i, FootballPosition.MIDFIELDER, stamina));
        }

        for (int i = 1; i <= 3; i++) {
            team.addPlayer(player(prefix + "-F" + i, FootballPosition.FORWARD, 70));
        }

        return team;
    }

    private FootballPlayer player(String id, FootballPosition position, int stamina) {
        return new FootballPlayer(
                id,
                id,
                position,
                new FootballAttributeProfile(70, 70, stamina, 70, 70)
        );
    }

    private FootballPlayer findPlayer(FootballTeam team, String id) {
        return team.getFootballPlayers().stream()
                .filter(player -> player.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
