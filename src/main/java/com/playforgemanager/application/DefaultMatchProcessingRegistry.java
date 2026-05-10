package com.playforgemanager.application;

import com.playforgemanager.football.FootballMatchProcessingStrategy;
import com.playforgemanager.handball.HandballMatchProcessingStrategy;

public final class DefaultMatchProcessingRegistry {

    // Prevents creating objects from this utility/factory class.
    private DefaultMatchProcessingRegistry() {
    }

    // Creates the default match processing registry for all supported sports.
    public static MatchProcessingRegistry create() {
        return new MatchProcessingRegistry()
                .register("football", new FootballMatchProcessingStrategy())
                .register("handball", new HandballMatchProcessingStrategy());
    }
}
