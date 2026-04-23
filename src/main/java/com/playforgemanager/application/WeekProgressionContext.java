package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Match;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WeekProgressionContext {
    private final int weekNumber;
    private final List<Fixture> scheduledFixtures;
    private final Map<Fixture, Match> preparedMatches;

    public WeekProgressionContext(int weekNumber, List<Fixture> scheduledFixtures) {
        if (weekNumber < 1) {
            throw new IllegalArgumentException("Week number must be at least 1.");
        }
        this.weekNumber = weekNumber;
        this.scheduledFixtures = List.copyOf(Objects.requireNonNull(
                scheduledFixtures,
                "Scheduled fixtures cannot be null."
        ));
        this.preparedMatches = new LinkedHashMap<>();
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public List<Fixture> getScheduledFixtures() {
        return scheduledFixtures;
    }

    public void addPreparedMatch(Fixture fixture, Match match) {
        preparedMatches.put(
                Objects.requireNonNull(fixture, "Fixture cannot be null."),
                Objects.requireNonNull(match, "Match cannot be null.")
        );
    }

    public Match getPreparedMatch(Fixture fixture) {
        return preparedMatches.get(Objects.requireNonNull(fixture, "Fixture cannot be null."));
    }

    public List<Match> getPreparedMatches() {
        return List.copyOf(preparedMatches.values());
    }
}
