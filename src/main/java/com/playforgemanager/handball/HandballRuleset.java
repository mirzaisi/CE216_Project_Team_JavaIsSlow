package com.playforgemanager.handball;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Ruleset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class HandballRuleset implements Ruleset {
    public static final int STARTING_LINEUP_SIZE = 7;
    public static final int MAX_BENCH_SIZE = 9;
    public static final int WIN_POINTS = 2;
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
        return true;
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

        if (!(lineup instanceof HandballLineup handballLineup)) {
            throw new IllegalArgumentException("Handball rules require a HandballLineup instance.");
        }

        if (handballLineup.getStartingPlayers().size() != getStartingLineupSize()) {
            throw new IllegalArgumentException(
                    "Handball starting lineup must contain exactly " + getStartingLineupSize() + " players."
            );
        }

        // Ensures the starting lineup has exactly one player for each required position.
        validateStartingPositionCount(handballLineup, HandballPosition.GOALKEEPER);
        validateStartingPositionCount(handballLineup, HandballPosition.LEFT_WING);
        validateStartingPositionCount(handballLineup, HandballPosition.RIGHT_WING);
        validateStartingPositionCount(handballLineup, HandballPosition.LEFT_BACK);
        validateStartingPositionCount(handballLineup, HandballPosition.CENTER_BACK);
        validateStartingPositionCount(handballLineup, HandballPosition.RIGHT_BACK);
        validateStartingPositionCount(handballLineup, HandballPosition.PIVOT);

        if (handballLineup.getBenchPlayers().size() > getBenchSize()) {
            throw new IllegalArgumentException(
                    "Handball bench can contain at most " + getBenchSize() + " players."
            );
        }

        Set<String> seenIds = new HashSet<>();

        // Rejects duplicate or unavailable players in the complete lineup.
        for (HandballPlayer player : handballLineup.getAllPlayers()) {
            if (!seenIds.add(player.getId())) {
                throw new IllegalArgumentException("Duplicate handball player in lineup: " + player.getId());
            }

            if (!player.isAvailable()) {
                throw new IllegalArgumentException(
                        "Unavailable handball player cannot be selected: " + player.getName()
                );
            }
        }
    }

    public HandballLineup buildLineup(List<HandballPlayer> availablePlayers) {
        Objects.requireNonNull(availablePlayers, "Available players cannot be null.");

        List<HandballPlayer> playerPool = new ArrayList<>(availablePlayers.size());

        // Copies the available player list while rejecting null entries.
        for (HandballPlayer player : availablePlayers) {
            playerPool.add(Objects.requireNonNull(player, "Available player cannot be null."));
        }

        List<HandballPlayer> starters = new ArrayList<>(getStartingLineupSize());
        Set<String> selectedIds = new HashSet<>();

        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.GOALKEEPER);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.LEFT_WING);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.RIGHT_WING);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.LEFT_BACK);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.CENTER_BACK);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.RIGHT_BACK);
        addRequiredStarter(playerPool, starters, selectedIds, HandballPosition.PIVOT);

        List<HandballPlayer> bench = new ArrayList<>();

        // Fills the bench with remaining available players up to the bench limit.
        for (HandballPlayer player : playerPool) {
            if (bench.size() == getBenchSize()) {
                break;
            }

            if (player.isAvailable() && selectedIds.add(player.getId())) {
                bench.add(player);
            }
        }

        HandballLineup lineup = new HandballLineup(starters, bench);

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

    private void validateScore(int goalsScored, int goalsConceded) {
        if (goalsScored < 0 || goalsConceded < 0) {
            throw new IllegalArgumentException("Scores cannot be negative.");
        }
    }

    private void addRequiredStarter(
            List<HandballPlayer> playerPool,
            List<HandballPlayer> starters,
            Set<String> selectedIds,
            HandballPosition position
    ) {
        for (HandballPlayer player : playerPool) {
            if (player.getPosition() == position && player.isAvailable() && selectedIds.add(player.getId())) {
                starters.add(player);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Not enough available " + position.name() + " players for handball lineup."
        );
    }

    private void validateStartingPositionCount(HandballLineup lineup, HandballPosition position) {
        long actualCount = 0;

        for (HandballPlayer player : lineup.getStartingPlayers()) {
            if (player.getPosition() == position) {
                actualCount++;
            }
        }

        if (actualCount != 1) {
            throw new IllegalArgumentException(
                    "Handball starting lineup must contain exactly "
                            + 1 + " " + position.name() + " players."
            );
        }
    }
}
