package com.playforgemanager.ui;

public enum Screen {
    HOME("Home"),
    SPORT_SELECTION("New Game"),
    SAVE_LOAD("Save & Load"),
    TEAM_OVERVIEW("Team"),
    SQUAD("Squad"),
    TACTICS_LINEUP("Tactics & Lineup"),
    FIXTURES("Fixtures"),
    STANDINGS("League Table"),
    MATCH("Next Match"),
    POST_MATCH("Match Result"),
    POST_WEEK("Week Summary");

    private final String title;

    Screen(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
