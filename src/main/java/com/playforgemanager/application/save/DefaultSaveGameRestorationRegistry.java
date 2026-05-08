package com.playforgemanager.application.save;

import com.playforgemanager.football.FootballSaveGameRestorer;
import com.playforgemanager.handball.HandballSaveGameRestorer;

public final class DefaultSaveGameRestorationRegistry {
    private DefaultSaveGameRestorationRegistry() {
    }

    public static SaveGameRestorationRegistry create() {
        return new SaveGameRestorationRegistry()
                .register("football", new FootballSaveGameRestorer())
                .register("handball", new HandballSaveGameRestorer());
    }
}
