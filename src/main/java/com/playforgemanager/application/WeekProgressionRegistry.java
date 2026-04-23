package com.playforgemanager.application;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class WeekProgressionRegistry {
    private final Map<String, WeekProgressionStrategy> strategiesBySportId;

    public WeekProgressionRegistry() {
        this.strategiesBySportId = new LinkedHashMap<>();
    }

    public WeekProgressionRegistry register(String sportId, WeekProgressionStrategy strategy) {
        String normalizedSportId = normalizeSportId(sportId);
        if (strategiesBySportId.containsKey(normalizedSportId)) {
            throw new IllegalArgumentException("A progression strategy is already registered for " + sportId + ".");
        }
        strategiesBySportId.put(
                normalizedSportId,
                Objects.requireNonNull(strategy, "Week progression strategy cannot be null.")
        );
        return this;
    }

    public WeekProgressionStrategy getStrategy(String sportId) {
        WeekProgressionStrategy strategy = strategiesBySportId.get(normalizeSportId(sportId));
        if (strategy == null) {
            throw new IllegalArgumentException("No week progression strategy registered for " + sportId + ".");
        }
        return strategy;
    }

    private String normalizeSportId(String sportId) {
        String cleaned = Objects.requireNonNull(sportId, "Sport id cannot be null.").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Sport id cannot be blank.");
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }
}
