package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FootballStandingsPolicy implements StandingsPolicy {
    private static final Comparator<FootballStandingRow> TABLE_ORDER =
            Comparator.comparingInt(FootballStandingRow::getPoints).reversed()
                    .thenComparing(Comparator.comparingInt(FootballStandingRow::getGoalDifference).reversed())
                    .thenComparing(Comparator.comparingInt(FootballStandingRow::getGoalsFor).reversed())
                    .thenComparing(row -> row.getTeam().getName());

    private final FootballRuleset ruleset;
    private final Map<League, List<Match>> recordedMatches;

    public FootballStandingsPolicy(FootballRuleset ruleset) {
        this.ruleset = Objects.requireNonNull(ruleset, "Football ruleset cannot be null.");
        this.recordedMatches = new IdentityHashMap<>();
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

        List<Match> matches = recordedMatches.computeIfAbsent(league, ignored -> new ArrayList<>());
        boolean alreadyRecorded = matches.stream().anyMatch(recorded -> recorded == match);
        if (!alreadyRecorded) {
            matches.add(match);
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

        Set<Match> appliedMatches = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Fixture fixture : league.getFixtures()) {
            if (!fixture.isPlayed()) {
                continue;
            }

            Match match = fixture.getPlayedMatch();
            if (match == null || !match.isPlayed()) {
                continue;
            }
            applyMatch(table, match);
            appliedMatches.add(match);
        }

        for (Match match : recordedMatches.getOrDefault(league, List.of())) {
            if (!appliedMatches.contains(match)) {
                applyMatch(table, match);
            }
        }

        return table.values().stream().sorted(TABLE_ORDER).toList();
    }

    private void applyMatch(Map<Team, FootballStandingRow> table, Match match) {
        FootballStandingRow homeRow = table.get(match.getHomeTeam());
        FootballStandingRow awayRow = table.get(match.getAwayTeam());
        if (homeRow == null || awayRow == null) {
            throw new IllegalStateException("Played match contains a team outside the league.");
        }
        homeRow.recordMatch(match.getHomeScore(), match.getAwayScore(), ruleset);
        awayRow.recordMatch(match.getAwayScore(), match.getHomeScore(), ruleset);
    }
}
