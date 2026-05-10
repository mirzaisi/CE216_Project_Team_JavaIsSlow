package com.playforgemanager.application.setup;

import com.playforgemanager.football.FootballTeamSetupAdapter;
import com.playforgemanager.handball.HandballTeamSetupAdapter;

public final class DefaultTeamSetupRegistry {

    // Prevents creating objects from this utility/factory class.
    private DefaultTeamSetupRegistry() {
    }

    // Creates the default team setup registry for all supported sports.
    public static TeamSetupRegistry create() {
        return new TeamSetupRegistry()
                .register("football", new FootballTeamSetupAdapter())
                .register("handball", new HandballTeamSetupAdapter());
    }
}
