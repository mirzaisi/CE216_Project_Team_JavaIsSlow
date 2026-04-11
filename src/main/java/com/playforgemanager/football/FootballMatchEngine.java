package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;

import java.util.Objects;
import java.util.Random;

public class FootballMatchEngine implements MatchEngine {

    private static final int MAX_BASE_GOALS = 4;
    private static final int HOME_BONUS_MAX = 3;

    private final long seedOffset;

    public FootballMatchEngine() {
        this(0L);
    }

    public FootballMatchEngine(long seedOffset) {
        this.seedOffset = seedOffset;
    }

    @Override
    public void simulate(Match match, Ruleset ruleset) {
        Objects.requireNonNull(match, "Match cannot be null.");
        Objects.requireNonNull(ruleset, "Ruleset cannot be null.");

        if (match.isPlayed()) {
            return;
        }

        checkLineup(match.getHomeLineup(), ruleset, "home");
        checkLineup(match.getAwayLineup(), ruleset, "away");

        long seed = seedFor(match);
        Random rng = new Random(seed);

        int home = rng.nextInt(MAX_BASE_GOALS);
        int away = rng.nextInt(MAX_BASE_GOALS);
        home += rng.nextInt(HOME_BONUS_MAX);

        match.setResult(home, away);
    }

    private long seedFor(Match match) {
        long h = hash(match.getHomeTeam().getName());
        long a = hash(match.getAwayTeam().getName());
        return (h * 1_000_003L) ^ a ^ seedOffset;
    }

    private long hash(String name) {
        return name == null ? 0L : name.hashCode();
    }

    private void checkLineup(Lineup lineup, Ruleset ruleset, String side) {
        if (lineup == null) {
            throw new IllegalArgumentException("The " + side + " lineup cannot be null.");
        }
        if (ruleset instanceof FootballRuleset fr) {
            fr.validateLineupOrThrow(lineup);
            return;
        }
        if (!ruleset.isValidLineup(lineup)) {
            throw new IllegalArgumentException("The " + side + " lineup is invalid.");
        }
    }
}
