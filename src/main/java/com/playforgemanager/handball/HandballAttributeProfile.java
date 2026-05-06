package com.playforgemanager.handball;

public final class HandballAttributeProfile {
    private static final int MIN_ATTRIBUTE = 0;
    private static final int MAX_ATTRIBUTE = 100;

    private final int shooting;
    private final int defense;
    private final int passing;
    private final int speed;
    private final int reflexes;

    public HandballAttributeProfile(int shooting, int defense, int passing, int speed, int reflexes) {
        this.shooting = validateAttribute("shooting", shooting);
        this.defense = validateAttribute("defense", defense);
        this.passing = validateAttribute("passing", passing);
        this.speed = validateAttribute("speed", speed);
        this.reflexes = validateAttribute("reflexes", reflexes);
    }

    public int getShooting() {
        return shooting;
    }

    public int getDefense() {
        return defense;
    }

    public int getPassing() {
        return passing;
    }

    public int getSpeed() {
        return speed;
    }

    public int getReflexes() {
        return reflexes;
    }

    public int getOverallRating() {
        return Math.round((shooting + defense + passing + speed + reflexes) / 5.0f);
    }

    private int validateAttribute(String name, int value) {
        if (value < MIN_ATTRIBUTE || value > MAX_ATTRIBUTE) {
            throw new IllegalArgumentException(name + " must be between " + MIN_ATTRIBUTE + " and " + MAX_ATTRIBUTE + ".");
        }
        return value;
    }
}
