package com.playforgemanager.application.save;

import com.playforgemanager.football.FootballSaveGameRestorer;
import com.playforgemanager.handball.HandballSaveGameRestorer;

public final class DefaultSaveGameRestorationRegistry {

    // Prevents creating objects from this utility/factory class.
    private DefaultSaveGameRestorationRegistry() {
    }

    // Creates the default registry and connects each sport to its save-game restorer.
    public static SaveGameRestorationRegistry create() {
        return new SaveGameRestorationRegistry()
                .register("football", new FootballSaveGameRestorer())
                .register("handball", new HandballSaveGameRestorer());
    }
}
