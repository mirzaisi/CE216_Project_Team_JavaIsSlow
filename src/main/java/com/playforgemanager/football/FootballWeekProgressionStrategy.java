package com.playforgemanager.football;

import com.playforgemanager.application.WeekProgressionContext;
import com.playforgemanager.application.WeekProgressionStrategy;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Team;

import java.util.List;
import java.util.Objects;

public class FootballWeekProgressionStrategy implements WeekProgressionStrategy {

    @Override
    public WeekProgressionContext createContext(GameSession session) {
        FootballSeason season = requireSeason(session);
        if (season.isCompleted()) {
            throw new IllegalStateException("Season is already completed.");
        }

        League league = season.getLeague();
        if (league.getFixtures().isEmpty()) {
            throw new IllegalStateException("Season has no scheduled fixtures.");
        }

        List<Fixture> currentWeekFixtures = season.getCurrentWeekFixtures();
        if (currentWeekFixtures.isEmpty()) {
            throw new IllegalStateException("No fixtures scheduled for week " + season.getCurrentWeek() + ".");
        }

        return new WeekProgressionContext(season.getCurrentWeek(), currentWeekFixtures);
    }

    @Override
    public void prepareMatches(GameSession session, WeekProgressionContext context) {
        FootballSeason season = requireSeason(session);
        Sport sport = Objects.requireNonNull(session, "Game session cannot be null.").getActiveSport();

        for (Fixture fixture : context.getScheduledFixtures()) {
            if (fixture.isPlayed()) {
                continue;
            }

            Match match = new FootballMatch(fixture.getHomeTeam(), fixture.getAwayTeam());
            season.prepareMatch(match, sport);
            context.addPreparedMatch(fixture, match);
        }
    }

    @Override
    public void simulateMatches(GameSession session, WeekProgressionContext context) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Sport sport = validatedSession.getActiveSport();
        League league = validatedSession.getCurrentSeason().getLeague();

        for (Fixture fixture : context.getScheduledFixtures()) {
            if (fixture.isPlayed()) {
                continue;
            }

            Match match = Objects.requireNonNull(
                    context.getPreparedMatch(fixture),
                    "Prepared match cannot be null."
            );
            sport.getMatchEngine().simulate(match, sport.getRuleset());
            fixture.attachPlayedMatch(match);
            sport.getStandingsPolicy().recordMatch(league, match);
        }
    }

    @Override
    public void refreshStandings(GameSession session, WeekProgressionContext context) {
        FootballSeason season = requireSeason(session);
        season.refreshStandings(session.getActiveSport().getStandingsPolicy());
    }

    @Override
    public void processPostMatch(GameSession session, WeekProgressionContext context) {
        Sport sport = Objects.requireNonNull(session, "Game session cannot be null.").getActiveSport();
        for (Match match : context.getPreparedMatches()) {
            sport.getInjuryPolicy().applyPostMatch(match);
        }
        for (Team team : session.getCurrentSeason().getLeague().getTeams()) {
            sport.getInjuryPolicy().recoverPlayers(team);
        }
    }

    @Override
    public void advanceWeek(GameSession session, WeekProgressionContext context) {
        requireSeason(session).advanceWeek();
    }

    private FootballSeason requireSeason(GameSession session) {
        Objects.requireNonNull(session, "Game session cannot be null.");
        if (!(session.getCurrentSeason() instanceof FootballSeason footballSeason)) {
            throw new IllegalArgumentException("Football progression requires FootballSeason.");
        }
        return footballSeason;
    }
}
