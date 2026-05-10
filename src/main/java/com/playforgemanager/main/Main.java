package com.playforgemanager.main;

import com.playforgemanager.application.DefaultSportRegistry;
import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;

import java.util.List;

public class Main {
    private static final String DEMO_LEAGUE_NAME = "PlayForge Demo League";

    public static void main(String[] args) {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportRegistry sportRegistry = DefaultSportRegistry.create(assetProvider);
        GameInitializationService initializationService = new GameInitializationService(sportRegistry);

        GameSession session = initializationService.startNewSession(
                resolveSportChoice(args, sportRegistry),
                DEMO_LEAGUE_NAME
        );

        GameSessionConsoleSummary summary = new GameSessionConsoleSummary();

        summary.print(session);
    }

    private static String resolveSportChoice(String[] args, SportRegistry sportRegistry) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return args[0];
        }

        List<SportRegistration> sports = sportRegistry.getRegisteredSports();

        // Falls back to the first registered sport when no command-line choice is provided.
        if (sports.isEmpty()) {
            throw new IllegalStateException("At least one sport must be registered.");
        }

        return sports.get(0).getSportId();
    }
}
