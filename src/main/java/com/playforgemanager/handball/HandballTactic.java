package com.playforgemanager.handball;

import com.playforgemanager.core.Tactic;

import java.util.Objects;

public final class HandballTactic implements Tactic {
    public enum Tempo {
        CONTROLLED,
        BALANCED,
        FAST_BREAK
    }

    private static final int MIN_SLIDER = 0;
    private static final int MAX_SLIDER = 100;

    private final String name;
    private final String shape;
    private final Tempo tempo;
    private final int pressureLevel;
    private final int transitionSpeed;

    public HandballTactic(String name, String shape, Tempo tempo, int pressureLevel, int transitionSpeed) {
        this.name = validateText(name, "Tactic name cannot be blank.");
        this.shape = validateText(shape, "Shape cannot be blank.");
        this.tempo = Objects.requireNonNull(tempo, "Tempo cannot be null.");
        this.pressureLevel = validateSlider("Pressure level", pressureLevel);
        this.transitionSpeed = validateSlider("Transition speed", transitionSpeed);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getShape() {
        return shape;
    }

    public Tempo getTempo() {
        return tempo;
    }

    public int getPressureLevel() {
        return pressureLevel;
    }

    public int getTransitionSpeed() {
        return transitionSpeed;
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
            throw new IllegalArgumentException(name + " must be between " + MIN_SLIDER + " and " + MAX_SLIDER + ".");
        }
        return value;
    }
}
