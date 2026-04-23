package com.playforgemanager.main;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class GameSessionConsoleSummary {

    public void print(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        League league = validatedSession.getCurrentSeason().getLeague();

        System.out.println("=== PlayForge Manager ===");
        System.out.println("Sport: " + validatedSession.getActiveSport().getName());
        System.out.println("League: " + league.getName());
        System.out.println("Controlled Team: " + validatedSession.getControlledTeam().getName());
        System.out.println();

        System.out.println("Generated Teams");
        System.out.println("---------------");
        for (Team team : league.getTeams()) {
            System.out.printf(
                    "- %-15s | Players: %-2d%n",
                    team.getName(),
                    team.getRoster().size()
            );
        }

        System.out.println();
        System.out.println("Total fixtures generated: " + league.getFixtures().size());
        System.out.println("Current week: " + validatedSession.getCurrentSeason().getCurrentWeek());
        System.out.println("Progression state: " + validatedSession.getProgressionState());
    }
}
