package com.playforgemanager.football;

import com.playforgemanager.core.Coach;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BootstrapFootballTeam extends Team {
    private final List<Coach> coaches;

    public BootstrapFootballTeam(String id, String name) {
        super(id, name);
        this.coaches = new ArrayList<>();
    }

    public void addCoach(Coach coach) {
        coaches.add(Objects.requireNonNull(coach, "Coach cannot be null."));
    }

    public List<Coach> getCoaches() {
        return Collections.unmodifiableList(coaches);
    }
}