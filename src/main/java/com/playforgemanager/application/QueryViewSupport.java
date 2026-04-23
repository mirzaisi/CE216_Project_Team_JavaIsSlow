package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class QueryViewSupport {
    private QueryViewSupport() {
    }

    static int countAvailablePlayers(Team team) {
        int availablePlayers = 0;
        for (Player player : team.getRoster()) {
            if (player.isAvailable()) {
                availablePlayers++;
            }
        }
        return availablePlayers;
    }

    static FixtureSummaryView toFixtureSummary(Fixture fixture, Team controlledTeam) {
        Objects.requireNonNull(fixture, "Fixture cannot be null.");
        boolean controlledInvolved = controlledTeam != null
                && (fixture.getHomeTeam() == controlledTeam || fixture.getAwayTeam() == controlledTeam);
        boolean controlledHome = controlledInvolved && fixture.getHomeTeam() == controlledTeam;
        Match playedMatch = fixture.getPlayedMatch();

        return new FixtureSummaryView(
                fixture.getWeek(),
                fixture.getHomeTeam().getName(),
                fixture.getAwayTeam().getName(),
                fixture.isPlayed(),
                playedMatch == null ? null : playedMatch.getHomeScore(),
                playedMatch == null ? null : playedMatch.getAwayScore(),
                controlledInvolved,
                controlledHome
        );
    }

    static List<FixtureSummaryView> buildFixtureSummaries(List<Fixture> fixtures, Team controlledTeam) {
        List<FixtureSummaryView> summaries = new ArrayList<>(fixtures.size());
        for (Fixture fixture : fixtures) {
            summaries.add(toFixtureSummary(fixture, controlledTeam));
        }
        return List.copyOf(summaries);
    }

    static String roleLabel(Player player) {
        Objects.requireNonNull(player, "Player cannot be null.");
        String reflectedLabel = invokeZeroArgString(player, "getPosition");
        if (reflectedLabel != null) {
            return reflectedLabel;
        }
        reflectedLabel = invokeZeroArgString(player, "getRole");
        if (reflectedLabel != null) {
            return reflectedLabel;
        }
        return "Player";
    }

    static List<PlayerSummaryView> buildPlayerSummaries(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");
        List<? extends Player> selectedPlayers = team.getSelectedLineup() == null
                ? List.of()
                : team.getSelectedLineup().getSelectedPlayers();

        return team.getRoster().stream()
                .sorted(Comparator.comparing(Player::getName))
                .map(player -> new PlayerSummaryView(
                        player.getId(),
                        player.getName(),
                        roleLabel(player),
                        player.isAvailable(),
                        player.getInjuryMatchesRemaining(),
                        selectedPlayers.contains(player)
                ))
                .toList();
    }

    static Team findControlledTeam(GameSession session) {
        return Objects.requireNonNull(session, "Game session cannot be null.").getControlledTeam();
    }

    private static String invokeZeroArgString(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object result = method.invoke(target);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
