package com.playforgemanager.football;

import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Tactic;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FootballMatchEngine implements MatchEngine {
    private static final double HOME_ADVANTAGE_GOALS = 0.25;
    private static final double BASE_EXPECTED_GOALS = 1.20;
    private static final int MAX_GOALS = 10;

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

        FootballLineup homeLineup = requireFootballLineup(match.getHomeLineup(), ruleset, "home");
        FootballLineup awayLineup = requireFootballLineup(match.getAwayLineup(), ruleset, "away");

        TeamStrength homeStrength = TeamStrength.from(homeLineup, match.getHomeTactic());
        TeamStrength awayStrength = TeamStrength.from(awayLineup, match.getAwayTactic());

        Random rng = new Random(seedFor(match));
        int homeGoals = scoreGoals(expectedGoals(homeStrength, awayStrength, true), rng);
        int awayGoals = scoreGoals(expectedGoals(awayStrength, homeStrength, false), rng);

        homeGoals = clampGoals(homeGoals + seedOffsetHomeAdjustment());

        match.setResult(homeGoals, awayGoals);
    }

    private FootballLineup requireFootballLineup(Lineup lineup, Ruleset ruleset, String side) {
        if (lineup == null) {
            throw new IllegalArgumentException("The " + side + " lineup cannot be null.");
        }

        if (ruleset instanceof FootballRuleset footballRuleset) {
            footballRuleset.validateLineupOrThrow(lineup);
        } else if (!ruleset.isValidLineup(lineup)) {
            throw new IllegalArgumentException("The " + side + " lineup is invalid.");
        }

        if (!(lineup instanceof FootballLineup footballLineup)) {
            throw new IllegalArgumentException("Football match simulation requires FootballLineup for the " + side + " side.");
        }

        return footballLineup;
    }

    private double expectedGoals(TeamStrength attackingSide, TeamStrength defendingSide, boolean homeSide) {
        double attackAdvantage = attackingSide.attack() - defendingSide.defense();
        double controlAdvantage = attackingSide.control() - defendingSide.control();
        double fitnessAdvantage = attackingSide.fitness() - defendingSide.fitness();

        double expectedGoals = BASE_EXPECTED_GOALS
                + (attackAdvantage * 0.050)
                + (controlAdvantage * 0.018)
                + (fitnessAdvantage * 0.010);

        if (homeSide) {
            expectedGoals += HOME_ADVANTAGE_GOALS;
        }

        return Math.max(0.15, Math.min(5.75, expectedGoals));
    }

    private int scoreGoals(double expectedGoals, Random rng) {
        double deterministicNoise = (rng.nextDouble() - 0.50) * 0.70;
        double extraChance = rng.nextDouble();
        double adjustedExpectedGoals = expectedGoals + deterministicNoise;

        int goals = (int) Math.floor(adjustedExpectedGoals);
        double fractionalPart = adjustedExpectedGoals - goals;
        if (extraChance < fractionalPart) {
            goals++;
        }

        return clampGoals(goals);
    }

    private long seedFor(Match match) {
        long homeHash = hash(match.getHomeTeam().getName());
        long awayHash = hash(match.getAwayTeam().getName());
        return (homeHash * 1_000_003L) ^ awayHash ^ seedOffset;
    }

    private long hash(String name) {
        return name == null ? 0L : name.hashCode();
    }

    private int seedOffsetHomeAdjustment() {
        if (seedOffset == 0L) {
            return 0;
        }
        return (int) Math.floorMod(seedOffset, 3L) + 1;
    }

    private int clampGoals(int goals) {
        return Math.max(0, Math.min(MAX_GOALS, goals));
    }

    private record TeamStrength(double attack, double defense, double control, double fitness) {
        private static TeamStrength from(FootballLineup lineup, Tactic tactic) {
            Objects.requireNonNull(lineup, "Football lineup cannot be null.");

            List<FootballPlayer> starters = lineup.getStartingPlayers();
            if (starters.isEmpty()) {
                throw new IllegalArgumentException("Football lineup must have starting players.");
            }

            double attackTotal = 0.0;
            double defenseTotal = 0.0;
            double controlTotal = 0.0;
            double fitnessTotal = 0.0;

            for (FootballPlayer player : starters) {
                PlayerContribution contribution = PlayerContribution.from(player);
                attackTotal += contribution.attack();
                defenseTotal += contribution.defense();
                controlTotal += contribution.control();
                fitnessTotal += contribution.fitness();
            }

            int size = starters.size();
            TeamStrength base = new TeamStrength(
                    attackTotal / size,
                    defenseTotal / size,
                    controlTotal / size,
                    fitnessTotal / size
            );

            return base.withTactic(tactic);
        }

        private TeamStrength withTactic(Tactic tactic) {
            if (!(tactic instanceof FootballTactic footballTactic)) {
                return this;
            }

            double attackModifier = 0.0;
            double defenseModifier = 0.0;
            double controlModifier = 0.0;
            double fitnessModifier = 0.0;

            switch (footballTactic.getMentality()) {
                case ATTACKING -> {
                    attackModifier += 5.0;
                    controlModifier += 1.0;
                    defenseModifier -= 2.5;
                }
                case DEFENSIVE -> {
                    defenseModifier += 5.0;
                    attackModifier -= 2.5;
                    controlModifier -= 0.5;
                }
                case BALANCED -> {
                    attackModifier += 1.0;
                    defenseModifier += 1.0;
                }
            }

            attackModifier += (footballTactic.getAttackingWidth() - 50) * 0.06;
            controlModifier += (footballTactic.getAttackingWidth() - 50) * 0.025;
            defenseModifier += (footballTactic.getPressingIntensity() - 50) * 0.045;
            fitnessModifier -= Math.max(0, footballTactic.getPressingIntensity() - 70) * 0.035;

            return new TeamStrength(
                    clampRating(attack + attackModifier),
                    clampRating(defense + defenseModifier),
                    clampRating(control + controlModifier),
                    clampRating(fitness + fitnessModifier)
            );
        }

        private static double clampRating(double rating) {
            return Math.max(0.0, Math.min(100.0, rating));
        }
    }

    private record PlayerContribution(double attack, double defense, double control, double fitness) {
        private static PlayerContribution from(FootballPlayer player) {
            Objects.requireNonNull(player, "Football player cannot be null.");
            FootballAttributeProfile profile = player.getEffectiveAttributeProfile();

            return switch (player.getPosition()) {
                case GOALKEEPER -> new PlayerContribution(
                        weighted(profile, 0.05, 0.20, 0.15, 0.25, 0.10),
                        weighted(profile, 0.05, 0.62, 0.12, 0.15, 0.06),
                        weighted(profile, 0.05, 0.30, 0.10, 0.45, 0.10),
                        weighted(profile, 0.02, 0.10, 0.48, 0.10, 0.30)
                );
                case DEFENDER -> new PlayerContribution(
                        weighted(profile, 0.18, 0.12, 0.20, 0.32, 0.18),
                        weighted(profile, 0.08, 0.56, 0.18, 0.10, 0.08),
                        weighted(profile, 0.12, 0.20, 0.20, 0.36, 0.12),
                        weighted(profile, 0.06, 0.10, 0.48, 0.10, 0.26)
                );
                case MIDFIELDER -> new PlayerContribution(
                        weighted(profile, 0.28, 0.08, 0.16, 0.34, 0.14),
                        weighted(profile, 0.08, 0.34, 0.24, 0.24, 0.10),
                        weighted(profile, 0.14, 0.14, 0.22, 0.42, 0.08),
                        weighted(profile, 0.06, 0.08, 0.52, 0.12, 0.22)
                );
                case FORWARD -> new PlayerContribution(
                        weighted(profile, 0.54, 0.03, 0.13, 0.16, 0.14),
                        weighted(profile, 0.08, 0.22, 0.26, 0.14, 0.30),
                        weighted(profile, 0.28, 0.05, 0.16, 0.32, 0.19),
                        weighted(profile, 0.14, 0.04, 0.44, 0.08, 0.30)
                );
            };
        }

        private static double weighted(
                FootballAttributeProfile profile,
                double attackWeight,
                double defenseWeight,
                double staminaWeight,
                double passingWeight,
                double speedWeight
        ) {
            return profile.getAttack() * attackWeight
                    + profile.getDefense() * defenseWeight
                    + profile.getStamina() * staminaWeight
                    + profile.getPassing() * passingWeight
                    + profile.getSpeed() * speedWeight;
        }
    }
}
