package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class FootballSeason extends com.playforgemanager.core.Season {
    private final FootballTrainingEffectService trainingEffectService;
    private List<FootballStandingRow> standings;

    public FootballSeason(League league) {
        this(league, new FootballTrainingEffectService());
    }

    public FootballSeason(League league, FootballTrainingEffectService trainingEffectService) {
        super(league);

        this.trainingEffectService = Objects.requireNonNull(
                trainingEffectService,
                "Training effect service cannot be null."
        );
        this.standings = buildInitialStandings();
    }

    public List<FootballStandingRow> getStandings() {
        return Collections.unmodifiableList(standings);
    }

    public List<Fixture> getCurrentWeekFixtures() {
        List<Fixture> currentWeekFixtures = new ArrayList<>();

        // Collects only the fixtures scheduled for the current season week.
        for (Fixture fixture : getLeague().getFixtures()) {
            if (fixture.getWeek() == getCurrentWeek()) {
                currentWeekFixtures.add(fixture);
            }
        }

        return currentWeekFixtures;
    }

    void prepareMatch(Match match, Sport sport) {
        Objects.requireNonNull(match, "Match cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");

        applySelectedSetup(match, match.getHomeTeam(), match.getAwayTeam(), sport);
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

        applyWeeklyTrainingEffects(sport);

        // Plays every unplayed fixture in the current week.
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

        // Recovers players after all current-week fixtures have been processed.
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

    public FootballSeason createNextSeason(Sport sport) {
        Objects.requireNonNull(sport, "Sport cannot be null.");

        requireCompletedForNextSeason();
        requireFootballRuleset(sport);

        FootballLeague nextLeague = new FootballLeague(getLeague().getName());

        // Carries existing football teams into the next generated season.
        for (Team team : getLeague().getTeams()) {
            FootballTeam footballTeam = requireFootballTeam(team);

            prepareTeamForNextSeason(footballTeam);
            nextLeague.addTeam(footballTeam);
        }

        List<Fixture> nextFixtures = sport.getScheduler().generateFixtures(nextLeague.getTeams());

        if (nextFixtures.isEmpty()) {
            throw new IllegalStateException("Next football season must contain scheduled fixtures.");
        }

        nextLeague.addFixtures(nextFixtures);

        FootballSeason nextSeason = new FootballSeason(nextLeague, trainingEffectService);

        nextSeason.refreshStandings(sport.getStandingsPolicy());

        return nextSeason;
    }

    public void restoreProgress(int currentWeek, boolean completed) {
        setCurrentWeek(currentWeek);

        if (completed) {
            markCompleted();
        }
    }

    @Override
    protected void doAdvanceWeek() {
        int lastScheduledWeek = 0;

        // Finds the final week that has at least one scheduled fixture.
        for (Fixture fixture : getLeague().getFixtures()) {
            if (fixture.getWeek() > lastScheduledWeek) {
                lastScheduledWeek = fixture.getWeek();
            }
        }

        if (lastScheduledWeek == 0 || getCurrentWeek() >= lastScheduledWeek) {
            markCompleted();
            return;
        }

        setCurrentWeek(getCurrentWeek() + 1);
    }

    private List<FootballStandingRow> buildInitialStandings() {
        List<FootballStandingRow> standingRows = new ArrayList<>();

        // Creates an empty standings row for each league team.
        for (Team team : getLeague().getTeams()) {
            standingRows.add(new FootballStandingRow(team));
        }

        return standingRows;
    }

    private void applySelectedSetup(Match match, Team homeTeam, Team awayTeam, Sport sport) {
        match.setHomeSetup(resolveLineup(homeTeam, sport), resolveTactic(homeTeam));
        match.setAwaySetup(resolveLineup(awayTeam, sport), resolveTactic(awayTeam));
    }

    private void applyWeeklyTrainingEffects(Sport sport) {
        FootballRuleset footballRuleset = requireFootballRuleset(sport);

        // Applies each team's selected training plan before match simulation.
        for (Team team : getLeague().getTeams()) {
            FootballTeam footballTeam = requireFootballTeam(team);

            trainingEffectService.applyWeeklyTraining(
                    footballTeam,
                    footballRuleset,
                    sport.getInjuryPolicy()
            );
        }
    }

    private Lineup resolveLineup(Team team, Sport sport) {
        Objects.requireNonNull(team, "Team cannot be null.");
        Objects.requireNonNull(sport, "Sport cannot be null.");

        FootballTeam footballTeam = requireFootballTeam(team);

        if (footballTeam.getSelectedLineup() != null) {
            validateLineup(footballTeam.getSelectedLineup(), sport);
            return footballTeam.getSelectedLineup();
        }

        FootballRuleset footballRuleset = requireFootballRuleset(sport);
        FootballLineup autoLineup = footballRuleset.buildLineup(footballTeam.getAvailablePlayers());

        footballTeam.assignLineup(autoLineup, footballRuleset);

        return autoLineup;
    }

    private void validateLineup(Lineup lineup, Sport sport) {
        FootballRuleset footballRuleset = requireFootballRuleset(sport);

        footballRuleset.validateLineupOrThrow(lineup);
    }

    private FootballRuleset requireFootballRuleset(Sport sport) {
        Objects.requireNonNull(sport, "Sport cannot be null.");

        if (!(sport.getRuleset() instanceof FootballRuleset footballRuleset)) {
            throw new IllegalArgumentException("FootballSeason requires FootballRuleset.");
        }

        return footballRuleset;
    }

    private FootballTeam requireFootballTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        if (!(team instanceof FootballTeam footballTeam)) {
            throw new IllegalStateException("FootballSeason expects FootballTeam instances.");
        }

        return footballTeam;
    }

    private void requireCompletedForNextSeason() {
        if (!canCreateNextSeason()) {
            throw new IllegalStateException("Current season must be completed before creating the next one.");
        }
    }

    private void prepareTeamForNextSeason(FootballTeam team) {
        team.setSelectedLineup(null);

        // Clears temporary player state before the next season starts.
        for (FootballPlayer player : team.getFootballPlayers()) {
            resetPlayerForNextSeason(player);
        }
    }

    private void resetPlayerForNextSeason(FootballPlayer player) {
        Objects.requireNonNull(player, "Football player cannot be null.");

        player.clearWeeklyTrainingEffect();

        while (player.getInjuryMatchesRemaining() > 0) {
            player.recoverOneMatch();
        }

        player.setAvailable(true);
    }

    private Tactic resolveTactic(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        FootballTeam footballTeam = requireFootballTeam(team);

        if (footballTeam.getSelectedTactic() != null) {
            return footballTeam.getSelectedTactic();
        }

        FootballTactic defaultTactic = new FootballTactic(
                "Balanced",
                "4-3-3",
                FootballTactic.Mentality.BALANCED,
                55,
                55
        );

        footballTeam.assignTactic(defaultTactic);

        return defaultTactic;
    }
}
