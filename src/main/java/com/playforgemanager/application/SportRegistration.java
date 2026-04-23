package com.playforgemanager.application;

import com.playforgemanager.core.SportFactory;

import java.util.Objects;

public final class SportRegistration {
    private final String sportId;
    private final String displayName;
    private final SportFactory sportFactory;

    public SportRegistration(String sportId, String displayName, SportFactory sportFactory) {
        this.sportId = validateText(sportId, "Sport id cannot be blank.");
        this.displayName = validateText(displayName, "Sport display name cannot be blank.");
        this.sportFactory = Objects.requireNonNull(sportFactory, "Sport factory cannot be null.");
    }

    public String getSportId() {
        return sportId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SportFactory getSportFactory() {
        return sportFactory;
    }

    private String validateText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
