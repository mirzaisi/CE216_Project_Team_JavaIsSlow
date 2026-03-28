package com.playforgemanager.main;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.core.Team;
import com.playforgemanager.football.BootstrapFootballLineup;
import com.playforgemanager.football.BootstrapFootballMatch;
import com.playforgemanager.football.BootstrapFootballTactic;
import com.playforgemanager.football.BootstrapFootballTeam;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportFactory sportFactory = new FootballSportFactory(assetProvider, 4);
        GameInitializationService initializationService = new GameInitializationService(sportFactory);

        GameSession session = initializationService.startNewSession("PlayForge Demo League");
        League league = session.getCurrentSeason().getLeague();
        Ruleset ruleset = session.getActiveSport().getRuleset();

        List<Fixture> generatedFixtures = session.getActiveSport()
                .getScheduler()
                .generateFixtures(league.getTeams());

        generatedFixtures.forEach(league::addFixture);

        System.out.println("=== PlayForge Manager - M2 Demo ===");
        System.out.println("Sport: " + session.getActiveSport().getName());
        System.out.println("League: " + league.getName());
        System.out.println("Controlled Team: " + session.getControlledTeam().getName());
        System.out.println();

        printGeneratedTeams(league);
        simulateCurrentWeek(session, ruleset);
        printStandingsTable(league, ruleset);

        session.markInProgress();
        session.getCurrentSeason().advanceWeek();

        System.out.println();
        System.out.println("Next week: " + session.getCurrentSeason().getCurrentWeek());
    }

    private static void printGeneratedTeams(League league) {
        System.out.println("Generated Teams");
        System.out.println("---------------");

        for (Team team : league.getTeams()) {
            int coachCount = 0;
            if (team instanceof BootstrapFootballTeam bootstrapTeam) {
                coachCount = bootstrapTeam.getCoaches().size();
            }

            System.out.printf(
                    "- %-15s | Players: %-2d | Coaches: %-2d%n",
                    team.getName(),
                    team.getRoster().size(),
                    coachCount
            );
        }

        System.out.println();
        System.out.println("Total fixtures generated: " + league.getFixtures().size());
        System.out.println();
    }

    private static void simulateCurrentWeek(GameSession session, Ruleset ruleset) {
        League league = session.getCurrentSeason().getLeague();
        int currentWeek = session.getCurrentSeason().getCurrentWeek();

        List<Fixture> currentWeekFixtures = league.getFixtures()
                .stream()
                .filter(fixture -> fixture.getWeek() == currentWeek)
                .toList();

        System.out.println("Week " + currentWeek + " Results");
        System.out.println("----------------");

        for (Fixture fixture : currentWeekFixtures) {
            BootstrapFootballMatch match = new BootstrapFootballMatch(
                    fixture.getHomeTeam(),
                    fixture.getAwayTeam()
            );

            match.setHomeSetup(
                    buildAutomaticLineup(fixture.getHomeTeam(), ruleset),
                    new BootstrapFootballTactic("Balanced")
            );

            match.setAwaySetup(
                    buildAutomaticLineup(fixture.getAwayTeam(), ruleset),
                    new BootstrapFootballTactic("Balanced")
            );

            session.getActiveSport().getMatchEngine().simulate(match, ruleset);
            fixture.attachPlayedMatch(match);

            System.out.printf(
                    "%-15s %d - %d %-15s%n",
                    match.getHomeTeam().getName(),
                    match.getHomeScore(),
                    match.getAwayScore(),
                    match.getAwayTeam().getName()
            );
        }
    }

    private static BootstrapFootballLineup buildAutomaticLineup(Team team, Ruleset ruleset) {
        List<Player> availablePlayers = team.getRoster()
                .stream()
                .filter(Player::isAvailable)
                .limit(ruleset.getStartingLineupSize())
                .toList();

        if (availablePlayers.size() != ruleset.getStartingLineupSize()) {
            throw new IllegalStateException("Not enough available players for team: " + team.getName());
        }

        return new BootstrapFootballLineup(availablePlayers);
    }

    private static void printStandingsTable(League league, Ruleset ruleset) {
        Map<Team, TableRow> table = new LinkedHashMap<>();

        for (Team team : league.getTeams()) {
            table.put(team, new TableRow(team.getName()));
        }

        for (Fixture fixture : league.getFixtures()) {
            if (!fixture.isPlayed()) {
                continue;
            }

            Match match = fixture.getPlayedMatch();
            TableRow home = table.get(match.getHomeTeam());
            TableRow away = table.get(match.getAwayTeam());

            home.played++;
            away.played++;

            home.goalsFor += match.getHomeScore();
            home.goalsAgainst += match.getAwayScore();

            away.goalsFor += match.getAwayScore();
            away.goalsAgainst += match.getHomeScore();

            if (match.getHomeScore() > match.getAwayScore()) {
                home.wins++;
                away.losses++;
                home.points += ruleset.getWinPoints();
                away.points += ruleset.getLossPoints();
            } else if (match.getHomeScore() < match.getAwayScore()) {
                away.wins++;
                home.losses++;
                away.points += ruleset.getWinPoints();
                home.points += ruleset.getLossPoints();
            } else {
                home.draws++;
                away.draws++;
                home.points += ruleset.getDrawPoints();
                away.points += ruleset.getDrawPoints();
            }
        }

        List<TableRow> sortedRows = table.values()
                .stream()
                .sorted(Comparator
                        .comparingInt(TableRow::getPoints).reversed()
                        .thenComparingInt(TableRow::getGoalDifference).reversed()
                        .thenComparingInt(TableRow::getGoalsFor).reversed()
                        .thenComparing(TableRow::getTeamName))
                .toList();

        System.out.println();
        System.out.println("Standings");
        System.out.println("---------");
        System.out.printf("%-3s %-15s %-3s %-3s %-3s %-3s %-3s %-3s %-3s %-3s%n",
                "#", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");

        int position = 1;
        for (TableRow row : sortedRows) {
            System.out.printf("%-3d %-15s %-3d %-3d %-3d %-3d %-3d %-3d %-3d %-3d%n",
                    position++,
                    row.teamName,
                    row.played,
                    row.wins,
                    row.draws,
                    row.losses,
                    row.goalsFor,
                    row.goalsAgainst,
                    row.getGoalDifference(),
                    row.points);
        }
    }

    private static class TableRow {
        private final String teamName;
        private int played;
        private int wins;
        private int draws;
        private int losses;
        private int goalsFor;
        private int goalsAgainst;
        private int points;

        private TableRow(String teamName) {
            this.teamName = teamName;
        }

        public String getTeamName() {
            return teamName;
        }

        public int getPoints() {
            return points;
        }

        public int getGoalsFor() {
            return goalsFor;
        }

        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }
    }
}