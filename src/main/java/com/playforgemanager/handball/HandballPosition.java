package com.playforgemanager.handball;

public enum HandballPosition {
    GOALKEEPER,
    LEFT_WING,
    RIGHT_WING,
    LEFT_BACK,
    CENTER_BACK,
    RIGHT_BACK,
    PIVOT;

    public boolean isGoalkeeper() {
        return this == GOALKEEPER;
    }
}
