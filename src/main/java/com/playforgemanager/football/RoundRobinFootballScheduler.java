package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RoundRobinFootballScheduler implements Scheduler {

    @Override
    public List<Fixture> generateFixtures(List<? extends Team> teams) {
        List<Team> validatedTeams = validateTeams(teams);
        List<Fixture> firstLeg = generateFirstLeg(validatedTeams);
        List<Fixture> reverseFixtures = generateReverseFixtures(firstLeg);

        List<Fixture> allFixtures = new ArrayList<>(firstLeg.size() + reverseFixtures.size());
        allFixtures.addAll(firstLeg);
        allFixtures.addAll(reverseFixtures);
        return allFixtures;
    }

    private List<Team> validateTeams(List<? extends Team> teams) {
        Objects.requireNonNull(teams, "Teams cannot be null.");

        if (teams.size() < 2) {
            throw new IllegalArgumentException("At least two teams are required.");
        }

        List<Team> validatedTeams = new ArrayList<>(teams.size());
        Set<String> teamIds = new HashSet<>();

        for (Team team : teams) {
            Objects.requireNonNull(team, "Team cannot be null.");

            if (!teamIds.add(team.getId())) {
                throw new IllegalArgumentException("Duplicate team id found: " + team.getId());
            }

            validatedTeams.add(team);
        }

        return validatedTeams;
    }

    private List<Fixture> generateFirstLeg(List<Team> teams) {
        List<Team> rotation = new ArrayList<>(teams);

        if (rotation.size() % 2 != 0) {
            rotation.add(null);
        }

        int teamCount = rotation.size();
        int rounds = teamCount - 1;
        List<Team> current = new ArrayList<>(rotation);
        List<Fixture> fixtures = new ArrayList<>();

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

                fixtures.add(new Fixture(week, home, away));
            }

            Team fixedTeam = current.get(0);
            List<Team> rotatingTeams = new ArrayList<>(current.subList(1, current.size()));
            Collections.rotate(rotatingTeams, 1);

            current = new ArrayList<>();
            current.add(fixedTeam);
            current.addAll(rotatingTeams);
        }

        return fixtures;
    }

    private List<Fixture> generateReverseFixtures(List<Fixture> firstLeg) {
        int secondLegWeekOffset = firstLeg.stream()
                .mapToInt(Fixture::getWeek)
                .max()
                .orElse(0);

        List<Fixture> reverseFixtures = new ArrayList<>(firstLeg.size());

        for (Fixture fixture : firstLeg) {
            reverseFixtures.add(new Fixture(
                    fixture.getWeek() + secondLegWeekOffset,
                    fixture.getAwayTeam(),
                    fixture.getHomeTeam()
            ));
        }

        return reverseFixtures;
    }
}
