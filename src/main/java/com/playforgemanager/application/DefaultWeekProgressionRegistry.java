package com.playforgemanager.application;

import com.playforgemanager.football.FootballWeekProgressionStrategy;

public final class DefaultWeekProgressionRegistry {
    private DefaultWeekProgressionRegistry() {
    }

    public static WeekProgressionRegistry create() {
        return new WeekProgressionRegistry()
                .register("football", new FootballWeekProgressionStrategy());
    }
}
