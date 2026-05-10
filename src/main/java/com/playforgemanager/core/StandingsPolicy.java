package com.playforgemanager.core;

import java.util.List;

public interface StandingsPolicy {

    // Records a played match into the league standings.
    void recordMatch(League league, Match match);

    // Returns teams ordered according to the sport's standings rules.
    List<Team> rankTeams(League league);
}
