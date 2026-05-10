package com.playforgemanager.application;

import com.playforgemanager.football.FootballWeekProgressionStrategy;
import com.playforgemanager.handball.HandballWeekProgressionStrategy;

public final class DefaultWeekProgressionRegistry {

    // Prevents creating objects from this utility/factory class.
    private DefaultWeekProgressionRegistry() {
    }

    // Creates the default week progression registry for all supported sports.
    public static WeekProgressionRegistry create() {
        return new WeekProgressionRegistry()
                .register("football", new FootballWeekProgressionStrategy())
                .register("handball", new HandballWeekProgressionStrategy());
    }
}
