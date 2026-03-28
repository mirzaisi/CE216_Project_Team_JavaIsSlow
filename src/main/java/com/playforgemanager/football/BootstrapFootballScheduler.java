package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BootstrapFootballScheduler implements Scheduler {

    @Override
    public List<Fixture> generateFixtures(List<? extends Team> teams) {
        if (teams == null || teams.size() < 2) {
            throw new IllegalArgumentException("At least two teams are required.");
        }

        List<Team> rotation = new ArrayList<>(teams);
        boolean hasBye = false;

        if (rotation.size() % 2 != 0) {
            rotation.add(null);
            hasBye = true;
        }

        int teamCount = rotation.size();
        int rounds = teamCount - 1;
        List<Fixture> firstLeg = new ArrayList<>();
        List<Team> current = new ArrayList<>(rotation);

        for (int round = 0; round < rounds; round++) {
            int week = round + 1;

            for (int i = 0; i < teamCount / 2; i++) {
                Team home = current.get(i);
                Team away = current.get(teamCount - 1 - i);

                if (home == null || away == null) {
                    continue;
                }

                if (round % 2 == 1) {
                    Team temp = home;
                    home = away;
                    away = temp;
                }

                firstLeg.add(new Fixture(week, home, away));
            }

            Team fixed = current.get(0);
            List<Team> rotating = new ArrayList<>(current.subList(1, current.size()));
            Collections.rotate(rotating, 1);

            current = new ArrayList<>();
            current.add(fixed);
            current.addAll(rotating);
        }

        List<Fixture> allFixtures = new ArrayList<>(firstLeg);
        int secondLegWeekOffset = hasBye ? rounds : rounds;

        for (Fixture fixture : firstLeg) {
            allFixtures.add(new Fixture(
                    fixture.getWeek() + secondLegWeekOffset,
                    fixture.getAwayTeam(),
                    fixture.getHomeTeam()
            ));
        }

        return allFixtures;
    }
}