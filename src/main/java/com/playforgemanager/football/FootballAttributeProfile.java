package com.playforgemanager.football;

public final class FootballAttributeProfile {
    private static final int MIN_ATTRIBUTE = 0;
    private static final int MAX_ATTRIBUTE = 100;

    private final int attack;
    private final int defense;
    private final int stamina;
    private final int passing;
    private final int speed;

    public FootballAttributeProfile(int attack, int defense, int stamina, int passing, int speed) {
        // Validates every football attribute before storing it.
        this.attack = validateAttribute("attack", attack);
        this.defense = validateAttribute("defense", defense);
        this.stamina = validateAttribute("stamina", stamina);
        this.passing = validateAttribute("passing", passing);
        this.speed = validateAttribute("speed", speed);
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getStamina() {
        return stamina;
    }

    public int getPassing() {
        return passing;
    }

    public int getSpeed() {
        return speed;
    }

    public int getOverallRating() {
        return Math.round((attack + defense + stamina + passing + speed) / 5.0f);
    }

    private static int validateAttribute(String name, int value) {
        // Football attributes must stay inside the shared 0-100 rating range.
        if (value < MIN_ATTRIBUTE || value > MAX_ATTRIBUTE) {
            throw new IllegalArgumentException(
                    name + " must be between " + MIN_ATTRIBUTE + " and " + MAX_ATTRIBUTE + "."
            );
        }

        return value;
    }
}
