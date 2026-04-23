package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.core.Team;

import java.util.Locale;
import java.util.Objects;

public class GameInitializationService {
    private final SportRegistry sportRegistry;

    public GameInitializationService(SportRegistry sportRegistry) {
        this.sportRegistry = Objects.requireNonNull(sportRegistry, "Sport registry cannot be null.");
    }

    public GameInitializationService(SportFactory sportFactory) {
        this(new SportRegistry().register(new SportRegistration(
                normalizeSportId(sportFactory),
                sportFactory.getSportName(),
                sportFactory
        )));
    }

    public GameSession startNewSession(String leagueName) {
        if (sportRegistry.getRegisteredSports().size() != 1) {
            throw new IllegalStateException("Sport choice is required when multiple sports are registered.");
        }

        String sportId = sportRegistry.getRegisteredSports().get(0).getSportId();
        return startNewSession(sportId, leagueName);
    }

    public GameSession startNewSession(String sportChoice, String leagueName) {
        SportRegistration registration = sportRegistry.getRegistration(sportChoice);

        if (leagueName == null || leagueName.isBlank()) {
            throw new IllegalArgumentException("League name cannot be blank.");
        }

        var sportFactory = registration.getSportFactory();
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
                ProgressionState.READY_TO_START,
                registration.getSportId()
        );
    }

    private static String normalizeSportId(SportFactory sportFactory) {
        return Objects.requireNonNull(sportFactory, "Sport factory cannot be null.")
                .getSportName()
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
