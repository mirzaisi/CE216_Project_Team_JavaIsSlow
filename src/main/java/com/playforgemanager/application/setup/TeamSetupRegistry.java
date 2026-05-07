package com.playforgemanager.application.setup;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class TeamSetupRegistry {
    private final Map<String, TeamSetupAdapter> adapters = new LinkedHashMap<>();

    public TeamSetupRegistry register(String sportId, TeamSetupAdapter adapter) {
        String normalized = normalize(sportId);
        if (adapters.containsKey(normalized)) {
            throw new IllegalArgumentException("Team setup adapter already registered for sport id: " + sportId);
        }
        adapters.put(normalized, Objects.requireNonNull(adapter, "Team setup adapter cannot be null."));
        return this;
    }

    public TeamSetupAdapter getAdapter(String sportId) {
        TeamSetupAdapter adapter = adapters.get(normalize(sportId));
        if (adapter == null) {
            throw new IllegalArgumentException("No team setup adapter registered for sport id: " + sportId);
        }
        return adapter;
    }

    private String normalize(String sportId) {
        String cleaned = Objects.requireNonNull(sportId, "Sport id cannot be null.").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Sport id cannot be blank.");
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }
}
