package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StandingsQueryService {

    public StandingsView build(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Map<Team, MutableStandingStats> statsByTeam = new LinkedHashMap<>();
        for (Team team : validatedSession.getCurrentSeason().getLeague().getTeams()) {
            statsByTeam.put(team, new MutableStandingStats());
        }

        for (Fixture fixture : validatedSession.getCurrentSeason().getLeague().getFixtures()) {
            if (!fixture.isPlayed()) {
                continue;
            }

            Match match = fixture.getPlayedMatch();
            if (match == null || !match.isPlayed()) {
                continue;
            }

            applyMatch(statsByTeam.get(match.getHomeTeam()), match.getHomeScore(), match.getAwayScore(), validatedSession);
            applyMatch(statsByTeam.get(match.getAwayTeam()), match.getAwayScore(), match.getHomeScore(), validatedSession);
        }

        List<Team> rankedTeams = validatedSession.getActiveSport()
                .getStandingsPolicy()
                .rankTeams(validatedSession.getCurrentSeason().getLeague());

        List<StandingsRowView> rows = new ArrayList<>(rankedTeams.size());
        for (int i = 0; i < rankedTeams.size(); i++) {
            Team team = rankedTeams.get(i);
            MutableStandingStats stats = statsByTeam.get(team);
            rows.add(new StandingsRowView(
                    i + 1,
                    team.getName(),
                    stats.played,
                    stats.wins,
                    stats.draws,
                    stats.losses,
                    stats.scoresFor,
                    stats.scoresAgainst,
                    stats.scoresFor - stats.scoresAgainst,
                    stats.points
            ));
        }

        return new StandingsView(
                validatedSession.getSelectedSportId(),
                validatedSession.getCurrentSeason().getLeague().getName(),
                validatedSession.getCurrentSeason().getCurrentWeek(),
                List.copyOf(rows)
        );
    }

    private void applyMatch(
            MutableStandingStats stats,
            int scored,
            int conceded,
            GameSession session
    ) {
        stats.played++;
        stats.scoresFor += scored;
        stats.scoresAgainst += conceded;

        if (scored > conceded) {
            stats.wins++;
            stats.points += session.getActiveSport().getRuleset().getWinPoints();
            return;
        }
        if (scored < conceded) {
            stats.losses++;
            stats.points += session.getActiveSport().getRuleset().getLossPoints();
            return;
        }

        stats.draws++;
        stats.points += session.getActiveSport().getRuleset().getDrawPoints();
    }

    private static final class MutableStandingStats {
        private int played;
        private int wins;
        private int draws;
        private int losses;
        private int scoresFor;
        private int scoresAgainst;
        private int points;
    }
}
