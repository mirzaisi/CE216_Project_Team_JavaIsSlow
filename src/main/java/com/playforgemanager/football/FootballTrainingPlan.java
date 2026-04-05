package com.playforgemanager.football;

import com.playforgemanager.core.TrainingPlan;

import java.util.Objects;

/**
 * Football-side training setup selected for the current week.
 */
public final class FootballTrainingPlan implements TrainingPlan {
    private static final int MIN_INTENSITY = 0;
    private static final int MAX_INTENSITY = 100;

    private final String focus;
    private final int intensity;
    private final int conditioningLoad;
    private final int tacticalLoad;
    private final boolean recoveryIncluded;

    public FootballTrainingPlan(
            String focus,
            int intensity,
            int conditioningLoad,
            int tacticalLoad,
            boolean recoveryIncluded) {
        this.focus = validateText(focus, "Training focus cannot be blank.");
        this.intensity = validateLoad("Intensity", intensity);
        this.conditioningLoad = validateLoad("Conditioning load", conditioningLoad);
        this.tacticalLoad = validateLoad("Tactical load", tacticalLoad);
        this.recoveryIncluded = recoveryIncluded;
    }

    @Override
    public String getFocus() {
        return focus;
    }

    @Override
    public int getIntensity() {
        return intensity;
    }

    public int getConditioningLoad() {
        return conditioningLoad;
    }

    public int getTacticalLoad() {
        return tacticalLoad;
    }

    public boolean isRecoveryIncluded() {
        return recoveryIncluded;
    }

    private String validateText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }

    private int validateLoad(String name, int value) {
        if (value < MIN_INTENSITY || value > MAX_INTENSITY) {
            throw new IllegalArgumentException(
                    name + " must be between " + MIN_INTENSITY + " and " + MAX_INTENSITY + ".");
        }
        return value;
    }
}
