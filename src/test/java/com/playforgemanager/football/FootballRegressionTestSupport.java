package com.playforgemanager.football;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;

import java.util.List;

final class FootballRegressionTestSupport {
    private static final List<FootballPosition> POSITION_PATTERN = List.of(
            FootballPosition.GOALKEEPER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.GOALKEEPER,
            FootballPosition.DEFENDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD,
            FootballPosition.DEFENDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD,
            FootballPosition.GOALKEEPER,
            FootballPosition.FORWARD
    );

    private FootballRegressionTestSupport() {
    }

    static FootballSport sport() {
        return new FootballSport();
    }

    static FootballRuleset ruleset() {
        return new FootballRuleset();
    }

    static FootballTactic balancedTactic(String name) {
        return new FootballTactic(name, "4-3-3", FootballTactic.Mentality.BALANCED, 55, 55);
    }

    static FootballTactic attackingTactic(String name) {
        return new FootballTactic(name, "4-3-3", FootballTactic.Mentality.ATTACKING, 82, 78);
    }

    static FootballTactic defensiveTactic(String name) {
        return new FootballTactic(name, "5-4-1", FootballTactic.Mentality.DEFENSIVE, 38, 31);
    }

    static FootballTrainingPlan balancedTraining() {
        return new FootballTrainingPlan("Balanced Development", 55, 55, 55, true);
    }

    static FootballTrainingPlan attackingTraining() {
        return new FootballTrainingPlan("Attacking Press", 78, 65, 70, true);
    }

    static FootballTrainingPlan recoveryTraining() {
        return new FootballTrainingPlan("Recovery", 35, 35, 35, true);
    }

    static FootballCoach coach(String id, String specialization, int rating) {
        return new FootballCoach(id, "Coach " + id, "Head Coach", specialization, rating);
    }

    static FootballTeam team(String id, String name, int quality) {
        return team(id, name, quality, balancedTactic("Balanced " + name), balancedTraining(), coach(id + "-coach", "General Management", 75));
    }

    static FootballTeam team(
            String id,
            String name,
            int quality,
            FootballTactic tactic,
            FootballTrainingPlan trainingPlan,
            FootballCoach coach
    ) {
        FootballTeam team = new FootballTeam(id, name);
        for (int i = 0; i < POSITION_PATTERN.size(); i++) {
            FootballPosition position = POSITION_PATTERN.get(i);
            team.addPlayer(new FootballPlayer(
                    id + "-player-" + (i + 1),
                    name + " Player " + (i + 1),
                    position,
                    profileFor(position, quality, i)
            ));
        }

        if (coach != null) {
            team.addCoach(coach);
        }
        if (tactic != null) {
            team.assignTactic(tactic);
        }
        if (trainingPlan != null) {
            team.assignTrainingPlan(trainingPlan);
        }
        assignFreshLineup(team);
        return team;
    }

    static void assignFreshLineup(FootballTeam team) {
        FootballRuleset ruleset = ruleset();
        team.assignLineup(ruleset.buildLineup(team.getAvailablePlayers()), ruleset);
    }

    static FootballMatch preparedMatch(FootballTeam home, FootballTeam away) {
        return preparedMatch(sport(), home, away);
    }

    static FootballMatch preparedMatch(FootballSport sport, FootballTeam home, FootballTeam away) {
        if (home.getSelectedFootballLineup() == null) {
            assignFreshLineup(home);
        }
        if (away.getSelectedFootballLineup() == null) {
            assignFreshLineup(away);
        }
        FootballMatch match = new FootballMatch(home, away);
        match.setHomeSetup(home.getSelectedFootballLineup(), home.getSelectedFootballTactic());
        match.setAwaySetup(away.getSelectedFootballLineup(), away.getSelectedFootballTactic());
        sport.getFootballRuleset().validateLineupOrThrow(match.getHomeLineup());
        sport.getFootballRuleset().validateLineupOrThrow(match.getAwayLineup());
        return match;
    }

    static FootballLeague scheduledLeague(int teamCount) {
        FootballLeague league = new FootballLeague("Regression League");
        for (int i = 0; i < teamCount; i++) {
            FootballTactic tactic = switch (i % 3) {
                case 0 -> attackingTactic("Attack " + i);
                case 1 -> defensiveTactic("Defense " + i);
                default -> balancedTactic("Balanced " + i);
            };
            FootballTrainingPlan plan = switch (i % 3) {
                case 0 -> attackingTraining();
                case 1 -> recoveryTraining();
                default -> balancedTraining();
            };
            FootballCoach coach = switch (i % 3) {
                case 0 -> coach("coach-" + i, "Attacking Play", 88);
                case 1 -> coach("coach-" + i, "Recovery and Rehab", 86);
                default -> coach("coach-" + i, "General Management", 76);
            };
            league.addTeam(team("team-" + i, "Team " + i, 62 + (i * 5), tactic, plan, coach));
        }
        league.addFixtures(new RoundRobinFootballScheduler().generateFixtures(league.getTeams()));
        return league;
    }

    static FootballSeason playableSeason(int teamCount) {
        FootballSport sport = sport();
        FootballSeason season = new FootballSeason(scheduledLeague(teamCount));
        season.refreshStandings(sport.getStandingsPolicy());
        return season;
    }

    static void playUntilCompleted(FootballSeason season, FootballSport sport) {
        int safetyCounter = 0;
        int maxWeeks = Math.max(1, lastScheduledWeek(season)) + 2;
        while (!season.isCompleted()) {
            if (safetyCounter++ > maxWeeks) {
                throw new AssertionError("Season did not complete within the expected number of weeks.");
            }
            season.playCurrentWeek(sport, FootballMatch::new);
        }
    }


    static int lastScheduledWeek(FootballSeason season) {
        int lastWeek = 0;
        for (Object value : season.getLeague().getFixtures()) {
            Fixture fixture = (Fixture) value;
            lastWeek = Math.max(lastWeek, fixture.getWeek());
        }
        return lastWeek;
    }

    static long playedFixtureCount(FootballSeason season) {
        long played = 0;
        for (Object value : season.getLeague().getFixtures()) {
            if (((Fixture) value).isPlayed()) {
                played++;
            }
        }
        return played;
    }

    static boolean anyFixturePlayed(FootballSeason season) {
        return playedFixtureCount(season) > 0;
    }

    static boolean noFixturePlayed(FootballSeason season) {
        return playedFixtureCount(season) == 0;
    }

    static GameSession sessionWithSeason(FootballSport sport, FootballSeason season) {
        return new GameSession(
                sport,
                season,
                season.getLeague().getTeams().get(0),
                ProgressionState.IN_PROGRESS,
                "football"
        );
    }

    static SportRegistration footballRegistration(int teamCount) {
        return new SportRegistration(
                "football",
                "Football",
                new FootballSportFactory(new InMemoryAssetProvider(), teamCount)
        );
    }

    static int goalDifference(FootballMatch match) {
        return match.getHomeScore() - match.getAwayScore();
    }

    private static FootballAttributeProfile profileFor(FootballPosition position, int quality, int index) {
        int modifier = Math.floorMod(index, 7);
        return switch (position) {
            case GOALKEEPER -> new FootballAttributeProfile(
                    clamp(quality - 25 + modifier),
                    clamp(quality + 16 + modifier),
                    clamp(quality + 5),
                    clamp(quality - 2),
                    clamp(quality - 8)
            );
            case DEFENDER -> new FootballAttributeProfile(
                    clamp(quality - 8 + modifier),
                    clamp(quality + 12 + modifier),
                    clamp(quality + 5),
                    clamp(quality + 1),
                    clamp(quality - 1)
            );
            case MIDFIELDER -> new FootballAttributeProfile(
                    clamp(quality + 3 + modifier),
                    clamp(quality + 1),
                    clamp(quality + 7),
                    clamp(quality + 13 + modifier),
                    clamp(quality + 4)
            );
            case FORWARD -> new FootballAttributeProfile(
                    clamp(quality + 18 + modifier),
                    clamp(quality - 15),
                    clamp(quality + 3),
                    clamp(quality + 2),
                    clamp(quality + 12 + modifier)
            );
        };
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(99, value));
    }
}
