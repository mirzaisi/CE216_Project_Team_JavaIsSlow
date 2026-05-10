package com.playforgemanager.core;

public interface Sport {

    // Returns the display name of the sport.
    String getName();

    // Provides the rules used by this sport.
    Ruleset getRuleset();

    // Provides fixture scheduling behavior for this sport.
    Scheduler getScheduler();

    // Provides league table ranking behavior for this sport.
    StandingsPolicy getStandingsPolicy();

    // Provides match simulation behavior for this sport.
    MatchEngine getMatchEngine();

    // Provides injury and recovery behavior for this sport.
    InjuryPolicy getInjuryPolicy();
}
