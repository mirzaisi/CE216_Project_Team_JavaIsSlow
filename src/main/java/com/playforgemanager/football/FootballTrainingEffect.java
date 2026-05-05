package com.playforgemanager.football;

public record FootballTrainingEffect(
        int attackDelta,
        int defenseDelta,
        int staminaDelta,
        int passingDelta,
        int speedDelta,
        boolean acceleratedRecovery
) {
    public FootballTrainingEffect {
        if (attackDelta < 0 || defenseDelta < 0 || staminaDelta < 0 || passingDelta < 0 || speedDelta < 0) {
            throw new IllegalArgumentException("Training effect deltas cannot be negative.");
        }
    }

    public static FootballTrainingEffect none() {
        return new FootballTrainingEffect(0, 0, 0, 0, 0, false);
    }
}
