package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballDataObjectsTest {

    @Test
    void footballLineupStoresStartingAndBenchPlayersSeparately() {
        FootballPlayer goalkeeper = createPlayer("p-1", "Goalkeeper", FootballPosition.GOALKEEPER);
        FootballPlayer defender = createPlayer("p-2", "Defender", FootballPosition.DEFENDER);
        FootballPlayer midfielder = createPlayer("p-3", "Midfielder", FootballPosition.MIDFIELDER);

        FootballLineup lineup = new FootballLineup(
                List.of(goalkeeper, defender),
                List.of(midfielder)
        );

        assertEquals(2, lineup.size());
        assertEquals(List.of(goalkeeper, defender), lineup.getStartingPlayers());
        assertEquals(List.of(midfielder), lineup.getBenchPlayers());
        assertEquals(3, lineup.getAllPlayers().size());
        assertTrue(lineup.containsPlayerId("p-3"));
    }

    @Test
    void footballTacticStoresItsTacticalChoices() {
        FootballTactic tactic = new FootballTactic(
                "Control Possession",
                "4-1-4-1",
                FootballTactic.Mentality.BALANCED,
                63,
                72
        );

        assertEquals("Control Possession", tactic.getName());
        assertEquals("4-1-4-1", tactic.getFormation());
        assertEquals(FootballTactic.Mentality.BALANCED, tactic.getMentality());
        assertEquals(63, tactic.getPressingIntensity());
        assertEquals(72, tactic.getAttackingWidth());
    }

    @Test
    void footballTrainingPlanStoresItsTrainingSetup() {
        FootballTrainingPlan plan = new FootballTrainingPlan("Finishing", 68, 50, 61, true);

        assertEquals("Finishing", plan.getFocus());
        assertEquals(68, plan.getIntensity());
        assertEquals(50, plan.getConditioningLoad());
        assertEquals(61, plan.getTacticalLoad());
        assertTrue(plan.isRecoveryIncluded());
    }

    private FootballPlayer createPlayer(String id, String name, FootballPosition position) {
        return new FootballPlayer(
                id,
                name,
                position,
                new FootballAttributeProfile(65, 65, 65, 65, 65)
        );
    }
}
