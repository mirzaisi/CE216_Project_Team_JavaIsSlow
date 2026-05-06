package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballTrainingCoachRegressionTest {
    private final FootballTrainingEffectService trainingService = new FootballTrainingEffectService();

    @Test
    void attackingTrainingAndMatchingCoachProduceStrongerForwardEffect() {
        FootballPlayer forward = new FootballPlayer(
                "forward-1",
                "Forward One",
                FootballPosition.FORWARD,
                new FootballAttributeProfile(78, 45, 72, 69, 81)
        );
        FootballTrainingPlan plan = FootballRegressionTestSupport.attackingTraining();

        FootballTrainingEffect withoutCoach = trainingService.buildEffect(forward, plan, List.of());
        FootballTrainingEffect withMatchingCoach = trainingService.buildEffect(
                forward,
                plan,
                List.of(FootballRegressionTestSupport.coach("attack", "Attacking Play", 90))
        );

        assertTrue(withMatchingCoach.attackDelta() > withoutCoach.attackDelta());
        assertTrue(withMatchingCoach.passingDelta() >= withoutCoach.passingDelta());
        assertTrue(withMatchingCoach.speedDelta() >= withoutCoach.speedDelta());
    }

    @Test
    void weeklyTeamTrainingAppliesCoachInfluencedEffectsToPlayers() {
        FootballTeam team = FootballRegressionTestSupport.team(
                "coach-team",
                "Coach Team",
                70,
                FootballRegressionTestSupport.attackingTactic("High Press"),
                FootballRegressionTestSupport.attackingTraining(),
                FootballRegressionTestSupport.coach("coach-1", "Attacking Play", 91)
        );
        FootballPlayer forward = (FootballPlayer) team.getPlayersByPosition(FootballPosition.FORWARD).get(0);

        assertEquals(FootballTrainingEffect.none(), forward.getWeeklyTrainingEffect());

        trainingService.applyWeeklyTraining(team, FootballRegressionTestSupport.ruleset(), new FootballInjuryPolicy());

        assertNotEquals(FootballTrainingEffect.none(), forward.getWeeklyTrainingEffect());
        assertTrue(forward.getWeeklyTrainingEffect().attackDelta() > 0);
        assertTrue(forward.getEffectiveAttributeProfile().getAttack() > forward.getAttributeProfile().getAttack());
    }

    @Test
    void recoveryCoachImprovesInjuredPlayerRecoveryDuringTraining() {
        FootballTeam team = FootballRegressionTestSupport.team(
                "recovery-team",
                "Recovery Team",
                68,
                FootballRegressionTestSupport.balancedTactic("Balanced"),
                FootballRegressionTestSupport.recoveryTraining(),
                FootballRegressionTestSupport.coach("recovery-coach", "Recovery and Rehab", 90)
        );
        FootballPlayer injuredPlayer = (FootballPlayer) team.getFootballPlayers().get(0);
        injuredPlayer.injureForMatches(3);

        trainingService.applyWeeklyTraining(team, FootballRegressionTestSupport.ruleset(), new FootballInjuryPolicy());

        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());
        assertTrue(injuredPlayer.getWeeklyTrainingEffect().acceleratedRecovery());
    }
}
