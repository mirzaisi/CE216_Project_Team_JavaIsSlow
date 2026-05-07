package com.playforgemanager.handball;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class HandballSeason extends com.playforgemanager.core.Season {
    private List<HandballStandingRow> standings;

    public HandballSeason(League league) {
        super(league);
        this.standings = buildInitialStandings();
    }

    public List<HandballStandingRow> getStandings() {
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

            prepareMatch(match, sport);
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
        if (!(standingsPolicy instanceof HandballStandingsPolicy handballStandingsPolicy)) {
            throw new IllegalArgumentException("HandballSeason requires HandballStandingsPolicy.");
        }
        this.standings = handballStandingsPolicy.calculateTable(getLeague());
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

    private List<HandballStandingRow> buildInitialStandings() {
        return getLeague().getTeams().stream()
                .map(HandballStandingRow::new)
                .toList();
    }

    void prepareMatch(Match match, Sport sport) {
        Objects.requireNonNull(match, "Match cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");
        match.setHomeSetup(resolveLineup(match.getHomeTeam(), sport), resolveTactic(match.getHomeTeam()));
        match.setAwaySetup(resolveLineup(match.getAwayTeam(), sport), resolveTactic(match.getAwayTeam()));
    }

    private Lineup resolveLineup(Team team, Sport sport) {
        Objects.requireNonNull(team, "Team cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");

        HandballTeam handballTeam = requireHandballTeam(team);

        if (handballTeam.getSelectedLineup() != null) {
            validateLineup(handballTeam.getSelectedLineup(), sport);
            return handballTeam.getSelectedLineup();
        }

        HandballRuleset handballRuleset = requireHandballRuleset(sport);
        HandballLineup autoLineup = handballRuleset.buildLineup(handballTeam.getAvailablePlayers());
        handballTeam.assignLineup(autoLineup, handballRuleset);
        return autoLineup;
    }

    private void validateLineup(Lineup lineup, Sport sport) {
        HandballRuleset handballRuleset = requireHandballRuleset(sport);
        handballRuleset.validateLineupOrThrow(lineup);
    }

    private Tactic resolveTactic(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        HandballTeam handballTeam = requireHandballTeam(team);

        if (handballTeam.getSelectedTactic() != null) {
            return handballTeam.getSelectedTactic();
        }

        HandballTactic defaultTactic = new HandballTactic(
                "Balanced",
                "3-3",
                HandballTactic.Tempo.BALANCED,
                55,
                55
        );
        handballTeam.assignTactic(defaultTactic);
        return defaultTactic;
    }

    private HandballRuleset requireHandballRuleset(Sport sport) {
        Objects.requireNonNull(sport, "Sport cannot be null.");
        if (!(sport.getRuleset() instanceof HandballRuleset handballRuleset)) {
            throw new IllegalArgumentException("HandballSeason requires HandballRuleset.");
        }
        return handballRuleset;
    }

    private HandballTeam requireHandballTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");
        if (!(team instanceof HandballTeam handballTeam)) {
            throw new IllegalStateException("HandballSeason expects HandballTeam instances.");
        }
        return handballTeam;
    }
}
