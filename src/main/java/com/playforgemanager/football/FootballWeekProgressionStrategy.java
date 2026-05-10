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
    private final FootballTrainingEffectService trainingEffectService = new FootballTrainingEffectService();

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
    public void applyTraining(GameSession session, WeekProgressionContext context) {
        FootballSeason season = requireSeason(session);

        // Prevents training from being applied once any fixture in the week is already played.
        for (Fixture fixture : context.getScheduledFixtures()) {
            if (fixture.isPlayed()) {
                return;
            }
        }

        Sport sport = Objects.requireNonNull(session, "Game session cannot be null.").getActiveSport();
        FootballRuleset ruleset = requireFootballRuleset(sport);

        // Applies weekly football training to every team in the league.
        for (Team team : season.getLeague().getTeams()) {
            FootballTeam footballTeam = requireFootballTeam(team);

            trainingEffectService.applyWeeklyTraining(
                    footballTeam,
                    ruleset,
                    sport.getInjuryPolicy()
            );
        }
    }

    @Override
    public void prepareMatches(GameSession session, WeekProgressionContext context) {
        FootballSeason season = requireSeason(session);
        Sport sport = Objects.requireNonNull(session, "Game session cannot be null.").getActiveSport();

        // Creates and prepares matches for all unplayed fixtures in the week.
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

        // Simulates each prepared match and records its result in the league standings.
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

        // Applies post-match injuries to all matches prepared during this progression.
        for (Match match : context.getPreparedMatches()) {
            sport.getInjuryPolicy().applyPostMatch(match);
        }

        // Recovers players after the week has finished processing.
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

    private FootballRuleset requireFootballRuleset(Sport sport) {
        Objects.requireNonNull(sport, "Sport cannot be null.");

        if (!(sport.getRuleset() instanceof FootballRuleset footballRuleset)) {
            throw new IllegalArgumentException("Football progression requires FootballRuleset.");
        }

        return footballRuleset;
    }

    private FootballTeam requireFootballTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        if (!(team instanceof FootballTeam footballTeam)) {
            throw new IllegalArgumentException("Football progression requires FootballTeam instances.");
        }

        return footballTeam;
    }
}
