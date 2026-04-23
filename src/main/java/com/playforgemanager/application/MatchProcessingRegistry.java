package com.playforgemanager.application;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class MatchProcessingRegistry {
    private final Map<String, MatchProcessingStrategy> strategiesBySportId;

    public MatchProcessingRegistry() {
        this.strategiesBySportId = new LinkedHashMap<>();
    }

    public MatchProcessingRegistry register(String sportId, MatchProcessingStrategy strategy) {
        String normalizedSportId = normalizeSportId(sportId);
        if (strategiesBySportId.containsKey(normalizedSportId)) {
            throw new IllegalArgumentException("A match processing strategy is already registered for " + sportId + ".");
        }
        strategiesBySportId.put(
                normalizedSportId,
                Objects.requireNonNull(strategy, "Match processing strategy cannot be null.")
        );
        return this;
    }

    public MatchProcessingStrategy getStrategy(String sportId) {
        MatchProcessingStrategy strategy = strategiesBySportId.get(normalizeSportId(sportId));
        if (strategy == null) {
            throw new IllegalArgumentException("No match processing strategy registered for " + sportId + ".");
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
