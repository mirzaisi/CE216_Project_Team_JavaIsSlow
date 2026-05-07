package com.playforgemanager.handball;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Tactic;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HandballMatchEngine implements MatchEngine {
    private static final double HOME_ADVANTAGE_GOALS = 1.10;
    private static final double BASE_EXPECTED_GOALS = 26.0;
    private static final int MAX_GOALS = 45;

    private final long seedOffset;

    public HandballMatchEngine() {
        this(0L);
    }

    public HandballMatchEngine(long seedOffset) {
        this.seedOffset = seedOffset;
    }

    @Override
    public void simulate(Match match, Ruleset ruleset) {
        Objects.requireNonNull(match, "Match cannot be null.");
        Objects.requireNonNull(ruleset, "Ruleset cannot be null.");

        if (match.isPlayed()) {
            return;
        }

        HandballLineup homeLineup = requireHandballLineup(match.getHomeLineup(), ruleset, "home");
        HandballLineup awayLineup = requireHandballLineup(match.getAwayLineup(), ruleset, "away");

        TeamPlan homePlan = TeamPlan.from(homeLineup, match.getHomeTactic());
        TeamPlan awayPlan = TeamPlan.from(awayLineup, match.getAwayTactic());

        Random rng = new Random(seedFor(match));
        int homeGoals = scoreGoals(expectedGoals(homePlan, awayPlan, true), rng);
        int awayGoals = scoreGoals(expectedGoals(awayPlan, homePlan, false), rng);

        match.setResult(homeGoals, awayGoals);
    }

    private HandballLineup requireHandballLineup(Lineup lineup, Ruleset ruleset, String side) {
        if (lineup == null) {
            throw new IllegalArgumentException("The " + side + " lineup cannot be null.");
        }

        if (ruleset instanceof HandballRuleset handballRuleset) {
            handballRuleset.validateLineupOrThrow(lineup);
        } else if (!ruleset.isValidLineup(lineup)) {
            throw new IllegalArgumentException("The " + side + " lineup is invalid.");
        }

        if (!(lineup instanceof HandballLineup handballLineup)) {
            throw new IllegalArgumentException(
                    "Handball match simulation requires HandballLineup for the " + side + " side."
            );
        }

        return handballLineup;
    }

    private double expectedGoals(TeamPlan attackingSide, TeamPlan defendingSide, boolean homeSide) {
        double attackAdvantage = attackingSide.attack() - defendingSide.defense();
        double paceAdvantage = attackingSide.pace() - defendingSide.pace();
        double keeperAdvantage = attackingSide.finishing() - defendingSide.goalkeeping();

        double expectedGoals = BASE_EXPECTED_GOALS
                + (attackAdvantage * 0.120)
                + (paceAdvantage * 0.060)
                + (keeperAdvantage * 0.070)
                + attackingSide.goalIntent()
                - defendingSide.slowdown();

        if (homeSide) {
            expectedGoals += HOME_ADVANTAGE_GOALS;
        }

        return Math.max(12.0, Math.min(38.0, expectedGoals));
    }

    private int scoreGoals(double expectedGoals, Random rng) {
        double deterministicNoise = (rng.nextDouble() - 0.50) * 4.0;
        double extraChance = rng.nextDouble();
        double adjustedExpectedGoals = expectedGoals + deterministicNoise;

        int goals = (int) Math.floor(adjustedExpectedGoals);
        double fractionalPart = adjustedExpectedGoals - goals;
        if (extraChance < fractionalPart) {
            goals++;
        }

        return Math.max(0, Math.min(MAX_GOALS, goals));
    }

    private long seedFor(Match match) {
        long homeHash = hash(match.getHomeTeam().getName());
        long awayHash = hash(match.getAwayTeam().getName());
        return (homeHash * 1_000_003L) ^ awayHash ^ seedOffset;
    }

    private long hash(String name) {
        return name == null ? 0L : name.hashCode();
    }

    private record TeamPlan(double attack, double defense, double pace, double finishing,
                            double goalkeeping, double goalIntent, double slowdown) {
        private static TeamPlan from(HandballLineup lineup, Tactic tactic) {
            TeamStrength baseStrength = TeamStrength.from(lineup);
            TacticImpact tacticImpact = TacticImpact.from(tactic);

            return new TeamPlan(
                    clampRating(baseStrength.attack() + tacticImpact.attackModifier()),
                    clampRating(baseStrength.defense() + tacticImpact.defenseModifier()),
                    clampRating(baseStrength.pace() + tacticImpact.paceModifier()),
                    clampRating(baseStrength.finishing() + tacticImpact.finishingModifier()),
                    clampRating(baseStrength.goalkeeping() + tacticImpact.goalkeepingModifier()),
                    tacticImpact.goalIntent(),
                    tacticImpact.slowdown()
            );
        }

        private static double clampRating(double rating) {
            return Math.max(0.0, Math.min(100.0, rating));
        }
    }

    private record TeamStrength(double attack, double defense, double pace, double finishing, double goalkeeping) {
        private static TeamStrength from(HandballLineup lineup) {
            Objects.requireNonNull(lineup, "Handball lineup cannot be null.");

            List<HandballPlayer> starters = lineup.getStartingPlayers();
            if (starters.isEmpty()) {
                throw new IllegalArgumentException("Handball lineup must have starting players.");
            }

            double attackTotal = 0.0;
            double defenseTotal = 0.0;
            double paceTotal = 0.0;
            double finishingTotal = 0.0;
            double goalkeepingTotal = 0.0;

            for (HandballPlayer player : starters) {
                PlayerContribution contribution = PlayerContribution.from(player);
                attackTotal += contribution.attack();
                defenseTotal += contribution.defense();
                paceTotal += contribution.pace();
                finishingTotal += contribution.finishing();
                goalkeepingTotal += contribution.goalkeeping();
            }

            int size = starters.size();
            return new TeamStrength(
                    attackTotal / size,
                    defenseTotal / size,
                    paceTotal / size,
                    finishingTotal / size,
                    goalkeepingTotal / size
            );
        }
    }

    private record PlayerContribution(double attack, double defense, double pace, double finishing, double goalkeeping) {
        private static PlayerContribution from(HandballPlayer player) {
            HandballAttributeProfile profile = player.getAttributeProfile();
            if (player.getPosition().isGoalkeeper()) {
                return new PlayerContribution(
                        profile.getPassing() * 0.30 + profile.getSpeed() * 0.10,
                        profile.getDefense() * 0.40 + profile.getReflexes() * 0.35,
                        profile.getSpeed() * 0.35,
                        profile.getShooting() * 0.15,
                        profile.getReflexes() * 0.85 + profile.getDefense() * 0.15
                );
            }

            return new PlayerContribution(
                    profile.getShooting() * 0.50 + profile.getPassing() * 0.25 + profile.getSpeed() * 0.15,
                    profile.getDefense() * 0.55 + profile.getSpeed() * 0.15 + profile.getPassing() * 0.10,
                    profile.getSpeed() * 0.45 + profile.getPassing() * 0.20,
                    profile.getShooting() * 0.55 + profile.getPassing() * 0.15,
                    profile.getReflexes() * 0.10
            );
        }
    }

    private record TacticImpact(double attackModifier, double defenseModifier, double paceModifier,
                                double finishingModifier, double goalkeepingModifier,
                                double goalIntent, double slowdown) {
        private static final TacticImpact NEUTRAL = new TacticImpact(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        private static TacticImpact from(Tactic tactic) {
            if (!(tactic instanceof HandballTactic handballTactic)) {
                return NEUTRAL;
            }

            double attackModifier = 0.0;
            double defenseModifier = 0.0;
            double paceModifier = 0.0;
            double finishingModifier = 0.0;
            double goalkeepingModifier = 0.0;
            double goalIntent = 0.0;
            double slowdown = 0.0;

            switch (handballTactic.getTempo()) {
                case FAST_BREAK -> {
                    attackModifier += 5.0;
                    paceModifier += 6.0;
                    finishingModifier += 3.0;
                    defenseModifier -= 1.0;
                    goalIntent += 2.4;
                }
                case CONTROLLED -> {
                    defenseModifier += 4.0;
                    goalkeepingModifier += 2.0;
                    paceModifier -= 2.0;
                    slowdown += 1.8;
                }
                case BALANCED -> {
                    attackModifier += 1.0;
                    defenseModifier += 1.0;
                    paceModifier += 1.0;
                }
            }

            attackModifier += (handballTactic.getTransitionSpeed() - 50) * 0.10;
            paceModifier += (handballTactic.getTransitionSpeed() - 50) * 0.14;
            finishingModifier += (handballTactic.getTransitionSpeed() - 50) * 0.05;

            defenseModifier += (handballTactic.getPressureLevel() - 50) * 0.12;
            goalkeepingModifier += (handballTactic.getPressureLevel() - 50) * 0.05;
            slowdown += Math.max(0, handballTactic.getPressureLevel() - 60) * 0.03;

            return new TacticImpact(
                    attackModifier,
                    defenseModifier,
                    paceModifier,
                    finishingModifier,
                    goalkeepingModifier,
                    goalIntent,
                    slowdown
            );
        }
    }
}
