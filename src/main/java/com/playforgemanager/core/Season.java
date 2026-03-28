package com.playforgemanager.core;

import java.util.Objects;

public abstract class Season {
    private final League league;
    private int currentWeek;
    private boolean completed;

    protected Season(League league) {
        this.league = Objects.requireNonNull(league, "League cannot be null.");
        this.currentWeek = 1;
        this.completed = false;
    }

    public League getLeague() {
        return league;
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public boolean isCompleted() {
        return completed;
    }

    public final void advanceWeek() {
        if (completed) {
            throw new IllegalStateException("Season is already completed.");
        }
        doAdvanceWeek();
    }

    protected abstract void doAdvanceWeek();

    protected final void setCurrentWeek(int currentWeek) {
        if (currentWeek < 1) {
            throw new IllegalArgumentException("Current week must be at least 1.");
        }
        this.currentWeek = currentWeek;
    }

    protected final void markCompleted() {
        this.completed = true;
    }
}