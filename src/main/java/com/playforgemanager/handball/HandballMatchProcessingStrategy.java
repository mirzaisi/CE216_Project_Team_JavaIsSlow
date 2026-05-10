package com.playforgemanager.handball;

import com.playforgemanager.application.MatchProcessingStrategy;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;

import java.util.Objects;

public class HandballMatchProcessingStrategy implements MatchProcessingStrategy {

    @Override
    public Match processMatch(GameSession session, Fixture fixture) {
        HandballSeason season = requireSeason(session);
        Fixture validatedFixture = Objects.requireNonNull(fixture, "Fixture cannot be null.");

        if (validatedFixture.isPlayed()) {
            throw new IllegalStateException("Fixture is already played.");
        }

        Sport sport = session.getActiveSport();
        League league = session.getCurrentSeason().getLeague();

        Match match = new HandballMatch(validatedFixture.getHomeTeam(), validatedFixture.getAwayTeam());

        // Prepares, simulates, records, and finalizes the handball match.
        season.prepareMatch(match, sport);
        sport.getMatchEngine().simulate(match, sport.getRuleset());
        validatedFixture.attachPlayedMatch(match);
        sport.getStandingsPolicy().recordMatch(league, match);
        season.refreshStandings(sport.getStandingsPolicy());
        sport.getInjuryPolicy().applyPostMatch(match);

        return match;
    }

    private HandballSeason requireSeason(GameSession session) {
        Objects.requireNonNull(session, "Game session cannot be null.");

        if (!(session.getCurrentSeason() instanceof HandballSeason handballSeason)) {
            throw new IllegalArgumentException("Handball match processing requires HandballSeason.");
        }

        return handballSeason;
    }
}
