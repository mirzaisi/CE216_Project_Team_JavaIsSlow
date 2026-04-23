package com.playforgemanager.application;

import com.playforgemanager.core.Team;

import java.util.Objects;

public final class TeamAvailabilityChange {
    private final Team team;
    private final int availablePlayersBefore;
    private final int availablePlayersAfter;

    public TeamAvailabilityChange(Team team, int availablePlayersBefore, int availablePlayersAfter) {
        if (availablePlayersBefore < 0 || availablePlayersAfter < 0) {
            throw new IllegalArgumentException("Available player counts cannot be negative.");
        }
        this.team = Objects.requireNonNull(team, "Team cannot be null.");
        this.availablePlayersBefore = availablePlayersBefore;
        this.availablePlayersAfter = availablePlayersAfter;
    }

    public Team getTeam() {
        return team;
    }

    public int getAvailablePlayersBefore() {
        return availablePlayersBefore;
    }

    public int getAvailablePlayersAfter() {
        return availablePlayersAfter;
    }
}
