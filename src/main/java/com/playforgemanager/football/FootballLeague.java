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

        // Adds each generated fixture to the league one by one.
        for (Fixture fixture : fixtures) {
            addFixture(fixture);
        }
    }

    public int getLastScheduledWeek() {
        int lastScheduledWeek = 0;

        // Finds the highest week number among all scheduled fixtures.
        for (Fixture fixture : getFixtures()) {
            if (fixture.getWeek() > lastScheduledWeek) {
                lastScheduledWeek = fixture.getWeek();
            }
        }

        return lastScheduledWeek;
    }
}
