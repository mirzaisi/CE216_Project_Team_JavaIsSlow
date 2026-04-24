package com.playforgemanager.application.save;

import com.playforgemanager.football.FootballSaveGameRestorer;

public final class DefaultSaveGameRestorationRegistry {
    private DefaultSaveGameRestorationRegistry() {
    }

    public static SaveGameRestorationRegistry create() {
        return new SaveGameRestorationRegistry()
                .register("football", new FootballSaveGameRestorer());
    }
}
