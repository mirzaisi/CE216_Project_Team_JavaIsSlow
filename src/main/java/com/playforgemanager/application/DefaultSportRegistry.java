package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.football.FootballSportFactory;

import java.util.Objects;

public final class DefaultSportRegistry {
    private static final int DEFAULT_TEAM_COUNT = 4;

    private DefaultSportRegistry() {
    }

    public static SportRegistry create(AssetProvider assetProvider) {
        Objects.requireNonNull(assetProvider, "Asset provider cannot be null.");

        return new SportRegistry()
                .register(new SportRegistration(
                        "football",
                        "Football",
                        new FootballSportFactory(assetProvider, DEFAULT_TEAM_COUNT)
                ));
    }
}
