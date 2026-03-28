package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;

public class BootstrapFootballSeason extends Season {
    public BootstrapFootballSeason(League league) {
        super(league);
    }

    @Override
    protected void doAdvanceWeek() {
        int maxWeek = getLeague().getFixtures()
                .stream()
                .mapToInt(Fixture::getWeek)
                .max()
                .orElse(1);

        if (getCurrentWeek() >= maxWeek) {
            markCompleted();
        } else {
            setCurrentWeek(getCurrentWeek() + 1);
        }
    }
}