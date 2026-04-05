package com.playforgemanager.football;

import com.playforgemanager.core.Tactic;

import java.util.Objects;

/**
 * Football-side match tactic object used before a match starts.
 */
public final class FootballTactic implements Tactic {
    public enum Mentality {
        DEFENSIVE,
        BALANCED,
        ATTACKING
    }

    private static final int MIN_SLIDER = 0;
    private static final int MAX_SLIDER = 100;

    private final String name;
    private final String formation;
    private final Mentality mentality;
    private final int pressingIntensity;
    private final int attackingWidth;

    public FootballTactic(
            String name,
            String formation,
            Mentality mentality,
            int pressingIntensity,
            int attackingWidth) {
        this.name = validateText(name, "Tactic name cannot be blank.");
        this.formation = validateText(formation, "Formation cannot be blank.");
        this.mentality = Objects.requireNonNull(mentality, "Mentality cannot be null.");
        this.pressingIntensity = validateSlider("Pressing intensity", pressingIntensity);
        this.attackingWidth = validateSlider("Attacking width", attackingWidth);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFormation() {
        return formation;
    }

    public Mentality getMentality() {
        return mentality;
    }

    public int getPressingIntensity() {
        return pressingIntensity;
    }

    public int getAttackingWidth() {
        return attackingWidth;
    }

    private String validateText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }

    private int validateSlider(String name, int value) {
        if (value < MIN_SLIDER || value > MAX_SLIDER) {
            throw new IllegalArgumentException(
                    name + " must be between " + MIN_SLIDER + " and " + MAX_SLIDER + ".");
        }
        return value;
    }
}
