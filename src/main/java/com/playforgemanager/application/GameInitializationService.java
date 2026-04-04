package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class GameInitializationService {
    private final SportFactory sportFactory;

    public GameInitializationService(SportFactory sportFactory) {
        this.sportFactory = Objects.requireNonNull(sportFactory, "Sport factory cannot be null.");
    }

    public GameSession startNewSession(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) {
            throw new IllegalArgumentException("League name cannot be blank.");
        }

        Sport sport = sportFactory.createSport();
        League league = sportFactory.createLeague(leagueName);
        for (Fixture fixture : sport.getScheduler().generateFixtures(league.getTeams())) {
            league.addFixture(fixture);
        }
        Season season = sportFactory.createSeason(league);

        if (league.getTeams().isEmpty()) {
            throw new IllegalStateException("League must contain at least one team.");
        }

        Team controlledTeam = league.getTeams().get(0);

        return new GameSession(
                sport,
                season,
                controlledTeam,
                ProgressionState.READY_TO_START
        );
    }
}
