package com.playforgemanager.core;

public interface Sport {
    String getName();
    Ruleset getRuleset();
    Scheduler getScheduler();
    StandingsPolicy getStandingsPolicy();
    MatchEngine getMatchEngine();
    InjuryPolicy getInjuryPolicy();
}