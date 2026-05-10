package com.playforgemanager.football;

import com.playforgemanager.core.TrainingPlan;

import java.util.Locale;
import java.util.Objects;

public final class FootballTrainingPlan implements TrainingPlan {
    private static final int MIN_INTENSITY = 0;
    private static final int MAX_INTENSITY = 100;

    public enum FocusType {
        BALANCED,
        ATTACKING,
        DEFENSIVE,
        FITNESS,
        POSSESSION,
        RECOVERY
    }

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
            boolean recoveryIncluded
    ) {
        // Stores the training plan after validating all load values.
        this.focus = validateText(focus);
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

    public FocusType resolveFocusType() {
        String normalized = focus.toLowerCase(Locale.ROOT).trim();

        // Resolves the text focus into the closest football training category.
        if (normalized.contains("recover")) {
            return FocusType.RECOVERY;
        }

        if (normalized.contains("defen")
                || normalized.contains("compact")
                || normalized.contains("press resistant")) {
            return FocusType.DEFENSIVE;
        }

        if (normalized.contains("fit")
                || normalized.contains("condition")
                || normalized.contains("stamina")
                || normalized.contains("physical")) {
            return FocusType.FITNESS;
        }

        if (normalized.contains("attack")
                || normalized.contains("press")
                || normalized.contains("finish")
                || normalized.contains("counter")) {
            return FocusType.ATTACKING;
        }

        if (normalized.contains("possess")
                || normalized.contains("control")
                || normalized.contains("tactic")
                || normalized.contains("pass")) {
            return FocusType.POSSESSION;
        }

        return FocusType.BALANCED;
    }

    private String validateText(String value) {
        String cleaned = Objects.requireNonNull(value, "Training focus cannot be blank.").trim();

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Training focus cannot be blank.");
        }

        return cleaned;
    }

    private int validateLoad(String name, int value) {
        // Training load values must stay inside the shared 0-100 range.
        if (value < MIN_INTENSITY || value > MAX_INTENSITY) {
            throw new IllegalArgumentException(
                    name + " must be between " + MIN_INTENSITY + " and " + MAX_INTENSITY + "."
            );
        }

        return value;
    }
}
