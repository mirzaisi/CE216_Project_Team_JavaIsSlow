package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballDataObjectsTest {

    @Test
    void handballCoachStoresItsCoachingData() {
        HandballCoach coach = new HandballCoach(
                "hc-1",
                "Deniz Yildiz",
                "Head Coach",
                HandballCoachSpecialization.DEFENSE,
                82
        );

        assertEquals("Deniz Yildiz", coach.getName());
        assertEquals("Head Coach", coach.getRole());
        assertEquals(HandballCoachSpecialization.DEFENSE, coach.getSpecialization());
        assertEquals(82, coach.getCoachingRating());
        assertEquals(82, coach.getRating());
    }

    @Test
    void handballAttributeProfileCalculatesOverallRating() {
        HandballAttributeProfile profile = new HandballAttributeProfile(78, 66, 70, 74, 52);

        assertEquals(78, profile.getShooting());
        assertEquals(66, profile.getDefense());
        assertEquals(68, profile.getOverallRating());
    }

    @Test
    void handballLineupStoresStartingAndBenchPlayersSeparately() {
        HandballPlayer goalkeeper = createPlayer("p-1", "Goalkeeper", HandballPosition.GOALKEEPER);
        HandballPlayer leftWing = createPlayer("p-2", "Left Wing", HandballPosition.LEFT_WING);
        HandballPlayer pivot = createPlayer("p-3", "Pivot", HandballPosition.PIVOT);

        HandballLineup lineup = new HandballLineup(
                List.of(goalkeeper, leftWing),
                List.of(pivot)
        );

        assertEquals(2, lineup.size());
        assertEquals(List.of(goalkeeper, leftWing), lineup.getStartingPlayers());
        assertEquals(List.of(pivot), lineup.getBenchPlayers());
        assertEquals(3, lineup.getAllPlayers().size());
        assertTrue(lineup.containsPlayerId("p-3"));
    }

    @Test
    void handballTacticStoresItsTacticalChoices() {
        HandballTactic tactic = new HandballTactic(
                "Fast Break",
                "3-3",
                HandballTactic.Tempo.FAST_BREAK,
                72,
                84
        );

        assertEquals("Fast Break", tactic.getName());
        assertEquals("3-3", tactic.getShape());
        assertEquals(HandballTactic.Tempo.FAST_BREAK, tactic.getTempo());
        assertEquals(72, tactic.getPressureLevel());
        assertEquals(84, tactic.getTransitionSpeed());
    }

    @Test
    void handballTrainingPlanStoresItsTrainingSetup() {
        HandballTrainingPlan plan = new HandballTrainingPlan("Transition", 74, 62, 68, true);

        assertEquals("Transition", plan.getFocus());
        assertEquals(74, plan.getIntensity());
        assertEquals(62, plan.getConditioningLoad());
        assertEquals(68, plan.getTacticalLoad());
        assertTrue(plan.isRecoveryIncluded());
    }

    private HandballPlayer createPlayer(String id, String name, HandballPosition position) {
        return new HandballPlayer(
                id,
                name,
                position,
                new HandballAttributeProfile(65, 65, 65, 65, 65)
        );
    }
}
