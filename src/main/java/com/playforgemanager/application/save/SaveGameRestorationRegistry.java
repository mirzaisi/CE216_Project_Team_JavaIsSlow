package com.playforgemanager.application.save;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SaveGameRestorationRegistry {
    private final Map<String, SaveGameRestorer> restorersBySportId = new LinkedHashMap<>();

    public SaveGameRestorationRegistry register(String sportId, SaveGameRestorer restorer) {
        String normalizedSportId = normalize(sportId);
        if (restorersBySportId.containsKey(normalizedSportId)) {
            throw new IllegalArgumentException("A save restorer is already registered for sport: " + sportId);
        }
        restorersBySportId.put(
                normalizedSportId,
                Objects.requireNonNull(restorer, "Save restorer cannot be null.")
        );
        return this;
    }

    public SaveGameRestorer getRestorer(String sportId) {
        String normalizedSportId = normalize(sportId);
        SaveGameRestorer restorer = restorersBySportId.get(normalizedSportId);
        if (restorer == null) {
            throw new IllegalArgumentException("No save restorer registered for sport: " + sportId);
        }
        return restorer;
    }

    private String normalize(String sportId) {
        String cleaned = Objects.requireNonNull(sportId, "Sport id cannot be null.").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Sport id cannot be blank.");
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }
}
