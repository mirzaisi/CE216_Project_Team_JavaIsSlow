package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.handball.HandballSportFactory;

import java.util.Objects;

public final class DefaultSportRegistry {
    private static final int DEFAULT_TEAM_COUNT = 4;

    // Prevents creating objects from this utility/factory class.
    private DefaultSportRegistry() {
    }

    // Creates the default sport registry with all supported sport modules.
    public static SportRegistry create(AssetProvider assetProvider) {
        Objects.requireNonNull(assetProvider, "Asset provider cannot be null.");

        return new SportRegistry()
                .register(new SportRegistration(
                        "football",
                        "Football",
                        new FootballSportFactory(assetProvider, DEFAULT_TEAM_COUNT)
                ))
                .register(new SportRegistration(
                        "handball",
                        "Handball",
                        new HandballSportFactory(assetProvider, DEFAULT_TEAM_COUNT)
                ));
    }
}
