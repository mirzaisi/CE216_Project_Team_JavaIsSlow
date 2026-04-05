package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FootballStandingsPolicy implements StandingsPolicy {
    private static final Comparator<FootballStandingRow> TABLE_ORDER =
            Comparator.comparingInt(FootballStandingRow::getPoints).reversed()
                    .thenComparing(Comparator.comparingInt(FootballStandingRow::getGoalDifference).reversed())
                    .thenComparing(Comparator.comparingInt(FootballStandingRow::getGoalsFor).reversed())
                    .thenComparing(row -> row.getTeam().getName());

    private final FootballRuleset ruleset;

    public FootballStandingsPolicy(FootballRuleset ruleset) {
        this.ruleset = Objects.requireNonNull(ruleset, "Football ruleset cannot be null.");
    }

    @Override
    public void recordMatch(League league, Match match) {
        Objects.requireNonNull(league, "League cannot be null.");
        Objects.requireNonNull(match, "Match cannot be null.");

        if (!match.isPlayed()) {
            throw new IllegalArgumentException("Only played matches can affect standings.");
        }

        if (!league.getTeams().contains(match.getHomeTeam()) || !league.getTeams().contains(match.getAwayTeam())) {
            throw new IllegalArgumentException("Match teams must belong to the league.");
        }
    }

    @Override
    public List<Team> rankTeams(League league) {
        return calculateTable(league).stream().map(FootballStandingRow::getTeam).toList();
    }

    public List<FootballStandingRow> calculateTable(League league) {
        Objects.requireNonNull(league, "League cannot be null.");
        Map<Team, FootballStandingRow> table = new LinkedHashMap<>();
        for (Team team : league.getTeams()) {
            table.put(team, new FootballStandingRow(team));
        }

        for (Fixture fixture : league.getFixtures()) {
            if (!fixture.isPlayed()) {
                continue;
            }

            Match match = fixture.getPlayedMatch();
            if (match == null || !match.isPlayed()) {
                continue;
            }
            FootballStandingRow homeRow = table.get(match.getHomeTeam());
            FootballStandingRow awayRow = table.get(match.getAwayTeam());
            if (homeRow == null || awayRow == null) {
                throw new IllegalStateException("Played fixture contains a team outside the league.");
            }
            homeRow.recordMatch(match.getHomeScore(), match.getAwayScore(), ruleset);
            awayRow.recordMatch(match.getAwayScore(), match.getHomeScore(), ruleset);
        }
        return table.values().stream().sorted(TABLE_ORDER).toList();
    }
}
