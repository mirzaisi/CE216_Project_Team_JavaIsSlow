package com.playforgemanager.core;

public interface SportFactory {
    String getSportName();
    Sport createSport();
    League createLeague(String leagueName);
    Season createSeason(League league);
}