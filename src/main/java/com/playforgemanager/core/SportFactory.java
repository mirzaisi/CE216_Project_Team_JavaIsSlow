package com.playforgemanager.core;

public interface SportFactory {

    // Returns the display name of the sport this factory creates.
    String getSportName();

    // Creates the sport module with its rules, scheduler, standings, and policies.
    Sport createSport();

    // Creates a league structure for the given league name.
    League createLeague(String leagueName);

    // Creates a season using the provided league.
    Season createSeason(League league);
}
