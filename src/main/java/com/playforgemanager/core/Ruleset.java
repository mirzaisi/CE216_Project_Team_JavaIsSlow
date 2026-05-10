package com.playforgemanager.core;

public interface Ruleset {

    // Points awarded when a team wins a match.
    int getWinPoints();

    // Points awarded when a match ends in a draw.
    int getDrawPoints();

    // Points awarded when a team loses a match.
    int getLossPoints();

    // Number of players required in the starting lineup.
    int getStartingLineupSize();

    // Number of players allowed on the bench.
    int getBenchSize();

    // Defines whether substitutions are limited or unlimited.
    boolean allowsUnlimitedSubstitutions();

    // Checks whether the given lineup satisfies this sport's rules.
    boolean isValidLineup(Lineup lineup);
}
