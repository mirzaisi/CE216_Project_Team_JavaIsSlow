package com.playforgemanager.handball;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;

import java.util.List;
import java.util.Objects;

public class HandballLeague extends League {

    public HandballLeague(String name) {
        super(name);
    }

    public void addFixtures(List<Fixture> fixtures) {
        Objects.requireNonNull(fixtures, "Fixtures cannot be null.");

        // Adds each generated fixture to the league.
        fixtures.forEach(this::addFixture);
    }
}
