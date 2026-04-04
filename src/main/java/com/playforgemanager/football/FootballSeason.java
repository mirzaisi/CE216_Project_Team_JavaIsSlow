package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class FootballSeason extends Season {

    public FootballSeason(League league) {
        super(league);
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
        }

        advanceWeek();
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

    private void applySelectedSetup(Match match, Team homeTeam, Team awayTeam, Sport sport) {
        match.setHomeSetup(resolveLineup(homeTeam, sport), resolveTactic(homeTeam));
        match.setAwaySetup(resolveLineup(awayTeam, sport), resolveTactic(awayTeam));
    }

    private Lineup resolveLineup(Team team, Sport sport) {
        if (team.getSelectedLineup() != null) {
            return team.getSelectedLineup();
        }

        List<Player> availablePlayers = team.getRoster().stream()
                .filter(Player::isAvailable)
                .limit(sport.getRuleset().getStartingLineupSize())
                .toList();

        if (availablePlayers.size() != sport.getRuleset().getStartingLineupSize()) {
            throw new IllegalStateException("Not enough available players for team: " + team.getName());
        }

        Lineup autoLineup = new BootstrapFootballLineup(availablePlayers);
        team.setSelectedLineup(autoLineup);
        return autoLineup;
    }

    private Tactic resolveTactic(Team team) {
        if (team.getSelectedTactic() != null) {
            return team.getSelectedTactic();
        }

        Tactic defaultTactic = new BootstrapFootballTactic("Balanced");
        team.setSelectedTactic(defaultTactic);
        return defaultTactic;
    }
}
