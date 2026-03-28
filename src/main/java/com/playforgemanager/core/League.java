package com.playforgemanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class League {
    private final String name;
    private final List<Team> teams;
    private final List<Fixture> fixtures;

    protected League(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("League name cannot be blank.");
        }
        this.name = name;
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public List<Fixture> getFixtures() {
        return Collections.unmodifiableList(fixtures);
    }

    public void addTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");
        if (teams.contains(team)) {
            throw new IllegalArgumentException("Team is already in the league.");
        }
        teams.add(team);
    }

    public void addFixture(Fixture fixture) {
        Objects.requireNonNull(fixture, "Fixture cannot be null.");
        fixtures.add(fixture);
    }

    public int getTeamCount() {
        return teams.size();
    }
}