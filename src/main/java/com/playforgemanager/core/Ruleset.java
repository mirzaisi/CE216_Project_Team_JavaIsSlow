package com.playforgemanager.core;

public interface Ruleset {
    int getWinPoints();
    int getDrawPoints();
    int getLossPoints();
    int getStartingLineupSize();
    int getBenchSize();
    boolean allowsUnlimitedSubstitutions();
    boolean isValidLineup(Lineup lineup);
}