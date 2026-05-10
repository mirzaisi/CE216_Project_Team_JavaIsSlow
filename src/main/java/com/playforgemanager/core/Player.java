package com.playforgemanager.core;

public abstract class Player {
    private final String id;
    private final String name;
    private boolean available;
    private int injuryMatchesRemaining;

    protected Player(String id, String name) {
        // Player identity fields must always contain valid text.
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Player id cannot be blank.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank.");
        }

        this.id = id;
        this.name = name;
        this.available = true;
        this.injuryMatchesRemaining = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available && injuryMatchesRemaining == 0;
    }

    public int getInjuryMatchesRemaining() {
        return injuryMatchesRemaining;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void injureForMatches(int matches) {
        // Injury duration must be at least one match.
        if (matches <= 0) {
            throw new IllegalArgumentException("Injury duration must be positive.");
        }

        this.injuryMatchesRemaining = matches;
        this.available = false;
    }

    public void recoverOneMatch() {
        if (injuryMatchesRemaining > 0) {
            injuryMatchesRemaining--;
        }

        // Once the injury countdown reaches zero, the player becomes available.
        if (injuryMatchesRemaining == 0) {
            available = true;
        }
    }
}
