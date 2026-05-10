package com.playforgemanager.core;

public interface InjuryPolicy {

    // Applies injury effects after a match has been played.
    void applyPostMatch(Match match);

    // Handles player recovery for the given team.
    void recoverPlayers(Team team);
}
