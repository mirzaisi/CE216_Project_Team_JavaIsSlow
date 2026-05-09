package com.playforgemanager.football;

import com.playforgemanager.application.MatchProcessingStrategy;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class FootballMatchProcessingStrategy implements MatchProcessingStrategy {
    private final FootballTrainingEffectService trainingEffectService = new FootballTrainingEffectService();

    @Override
    public Match processMatch(GameSession session, Fixture fixture) {
        FootballSeason season = requireSeason(session);
        Fixture validatedFixture = Objects.requireNonNull(fixture, "Fixture cannot be null.");
        if (validatedFixture.isPlayed()) {
            throw new IllegalStateException("Fixture is already played.");
        }

        Sport sport = session.getActiveSport();
        League league = session.getCurrentSeason().getLeague();
        applyWeeklyTrainingIfNeeded(season, sport);
        Match match = new FootballMatch(validatedFixture.getHomeTeam(), validatedFixture.getAwayTeam());

        season.prepareMatch(match, sport);
        sport.getMatchEngine().simulate(match, sport.getRuleset());
        validatedFixture.attachPlayedMatch(match);
        sport.getStandingsPolicy().recordMatch(league, match);
        season.refreshStandings(sport.getStandingsPolicy());
        sport.getInjuryPolicy().applyPostMatch(match);

        return match;
    }

    private void applyWeeklyTrainingIfNeeded(FootballSeason season, Sport sport) {
        Objects.requireNonNull(season, "Season cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");

        if (season.getCurrentWeekFixtures().stream().anyMatch(Fixture::isPlayed)) {
            return;
        }

        FootballRuleset ruleset = requireFootballRuleset(sport);
        for (Team team : season.getLeague().getTeams()) {
            FootballTeam footballTeam = requireFootballTeam(team);
            trainingEffectService.applyWeeklyTraining(footballTeam, ruleset, sport.getInjuryPolicy());
        }
    }

    private FootballSeason requireSeason(GameSession session) {
        Objects.requireNonNull(session, "Game session cannot be null.");
        if (!(session.getCurrentSeason() instanceof FootballSeason footballSeason)) {
            throw new IllegalArgumentException("Football match processing requires FootballSeason.");
        }
        return footballSeason;
    }

    private FootballRuleset requireFootballRuleset(Sport sport) {
        Objects.requireNonNull(sport, "Sport cannot be null.");
        if (!(sport.getRuleset() instanceof FootballRuleset footballRuleset)) {
            throw new IllegalArgumentException("Football match processing requires FootballRuleset.");
        }
        return footballRuleset;
    }

    private FootballTeam requireFootballTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");
        if (!(team instanceof FootballTeam footballTeam)) {
            throw new IllegalArgumentException("Football match processing requires FootballTeam instances.");
        }
        return footballTeam;
    }
}
