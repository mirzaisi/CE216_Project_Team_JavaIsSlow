package com.playforgemanager.core;

import java.util.Objects;

public class GameSession {
    private final Sport activeSport;
    private final Season currentSeason;
    private final Team controlledTeam;
    private ProgressionState progressionState;

    public GameSession(
            Sport activeSport,
            Season currentSeason,
            Team controlledTeam,
            ProgressionState progressionState
    ) {
        this.activeSport = Objects.requireNonNull(activeSport, "Active sport cannot be null.");
        this.currentSeason = Objects.requireNonNull(currentSeason, "Current season cannot be null.");
        this.controlledTeam = Objects.requireNonNull(controlledTeam, "Controlled team cannot be null.");
        this.progressionState = Objects.requireNonNull(progressionState, "Progression state cannot be null.");
    }

    public Sport getActiveSport() {
        return activeSport;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public Team getControlledTeam() {
        return controlledTeam;
    }

    public ProgressionState getProgressionState() {
        return progressionState;
    }

    public void setProgressionState(ProgressionState progressionState) {
        this.progressionState = Objects.requireNonNull(progressionState, "Progression state cannot be null.");
    }

    public void markInProgress() {
        this.progressionState = ProgressionState.IN_PROGRESS;
    }

    public void markCompleted() {
        this.progressionState = ProgressionState.COMPLETED;
    }
}