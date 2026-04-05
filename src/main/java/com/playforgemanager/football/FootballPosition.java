package com.playforgemanager.football;

public enum FootballPosition {
    GOALKEEPER,
    DEFENDER,
    MIDFIELDER,
    FORWARD;

    public boolean isGoalkeeper() {
        return this == GOALKEEPER;
    }
}
