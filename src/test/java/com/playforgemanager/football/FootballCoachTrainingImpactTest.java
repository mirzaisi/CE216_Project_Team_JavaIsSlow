package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballCoachTrainingImpactTest {
    private final FootballTrainingEffectService service = new FootballTrainingEffectService();

    @Test
    void matchingCoachSpecializationBoostsRelevantTrainingOutcome() {
        FootballPlayer forward = createPlayer("f-1", FootballPosition.FORWARD);
        FootballTrainingPlan attackingPlan = new FootballTrainingPlan("Attacking Play", 75, 60, 60, false);

        FootballTrainingEffect withoutCoach = service.buildEffect(forward, attackingPlan, List.of());
        FootballTrainingEffect withAttackingCoach = service.buildEffect(
                forward,
                attackingPlan,
                List.of(new FootballCoach("c-1", "Attack Coach", "Coach", "Attacking Play", 90))
        );
        FootballTrainingEffect withDefensiveCoach = service.buildEffect(
                forward,
                attackingPlan,
                List.of(new FootballCoach("c-2", "Defense Coach", "Coach", "Defensive Organization", 90))
        );

        assertTrue(withAttackingCoach.attackDelta() > withoutCoach.attackDelta());
        assertEquals(withoutCoach.attackDelta(), withDefensiveCoach.attackDelta());
    }

    @Test
    void coachRatingChangesSizeOfMatchingTrainingEffect() {
        FootballPlayer midfielder = createPlayer("m-1", FootballPosition.MIDFIELDER);
        FootballTrainingPlan possessionPlan = new FootballTrainingPlan("Possession Control", 65, 60, 75, false);

        FootballTrainingEffect lowRatedCoach = service.buildEffect(
                midfielder,
                possessionPlan,
                List.of(new FootballCoach("c-low", "Low Coach", "Coach", "Possession Play", 50))
        );
        FootballTrainingEffect eliteCoach = service.buildEffect(
                midfielder,
                possessionPlan,
                List.of(new FootballCoach("c-high", "Elite Coach", "Coach", "Possession Play", 90))
        );

        assertTrue(eliteCoach.passingDelta() > lowRatedCoach.passingDelta());
        assertTrue(eliteCoach.staminaDelta() >= lowRatedCoach.staminaDelta());
    }

    @Test
    void generalCoachGivesSmallSupportButLessThanSpecialist() {
        FootballPlayer defender = createPlayer("d-1", FootballPosition.DEFENDER);
        FootballTrainingPlan defensivePlan = new FootballTrainingPlan("Defensive Organization", 75, 60, 60, false);

        FootballTrainingEffect generalCoach = service.buildEffect(
                defender,
                defensivePlan,
                List.of(new FootballCoach("c-general", "General Coach", "Coach", "General Management", 90))
        );
        FootballTrainingEffect specialistCoach = service.buildEffect(
                defender,
                defensivePlan,
                List.of(new FootballCoach("c-defense", "Defense Coach", "Coach", "Defensive Organization", 90))
        );

        assertTrue(generalCoach.defenseDelta() > service.buildEffect(defender, defensivePlan).defenseDelta());
        assertTrue(specialistCoach.defenseDelta() > generalCoach.defenseDelta());
    }

    @Test
    void eliteRecoveryCoachAddsOneExtraDeterministicRecoveryStep() {
        FootballTeam team = new FootballTeam("team-1", "Recovery FC");
        FootballPlayer injuredPlayer = createPlayer("m-2", FootballPosition.MIDFIELDER);
        injuredPlayer.injureForMatches(3);
        team.addPlayer(injuredPlayer);
        team.addCoach(new FootballCoach("c-recovery", "Recovery Coach", "Coach", "Recovery and Rehab", 90));
        team.assignTrainingPlan(new FootballTrainingPlan("Recovery", 35, 35, 35, true));

        service.applyWeeklyTraining(team);

        assertEquals(1, injuredPlayer.getInjuryMatchesRemaining());
    }

    private FootballPlayer createPlayer(String id, FootballPosition position) {
        return new FootballPlayer(id, id, position, new FootballAttributeProfile(70, 70, 70, 70, 70));
    }
}
