package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Ruleset;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Central football rules implementation for M2.
 * Keeps lineup validation, bench limits, and points logic in one place.
 */
public final class FootballRuleset implements Ruleset {
    public static final int STARTING_LINEUP_SIZE = 11;
    public static final int MAX_BENCH_SIZE = 7;
    public static final int WIN_POINTS = 3;
    public static final int DRAW_POINTS = 1;
    public static final int LOSS_POINTS = 0;

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
}
