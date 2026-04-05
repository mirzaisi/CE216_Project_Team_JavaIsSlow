package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballTeamTest {

    @Test
    void addsAndRemovesPlayersFromRoster() {
        FootballTeam team = new FootballTeam("t-1", "Izmir United");
        FootballPlayer goalkeeper = createPlayer("p-1", "Goalkeeper One", FootballPosition.GOALKEEPER);
        FootballPlayer defender = createPlayer("p-2", "Defender One", FootballPosition.DEFENDER);

        team.addPlayer(goalkeeper);
        team.addPlayer(defender);

        assertEquals(2, team.getRoster().size());
        assertEquals(2, team.getFootballPlayers().size());
        assertTrue(team.getFootballPlayers().contains(goalkeeper));
        assertTrue(team.getFootballPlayers().contains(defender));

        assertTrue(team.removePlayerById("p-1"));
        assertEquals(1, team.getRoster().size());
        assertFalse(team.getFootballPlayers().contains(goalkeeper));
        assertFalse(team.removePlayerById("missing-player"));
    }

    @Test
    void removesSelectedLineupWhenRemovedPlayerWasInThatLineup() {
        FootballTeam team = createTeamWithPlayers(12);
        FootballLineup lineup = createLineupFromTeam(team, 11, 1);
        team.assignLineup(lineup);

        assertSame(lineup, team.getSelectedFootballLineup());

        FootballPlayer removedStarter = lineup.getStartingPlayers().get(0);
        assertTrue(team.removePlayerById(removedStarter.getId()));

        assertNull(team.getSelectedFootballLineup());
        assertNull(team.getSelectedLineup());
    }

    @Test
    void storesAndUpdatesSelectedTactic() {
        FootballTeam team = new FootballTeam("t-2", "Ankara City");
        FootballTactic balanced = new FootballTactic(
                "Balanced",
                "4-3-3",
                FootballTactic.Mentality.BALANCED,
                55,
                60
        );
        FootballTactic attacking = new FootballTactic(
                "High Press",
                "4-2-3-1",
                FootballTactic.Mentality.ATTACKING,
                82,
                74
        );

        team.assignTactic(balanced);
        assertSame(balanced, team.getSelectedFootballTactic());
        assertSame(balanced, team.getSelectedTactic());

        team.assignTactic(attacking);
        assertSame(attacking, team.getSelectedFootballTactic());
        assertSame(attacking, team.getSelectedTactic());
    }

    @Test
    void storesAndUpdatesSelectedLineup() {
        FootballTeam team = createTeamWithPlayers(15);
        FootballLineup firstLineup = createLineupFromTeam(team, 11, 2);
        FootballLineup updatedLineup = new FootballLineup(
                new ArrayList<>(team.getFootballPlayers().subList(1, 12)),
                List.of(team.getFootballPlayers().get(12), team.getFootballPlayers().get(13))
        );

        team.assignLineup(firstLineup);
        assertSame(firstLineup, team.getSelectedFootballLineup());
        assertSame(firstLineup, team.getSelectedLineup());

        team.assignLineup(updatedLineup);
        assertSame(updatedLineup, team.getSelectedFootballLineup());
        assertSame(updatedLineup, team.getSelectedLineup());
    }

    @Test
    void storesAndUpdatesSelectedTrainingPlan() {
        FootballTeam team = new FootballTeam("t-3", "Bursa Stars");
        FootballTrainingPlan recoveryPlan = new FootballTrainingPlan("Recovery", 35, 20, 40, true);
        FootballTrainingPlan intensePlan = new FootballTrainingPlan("Pressing", 78, 82, 71, false);

        team.assignTrainingPlan(recoveryPlan);
        assertSame(recoveryPlan, team.getSelectedFootballTrainingPlan());
        assertSame(recoveryPlan, team.getTrainingPlan());

        team.assignTrainingPlan(intensePlan);
        assertSame(intensePlan, team.getSelectedFootballTrainingPlan());
        assertSame(intensePlan, team.getTrainingPlan());
    }

    private FootballTeam createTeamWithPlayers(int playerCount) {
        FootballTeam team = new FootballTeam("team-generated", "Generated FC");
        for (int i = 0; i < playerCount; i++) {
            team.addPlayer(createPlayer(
                    "p-" + i,
                    "Player " + i,
                    choosePosition(i)
            ));
        }
        return team;
    }

    private FootballLineup createLineupFromTeam(FootballTeam team, int startersCount, int benchCount) {
        List<FootballPlayer> players = team.getFootballPlayers();
        List<FootballPlayer> starters = new ArrayList<>(players.subList(0, startersCount));
        List<FootballPlayer> bench = new ArrayList<>(players.subList(startersCount, startersCount + benchCount));
        return new FootballLineup(starters, bench);
    }

    private FootballPlayer createPlayer(String id, String name, FootballPosition position) {
        return new FootballPlayer(
                id,
                name,
                position,
                new FootballAttributeProfile(70, 70, 70, 70, 70)
        );
    }

    private FootballPosition choosePosition(int index) {
        if (index == 0) {
            return FootballPosition.GOALKEEPER;
        }
        if (index < 5) {
            return FootballPosition.DEFENDER;
        }
        if (index < 9) {
            return FootballPosition.MIDFIELDER;
        }
        return FootballPosition.FORWARD;
    }
}
