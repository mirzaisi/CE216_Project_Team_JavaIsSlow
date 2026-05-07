package com.playforgemanager.application;

import com.playforgemanager.football.FootballMatchProcessingStrategy;
import com.playforgemanager.handball.HandballMatchProcessingStrategy;

public final class DefaultMatchProcessingRegistry {
    private DefaultMatchProcessingRegistry() {
    }

    public static MatchProcessingRegistry create() {
        return new MatchProcessingRegistry()
                .register("football", new FootballMatchProcessingStrategy())
                .register("handball", new HandballMatchProcessingStrategy());
    }
}
