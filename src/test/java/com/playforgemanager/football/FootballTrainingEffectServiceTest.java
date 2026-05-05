package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballTrainingEffectServiceTest {
    private final FootballTrainingEffectService service = new FootballTrainingEffectService();

    @Test
    void attackingFocusBoostsForwardAttackMoreThanDefenderAttack() {
        FootballTrainingPlan plan = new FootballTrainingPlan("Attacking Play", 80, 70, 65, false);
        FootballPlayer forward = createPlayer("f-1", FootballPosition.FORWARD);
        FootballPlayer defender = createPlayer("d-1", FootballPosition.DEFENDER);

        FootballTrainingEffect forwardEffect = service.buildEffect(forward, plan);
        FootballTrainingEffect defenderEffect = service.buildEffect(defender, plan);

        assertTrue(forwardEffect.attackDelta() > defenderEffect.attackDelta());
        assertTrue(forwardEffect.speedDelta() >= defenderEffect.speedDelta());
    }

    @Test
    void applyingRecoveryPlanImprovesAvailabilityAndEffectiveStamina() {
        FootballTeam team = new FootballTeam("team-1", "Recovery FC");
        FootballPlayer injuredMidfielder = createPlayer("m-1", FootballPosition.MIDFIELDER);
        injuredMidfielder.injureForMatches(2);
        team.addPlayer(injuredMidfielder);
        team.assignTrainingPlan(new FootballTrainingPlan("Recovery", 30, 25, 25, true));

        service.applyWeeklyTraining(team);

        assertEquals(1, injuredMidfielder.getInjuryMatchesRemaining());
        assertTrue(injuredMidfielder.getEffectiveAttributeProfile().getStamina()
                > injuredMidfielder.getAttributeProfile().getStamina());
    }

    @Test
    void weeklyTrainingDoesNotStackAcrossApplications() {
        FootballTeam team = new FootballTeam("team-2", "Fresh Legs United");
        FootballPlayer forward = createPlayer("f-2", FootballPosition.FORWARD);
        team.addPlayer(forward);
        team.assignTrainingPlan(new FootballTrainingPlan("Attacking", 85, 70, 70, false));

        service.applyWeeklyTraining(team);
        int firstWeekAttack = forward.getEffectiveAttributeProfile().getAttack();

        service.applyWeeklyTraining(team);
        int secondWeekAttack = forward.getEffectiveAttributeProfile().getAttack();

        assertEquals(firstWeekAttack, secondWeekAttack);
    }

    private FootballPlayer createPlayer(String id, FootballPosition position) {
        return new FootballPlayer(id, id, position, new FootballAttributeProfile(70, 70, 70, 70, 70));
    }
}
