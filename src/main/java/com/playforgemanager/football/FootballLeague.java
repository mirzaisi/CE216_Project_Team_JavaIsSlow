package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;

import java.util.List;
import java.util.Objects;

public class FootballLeague extends League {

    public FootballLeague(String name) {
        super(name);
    }

    public void addFixtures(List<Fixture> fixtures) {
        Objects.requireNonNull(fixtures, "Fixtures cannot be null.");
        fixtures.forEach(this::addFixture);
    }

    public List<Fixture> getFixturesForWeek(int week) {
        if (week < 1) {
            throw new IllegalArgumentException("Week must be at least 1.");
        }

        return getFixtures().stream()
                .filter(fixture -> fixture.getWeek() == week)
                .toList();
    }

    public boolean hasFixturesForWeek(int week) {
        if (week < 1) {
            throw new IllegalArgumentException("Week must be at least 1.");
        }

        return getFixtures().stream().anyMatch(fixture -> fixture.getWeek() == week);
    }

    public int getLastScheduledWeek() {
        return getFixtures().stream()
                .mapToInt(Fixture::getWeek)
                .max()
                .orElse(0);
    }
}
