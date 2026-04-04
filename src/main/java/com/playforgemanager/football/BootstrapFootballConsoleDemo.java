package com.playforgemanager.football;

import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.SportFactory;
import java.util.List;

public class BootstrapFootballConsoleDemo {

    public void run(SportFactory sportFactory, String leagueName) {
        GameInitializationService initializationService = new GameInitializationService(sportFactory);

        GameSession session = initializationService.startNewSession(leagueName);
        League league = session.getCurrentSeason().getLeague();
        if (!(session.getCurrentSeason() instanceof FootballSeason footballSeason)) {
            throw new IllegalStateException("Expected a FootballSeason for the football demo.");
        }

        System.out.println("=== PlayForge Manager - M2 Demo ===");
        System.out.println("Sport: " + session.getActiveSport().getName());
        System.out.println("League: " + league.getName());
        System.out.println("Controlled Team: " + session.getControlledTeam().getName());
        System.out.println();

        printGeneratedTeams(league);
        List<Fixture> currentWeekFixtures = footballSeason.getCurrentWeekFixtures();
        int playedWeek = footballSeason.getCurrentWeek();
        footballSeason.playCurrentWeek(session.getActiveSport(), BootstrapFootballMatch::new);
        printWeekResults(currentWeekFixtures, playedWeek);
        printStandingsTable(footballSeason.getStandings());

        session.markInProgress();

        System.out.println();
        if (session.getCurrentSeason().isCompleted()) {
            System.out.println("Season completed.");
            session.markCompleted();
        } else {
            System.out.println("Next week: " + session.getCurrentSeason().getCurrentWeek());
        }
    }

    private void printGeneratedTeams(League league) {
        System.out.println("Generated Teams");
        System.out.println("---------------");

        for (com.playforgemanager.core.Team team : league.getTeams()) {
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

    private void printWeekResults(List<Fixture> playedWeekFixtures, int week) {
        System.out.println("Week " + week + " Results");
        System.out.println("----------------");

        for (Fixture fixture : playedWeekFixtures) {
            if (!fixture.isPlayed()) {
                continue;
            }

            var match = fixture.getPlayedMatch();

            System.out.printf(
                    "%-15s %d - %d %-15s%n",
                    match.getHomeTeam().getName(),
                    match.getHomeScore(),
                    match.getAwayScore(),
                    match.getAwayTeam().getName()
            );
        }
    }

    private void printStandingsTable(List<FootballStandingRow> standings) {
        System.out.println();
        System.out.println("Standings");
        System.out.println("---------");
        System.out.printf("%-3s %-15s %-3s %-3s %-3s %-3s %-3s %-3s %-3s %-3s%n",
                "#", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");

        int position = 1;
        for (FootballStandingRow row : standings) {
            System.out.printf("%-3d %-15s %-3d %-3d %-3d %-3d %-3d %-3d %-3d %-3d%n",
                    position++,
                    row.getTeam().getName(),
                    row.getPlayed(),
                    row.getWins(),
                    row.getDraws(),
                    row.getLosses(),
                    row.getGoalsFor(),
                    row.getGoalsAgainst(),
                    row.getGoalDifference(),
                    row.getPoints());
        }
    }
}
