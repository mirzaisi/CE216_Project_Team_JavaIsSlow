package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballTeamTest {

    @Test
    void addsAndRemovesPlayersFromRoster() {
        HandballTeam team = new HandballTeam("t-1", "Izmir Handball");
        HandballPlayer goalkeeper = createPlayer("p-1", "Goalkeeper One", HandballPosition.GOALKEEPER);
        HandballPlayer pivot = createPlayer("p-2", "Pivot One", HandballPosition.PIVOT);

        team.addPlayer(goalkeeper);
        team.addPlayer(pivot);

        assertEquals(2, team.getRoster().size());
        assertEquals(2, team.getHandballPlayers().size());
        assertTrue(team.getHandballPlayers().contains(goalkeeper));
        assertTrue(team.getHandballPlayers().contains(pivot));

        assertTrue(team.removePlayerById("p-1"));
        assertEquals(1, team.getRoster().size());
        assertFalse(team.getHandballPlayers().contains(goalkeeper));
        assertFalse(team.removePlayerById("missing-player"));
    }

    @Test
    void removesSelectedLineupWhenRemovedPlayerWasInThatLineup() {
        HandballTeam team = createTeamWithPlayers();
        HandballLineup lineup = createLineupFromTeam(team);
        team.assignLineup(lineup);

        assertSame(lineup, team.getSelectedHandballLineup());

        HandballPlayer removedStarter = lineup.getStartingPlayers().get(0);
        assertTrue(team.removePlayerById(removedStarter.getId()));

        assertNull(team.getSelectedHandballLineup());
        assertNull(team.getSelectedLineup());
    }

    @Test
    void storesSelectedTacticAndTrainingPlan() {
        HandballTeam team = new HandballTeam("t-2", "Ankara HB");
        HandballTactic tactic = new HandballTactic("Fast Break", "3-3", HandballTactic.Tempo.FAST_BREAK, 74, 88);
        HandballTrainingPlan trainingPlan = new HandballTrainingPlan("Transition", 79, 66, 70, false);

        team.assignTactic(tactic);
        team.assignTrainingPlan(trainingPlan);

        assertSame(tactic, team.getSelectedHandballTactic());
        assertSame(trainingPlan, team.getSelectedHandballTrainingPlan());
        assertSame(tactic, team.getSelectedTactic());
        assertSame(trainingPlan, team.getTrainingPlan());
    }

    @Test
    void rulesetValidatedLineupCanBeAssigned() {
        HandballRuleset ruleset = new HandballRuleset();
        HandballTeam team = createTeamWithPlayers();
        HandballLineup lineup = ruleset.buildLineup(team.getAvailablePlayers());

        team.assignLineup(lineup, ruleset);

        assertSame(lineup, team.getSelectedHandballLineup());
    }

    @Test
    void lineupContainingExternalPlayerIsRejected() {
        HandballTeam team = createTeamWithPlayers();
        List<HandballPlayer> starters = new ArrayList<>(team.getHandballPlayers().subList(0, 6));
        starters.add(createPlayer("external", "External Player", HandballPosition.PIVOT));
        HandballLineup lineup = new HandballLineup(starters);

        assertThrows(IllegalArgumentException.class, () -> team.assignLineup(lineup));
    }

    private HandballTeam createTeamWithPlayers() {
        HandballTeam team = new HandballTeam("team-generated", "Generated HB");
        team.addPlayer(createPlayer("gk-1", "Goalkeeper One", HandballPosition.GOALKEEPER));
        team.addPlayer(createPlayer("lw-1", "Left Wing One", HandballPosition.LEFT_WING));
        team.addPlayer(createPlayer("rw-1", "Right Wing One", HandballPosition.RIGHT_WING));
        team.addPlayer(createPlayer("lb-1", "Left Back One", HandballPosition.LEFT_BACK));
        team.addPlayer(createPlayer("cb-1", "Center Back One", HandballPosition.CENTER_BACK));
        team.addPlayer(createPlayer("rb-1", "Right Back One", HandballPosition.RIGHT_BACK));
        team.addPlayer(createPlayer("pv-1", "Pivot One", HandballPosition.PIVOT));
        team.addPlayer(createPlayer("gk-2", "Goalkeeper Two", HandballPosition.GOALKEEPER));
        team.addPlayer(createPlayer("lw-2", "Left Wing Two", HandballPosition.LEFT_WING));
        team.addPlayer(createPlayer("pv-2", "Pivot Two", HandballPosition.PIVOT));
        return team;
    }

    private HandballLineup createLineupFromTeam(HandballTeam team) {
        List<HandballPlayer> players = team.getHandballPlayers();
        return new HandballLineup(players.subList(0, 7), players.subList(7, 10));
    }

    private HandballPlayer createPlayer(String id, String name, HandballPosition position) {
        return new HandballPlayer(id, name, position, new HandballAttributeProfile(70, 70, 70, 70, 70));
    }
}
