package com.playforgemanager.application.setup;

import com.playforgemanager.football.FootballTeamSetupAdapter;
import com.playforgemanager.handball.HandballTeamSetupAdapter;

public final class DefaultTeamSetupRegistry {
    private DefaultTeamSetupRegistry() {
    }

    public static TeamSetupRegistry create() {
        return new TeamSetupRegistry()
                .register("football", new FootballTeamSetupAdapter())
                .register("handball", new HandballTeamSetupAdapter());
    }
}
