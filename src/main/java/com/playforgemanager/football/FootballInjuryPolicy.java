package com.playforgemanager.football;

import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Team;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FootballInjuryPolicy implements InjuryPolicy {

    private static final int DEFAULT_DURATION = 2;

    private final int injuryDurationMatches;
    private final Set<Player> newlyInjuredPlayers;

    public FootballInjuryPolicy() {
        this(DEFAULT_DURATION);
    }

    public FootballInjuryPolicy(int injuryDurationMatches) {
        if (injuryDurationMatches <= 0) {
            throw new IllegalArgumentException("Injury duration must be positive.");
        }
        this.injuryDurationMatches = injuryDurationMatches;
        this.newlyInjuredPlayers = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    @Override
    public void applyPostMatch(Match match) {
        Objects.requireNonNull(match, "Match cannot be null.");

        if (!match.isPlayed()) {
            return;
        }

        Team targetTeam = pickLosingSide(match);
        Lineup targetLineup = targetTeam == match.getHomeTeam()
                ? match.getHomeLineup()
                : match.getAwayLineup();

        if (targetLineup == null) {
            return;
        }

        List<? extends Player> starters = targetLineup.getSelectedPlayers();
        if (starters == null || starters.isEmpty()) {
            return;
        }

        // pick a player based on the scoreline so the same match always hits
        // the same slot (keeps tests stable)
        int index = Math.floorMod(match.getHomeScore() + match.getAwayScore(), starters.size());
        Player victim = starters.get(index);
        applyInjury(targetTeam, victim, injuryDurationMatches);
    }

    public void applyTrainingInjury(Team team, Player player, int durationMatches) {
        Objects.requireNonNull(team, "Team cannot be null.");
        Objects.requireNonNull(player, "Player cannot be null.");
        if (durationMatches <= 0) {
            throw new IllegalArgumentException("Training injury duration must be positive.");
        }
        if (!team.getRoster().contains(player)) {
            throw new IllegalArgumentException("Training injury player must belong to the affected team.");
        }

        applyInjury(team, player, durationMatches);
    }

    @Override
    public void recoverPlayers(Team team) {
        Objects.requireNonNull(team, "Team cannot be null.");

        for (Player player : team.getRoster()) {
            if (player.getInjuryMatchesRemaining() > 0) {
                if (newlyInjuredPlayers.remove(player)) {
                    continue;
                }
                player.recoverOneMatch();
            }
        }
    }

    public int getInjuryDurationMatches() {
        return injuryDurationMatches;
    }

    private void applyInjury(Team team, Player victim, int durationMatches) {
        if (victim != null && victim.getInjuryMatchesRemaining() == 0) {
            victim.injureForMatches(durationMatches);
            newlyInjuredPlayers.add(victim);
            team.setSelectedLineup(null);
        }
    }

    private Team pickLosingSide(Match match) {
        if (match.getHomeScore() < match.getAwayScore()) {
            return match.getHomeTeam();
        }
        if (match.getAwayScore() < match.getHomeScore()) {
            return match.getAwayTeam();
        }
        return match.getHomeTeam();
    }
}
