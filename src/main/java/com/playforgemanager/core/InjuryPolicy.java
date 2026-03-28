package com.playforgemanager.core;

public interface InjuryPolicy {
    void applyPostMatch(Match match);
    void recoverPlayers(Team team);
}