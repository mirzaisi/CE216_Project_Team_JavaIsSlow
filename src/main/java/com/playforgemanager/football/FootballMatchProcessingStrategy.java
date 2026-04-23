package com.playforgemanager.football;

import com.playforgemanager.application.MatchProcessingStrategy;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;

import java.util.Objects;

public class FootballMatchProcessingStrategy implements MatchProcessingStrategy {

    @Override
    public Match processMatch(GameSession session, Fixture fixture) {
        FootballSeason season = requireSeason(session);
        Fixture validatedFixture = Objects.requireNonNull(fixture, "Fixture cannot be null.");
        if (validatedFixture.isPlayed()) {
            throw new IllegalStateException("Fixture is already played.");
        }

        Sport sport = session.getActiveSport();
        League league = session.getCurrentSeason().getLeague();
        Match match = new FootballMatch(validatedFixture.getHomeTeam(), validatedFixture.getAwayTeam());

        season.prepareMatch(match, sport);
        sport.getMatchEngine().simulate(match, sport.getRuleset());
        validatedFixture.attachPlayedMatch(match);
        sport.getStandingsPolicy().recordMatch(league, match);
        season.refreshStandings(sport.getStandingsPolicy());
        sport.getInjuryPolicy().applyPostMatch(match);

        return match;
    }

    private FootballSeason requireSeason(GameSession session) {
        Objects.requireNonNull(session, "Game session cannot be null.");
        if (!(session.getCurrentSeason() instanceof FootballSeason footballSeason)) {
            throw new IllegalArgumentException("Football match processing requires FootballSeason.");
        }
        return footballSeason;
    }
}
