package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class FootballSeason extends Season {
    private List<FootballStandingRow> standings;

    public FootballSeason(League league) {
        super(league);
        this.standings = buildInitialStandings();
    }

    public List<FootballStandingRow> getStandings() {
        return Collections.unmodifiableList(standings);
    }

    public List<Fixture> getCurrentWeekFixtures() {
        return getLeague().getFixtures().stream()
                .filter(fixture -> fixture.getWeek() == getCurrentWeek())
                .toList();
    }

    public void playCurrentWeek(Sport sport, BiFunction<Team, Team, Match> matchFactory) {
        Objects.requireNonNull(sport, "Sport cannot be null.");
        Objects.requireNonNull(matchFactory, "Match factory cannot be null.");

        if (isCompleted()) {
            throw new IllegalStateException("Season is already completed.");
        }

        if (getLeague().getFixtures().isEmpty()) {
            throw new IllegalStateException("Season has no scheduled fixtures.");
        }

        List<Fixture> currentWeekFixtures = getCurrentWeekFixtures();
        if (currentWeekFixtures.isEmpty()) {
            throw new IllegalStateException("No fixtures scheduled for week " + getCurrentWeek() + ".");
        }

        for (Fixture fixture : currentWeekFixtures) {
            if (fixture.isPlayed()) {
                continue;
            }

            Match match = Objects.requireNonNull(
                    matchFactory.apply(fixture.getHomeTeam(), fixture.getAwayTeam()),
                    "Match factory cannot return null."
            );

            applySelectedSetup(match, fixture.getHomeTeam(), fixture.getAwayTeam(), sport);
            sport.getMatchEngine().simulate(match, sport.getRuleset());
            fixture.attachPlayedMatch(match);
            sport.getStandingsPolicy().recordMatch(getLeague(), match);
            sport.getInjuryPolicy().applyPostMatch(match);
        }

        for (Team team : getLeague().getTeams()) {
            sport.getInjuryPolicy().recoverPlayers(team);
        }

        refreshStandings(sport.getStandingsPolicy());
        advanceWeek();
    }

    public void refreshStandings(StandingsPolicy standingsPolicy) {
        Objects.requireNonNull(standingsPolicy, "Standings policy cannot be null.");

        if (!(standingsPolicy instanceof FootballStandingsPolicy footballStandingsPolicy)) {
            throw new IllegalArgumentException("FootballSeason requires FootballStandingsPolicy.");
        }

        this.standings = footballStandingsPolicy.calculateTable(getLeague());
    }

    public boolean canCreateNextSeason() {
        return isCompleted();
    }

    public FootballSeason createNextSeasonStub() {
        if (!canCreateNextSeason()) {
            throw new IllegalStateException("Current season must be completed before creating the next one.");
        }

        FootballLeague nextLeague = new FootballLeague(getLeague().getName());
        for (Team team : getLeague().getTeams()) {
            nextLeague.addTeam(team);
        }

        return new FootballSeason(nextLeague);
    }

    @Override
    protected void doAdvanceWeek() {
        int lastScheduledWeek = getLeague().getFixtures().stream()
                .mapToInt(Fixture::getWeek)
                .max()
                .orElse(0);

        if (lastScheduledWeek == 0 || getCurrentWeek() >= lastScheduledWeek) {
            markCompleted();
            return;
        }

        setCurrentWeek(getCurrentWeek() + 1);
    }

    private List<FootballStandingRow> buildInitialStandings() {
        return getLeague().getTeams().stream()
                .map(FootballStandingRow::new)
                .toList();
    }

    private void applySelectedSetup(Match match, Team homeTeam, Team awayTeam, Sport sport) {
        match.setHomeSetup(resolveLineup(homeTeam, sport), resolveTactic(homeTeam));
        match.setAwaySetup(resolveLineup(awayTeam, sport), resolveTactic(awayTeam));
    }

    private Lineup resolveLineup(Team team, Sport sport) {
        Objects.requireNonNull(team, "Team cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");

        if (team.getSelectedLineup() != null) {
            validateLineup(team.getSelectedLineup(), sport);
            return team.getSelectedLineup();
        }

        if (team instanceof FootballTeam footballTeam) {
            List<FootballPlayer> availablePlayers = footballTeam.getAvailablePlayers();

            List<FootballPlayer> starters = availablePlayers.stream()
                    .limit(sport.getRuleset().getStartingLineupSize())
                    .toList();

            if (starters.size() != sport.getRuleset().getStartingLineupSize()) {
                throw new IllegalStateException("Not enough available players for team: " + team.getName());
            }

            int benchSize = Math.min(
                    sport.getRuleset().getBenchSize(),
                    Math.max(0, availablePlayers.size() - starters.size())
            );

            List<FootballPlayer> bench = availablePlayers.stream()
                    .skip(starters.size())
                    .limit(benchSize)
                    .toList();

            FootballLineup autoLineup = new FootballLineup(starters, bench);
            validateLineup(autoLineup, sport);

            if (sport.getRuleset() instanceof FootballRuleset footballRuleset) {
                footballTeam.assignLineup(autoLineup, footballRuleset);
            } else {
                footballTeam.assignLineup(autoLineup);
            }

            return autoLineup;
        }

        List<Player> availablePlayers = team.getRoster().stream()
                .filter(Player::isAvailable)
                .limit(sport.getRuleset().getStartingLineupSize())
                .toList();

        if (availablePlayers.size() != sport.getRuleset().getStartingLineupSize()) {
            throw new IllegalStateException("Not enough available players for team: " + team.getName());
        }

        throw new IllegalStateException("FootballSeason expects FootballTeam instances.");
    }

    private void validateLineup(Lineup lineup, Sport sport) {
        if (sport.getRuleset() instanceof FootballRuleset footballRuleset) {
            footballRuleset.validateLineupOrThrow(lineup);
            return;
        }

        if (!sport.getRuleset().isValidLineup(lineup)) {
            throw new IllegalArgumentException("Lineup is invalid for the active ruleset.");
        }
    }

    private Tactic resolveTactic(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        if (team.getSelectedTactic() != null) {
            return team.getSelectedTactic();
        }

        FootballTactic defaultTactic = new FootballTactic(
                "Balanced",
                "4-3-3",
                FootballTactic.Mentality.BALANCED,
                55,
                55
        );

        if (team instanceof FootballTeam footballTeam) {
            footballTeam.assignTactic(defaultTactic);
        } else {
            team.setSelectedTactic(defaultTactic);
        }

        return defaultTactic;
    }
}
