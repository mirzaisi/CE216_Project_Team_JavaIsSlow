package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Ruleset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Keeps lineup validation, starting-lineup composition, bench limits, and points logic in one place.
 */
public final class FootballRuleset implements Ruleset {
    public static final int STARTING_LINEUP_SIZE = 11;
    public static final int MAX_BENCH_SIZE = 7;
    public static final int WIN_POINTS = 3;
    public static final int DRAW_POINTS = 1;
    public static final int LOSS_POINTS = 0;
    public static final int REQUIRED_GOALKEEPERS = 1;
    public static final int REQUIRED_DEFENDERS = 4;
    public static final int REQUIRED_MIDFIELDERS = 4;
    public static final int REQUIRED_FORWARDS = 2;

    public enum MatchOutcome {
        WIN,
        DRAW,
        LOSS
    }

    @Override
    public int getWinPoints() {
        return WIN_POINTS;
    }

    @Override
    public int getDrawPoints() {
        return DRAW_POINTS;
    }

    @Override
    public int getLossPoints() {
        return LOSS_POINTS;
    }

    @Override
    public int getStartingLineupSize() {
        return STARTING_LINEUP_SIZE;
    }

    @Override
    public int getBenchSize() {
        return MAX_BENCH_SIZE;
    }

    @Override
    public boolean allowsUnlimitedSubstitutions() {
        return false;
    }

    @Override
    public boolean isValidLineup(Lineup lineup) {
        try {
            validateLineupOrThrow(lineup);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public void validateLineupOrThrow(Lineup lineup) {
        Objects.requireNonNull(lineup, "Lineup cannot be null.");
        if (!(lineup instanceof FootballLineup footballLineup)) {
            throw new IllegalArgumentException("Football rules require a FootballLineup instance.");
        }

        if (footballLineup.getStartingPlayers().size() != getStartingLineupSize()) {
            throw new IllegalArgumentException(
                    "Football starting lineup must contain exactly " + getStartingLineupSize() + " players.");
        }

        validateStartingPositionCount(footballLineup, FootballPosition.GOALKEEPER, REQUIRED_GOALKEEPERS);
        validateStartingPositionCount(footballLineup, FootballPosition.DEFENDER, REQUIRED_DEFENDERS);
        validateStartingPositionCount(footballLineup, FootballPosition.MIDFIELDER, REQUIRED_MIDFIELDERS);
        validateStartingPositionCount(footballLineup, FootballPosition.FORWARD, REQUIRED_FORWARDS);

        if (footballLineup.getBenchPlayers().size() > getBenchSize()) {
            throw new IllegalArgumentException(
                    "Football bench can contain at most " + getBenchSize() + " players.");
        }

        Set<String> seenIds = new HashSet<>();
        for (FootballPlayer player : footballLineup.getAllPlayers()) {
            if (!seenIds.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate football player in lineup: " + player.getId());
            }
            if (!player.isAvailable()) {
                throw new IllegalArgumentException(
                        "Unavailable football player cannot be selected: " + player.getName());
            }
        }
    }

    public FootballLineup buildLineup(List<FootballPlayer> availablePlayers) {
        Objects.requireNonNull(availablePlayers, "Available players cannot be null.");

        List<FootballPlayer> playerPool = new ArrayList<>(availablePlayers.size());
        for (FootballPlayer player : availablePlayers) {
            playerPool.add(Objects.requireNonNull(player, "Available player cannot be null."));
        }

        List<FootballPlayer> starters = new ArrayList<>(getStartingLineupSize());
        Set<String> selectedIds = new HashSet<>();
        addRequiredStarters(playerPool, starters, selectedIds, FootballPosition.GOALKEEPER, REQUIRED_GOALKEEPERS);
        addRequiredStarters(playerPool, starters, selectedIds, FootballPosition.DEFENDER, REQUIRED_DEFENDERS);
        addRequiredStarters(playerPool, starters, selectedIds, FootballPosition.MIDFIELDER, REQUIRED_MIDFIELDERS);
        addRequiredStarters(playerPool, starters, selectedIds, FootballPosition.FORWARD, REQUIRED_FORWARDS);

        List<FootballPlayer> bench = new ArrayList<>();
        for (FootballPlayer player : playerPool) {
            if (bench.size() == getBenchSize()) {
                break;
            }
            if (player.isAvailable() && selectedIds.add(player.getId())) {
                bench.add(player);
            }
        }

        FootballLineup lineup = new FootballLineup(starters, bench);
        validateLineupOrThrow(lineup);
        return lineup;
    }

    public MatchOutcome determineOutcome(int goalsScored, int goalsConceded) {
        validateScore(goalsScored, goalsConceded);
        if (goalsScored > goalsConceded) {
            return MatchOutcome.WIN;
        }
        if (goalsScored < goalsConceded) {
            return MatchOutcome.LOSS;
        }
        return MatchOutcome.DRAW;
    }

    public int getPointsForOutcome(MatchOutcome outcome) {
        return switch (Objects.requireNonNull(outcome, "Outcome cannot be null.")) {
            case WIN -> getWinPoints();
            case DRAW -> getDrawPoints();
            case LOSS -> getLossPoints();
        };
    }

    public int getPointsForResult(int goalsScored, int goalsConceded) {
        return getPointsForOutcome(determineOutcome(goalsScored, goalsConceded));
    }

    private void validateScore(int goalsScored, int goalsConceded) {
        if (goalsScored < 0 || goalsConceded < 0) {
            throw new IllegalArgumentException("Scores cannot be negative.");
        }
    }

    private void addRequiredStarters(
            List<FootballPlayer> playerPool,
            List<FootballPlayer> starters,
            Set<String> selectedIds,
            FootballPosition position,
            int requiredCount
    ) {
        int added = 0;
        for (FootballPlayer player : playerPool) {
            if (player.getPosition() == position && player.isAvailable() && selectedIds.add(player.getId())) {
                starters.add(player);
                added++;
                if (added == requiredCount) {
                    return;
                }
            }
        }

        throw new IllegalArgumentException(
                "Not enough available " + position.name() + " players for football lineup.");
    }

    private void validateStartingPositionCount(
            FootballLineup lineup,
            FootballPosition position,
            int expectedCount
    ) {
        long actualCount = lineup.getStartingPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .count();
        if (actualCount != expectedCount) {
            throw new IllegalArgumentException(
                    "Football starting lineup must contain exactly "
                            + expectedCount
                            + " "
                            + position.name()
                            + " players.");
        }
    }
}
