package com.playforgemanager.core;

import java.util.List;

public interface StandingsPolicy {
    void recordMatch(League league, Match match);
    List<Team> rankTeams(League league);
}