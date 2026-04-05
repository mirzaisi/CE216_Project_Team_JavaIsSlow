package com.playforgemanager.football;

/**
 * Football-specific position model kept inside the football module.
 */
public enum FootballPosition {
    GOALKEEPER,
    DEFENDER,
    MIDFIELDER,
    FORWARD;

    public boolean isGoalkeeper() {
        return this == GOALKEEPER;
    }
}
