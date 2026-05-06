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

        TeamPlan homePlan = TeamPlan.from(homeLineup, match.getHomeTactic());
        TeamPlan awayPlan = TeamPlan.from(awayLineup, match.getAwayTactic());

        Random rng = new Random(seedFor(match));
        int homeGoals = scoreGoals(expectedGoals(homePlan, awayPlan, true), rng);
        int awayGoals = scoreGoals(expectedGoals(awayPlan, homePlan, false), rng);

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

    private double expectedGoals(TeamPlan attackingSide, TeamPlan defendingSide, boolean homeSide) {
        double attackAdvantage = attackingSide.attack() - defendingSide.defense();
        double controlAdvantage = attackingSide.control() - defendingSide.control();
        double fitnessAdvantage = attackingSide.fitness() - defendingSide.fitness();

        double expectedGoals = BASE_EXPECTED_GOALS
                + (attackAdvantage * 0.050)
                + (controlAdvantage * 0.018)
                + (fitnessAdvantage * 0.010)
                + attackingSide.goalIntent()
                + defendingSide.riskExposure();

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

    private record TeamPlan(double attack, double defense, double control, double fitness,
                            double goalIntent, double riskExposure) {
        private static TeamPlan from(FootballLineup lineup, Tactic tactic) {
            TeamStrength baseStrength = TeamStrength.from(lineup);
            TacticImpact tacticImpact = TacticImpact.from(tactic);

            return new TeamPlan(
                    clampRating(baseStrength.attack() + tacticImpact.attackModifier()),
                    clampRating(baseStrength.defense() + tacticImpact.defenseModifier()),
                    clampRating(baseStrength.control() + tacticImpact.controlModifier()),
                    clampRating(baseStrength.fitness() + tacticImpact.fitnessModifier()),
                    tacticImpact.goalIntent(),
                    tacticImpact.riskExposure()
            );
        }

        private static double clampRating(double rating) {
            return Math.max(0.0, Math.min(100.0, rating));
        }
    }

    private record TeamStrength(double attack, double defense, double control, double fitness) {
        private static TeamStrength from(FootballLineup lineup) {
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
            return new TeamStrength(
                    attackTotal / size,
                    defenseTotal / size,
                    controlTotal / size,
                    fitnessTotal / size
            );
        }
    }

    private record TacticImpact(double attackModifier, double defenseModifier, double controlModifier,
                                double fitnessModifier, double goalIntent, double riskExposure) {
        private static final TacticImpact NEUTRAL = new TacticImpact(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        private static TacticImpact from(Tactic tactic) {
            if (!(tactic instanceof FootballTactic footballTactic)) {
                return NEUTRAL;
            }

            double attackModifier = 0.0;
            double defenseModifier = 0.0;
            double controlModifier = 0.0;
            double fitnessModifier = 0.0;
            double goalIntent = 0.0;
            double riskExposure = 0.0;

            switch (footballTactic.getMentality()) {
                case ATTACKING -> {
                    attackModifier += 6.0;
                    controlModifier += 1.0;
                    defenseModifier -= 2.5;
                    goalIntent += 0.55;
                    riskExposure += 0.22;
                }
                case DEFENSIVE -> {
                    defenseModifier += 6.0;
                    attackModifier -= 2.5;
                    controlModifier -= 0.6;
                    goalIntent -= 0.30;
                    riskExposure -= 0.20;
                }
                case BALANCED -> {
                    attackModifier += 1.0;
                    defenseModifier += 1.0;
                    controlModifier += 0.6;
                }
            }

            SliderImpact pressingImpact = pressingImpact(footballTactic.getPressingIntensity());
            attackModifier += pressingImpact.attackModifier();
            defenseModifier += pressingImpact.defenseModifier();
            controlModifier += pressingImpact.controlModifier();
            fitnessModifier += pressingImpact.fitnessModifier();
            riskExposure += pressingImpact.riskExposure();

            SliderImpact widthImpact = widthImpact(footballTactic.getAttackingWidth());
            attackModifier += widthImpact.attackModifier();
            defenseModifier += widthImpact.defenseModifier();
            controlModifier += widthImpact.controlModifier();
            fitnessModifier += widthImpact.fitnessModifier();
            riskExposure += widthImpact.riskExposure();

            FormationImpact formationImpact = FormationImpact.from(footballTactic.getFormation());
            attackModifier += formationImpact.attackModifier();
            defenseModifier += formationImpact.defenseModifier();
            controlModifier += formationImpact.controlModifier();
            riskExposure += formationImpact.riskExposure();

            return new TacticImpact(
                    attackModifier,
                    defenseModifier,
                    controlModifier,
                    fitnessModifier,
                    clampGoalIntent(goalIntent),
                    clampRiskExposure(riskExposure)
            );
        }

        private static SliderImpact pressingImpact(int pressingIntensity) {
            double normalized = (pressingIntensity - 50) / 10.0;
            double attackModifier = normalized * 0.15;
            double defenseModifier = normalized * 0.55;
            double controlModifier = normalized * 0.15;
            double fitnessModifier = -Math.max(0, pressingIntensity - 70) * 0.060;
            double riskExposure = Math.max(0, pressingIntensity - 75) * 0.006;

            if (pressingIntensity < 35) {
                defenseModifier -= (35 - pressingIntensity) * 0.040;
                fitnessModifier += (35 - pressingIntensity) * 0.015;
            }

            return new SliderImpact(attackModifier, defenseModifier, controlModifier, fitnessModifier, riskExposure);
        }

        private static SliderImpact widthImpact(int attackingWidth) {
            double normalized = (attackingWidth - 50) / 10.0;
            double attackModifier = normalized * 0.45;
            double defenseModifier = 0.0;
            double controlModifier = normalized * 0.20;
            double riskExposure = Math.max(0, attackingWidth - 75) * 0.007;

            if (attackingWidth > 75) {
                defenseModifier -= (attackingWidth - 75) * 0.045;
            } else if (attackingWidth < 35) {
                defenseModifier += (35 - attackingWidth) * 0.035;
                controlModifier -= (35 - attackingWidth) * 0.025;
            }

            return new SliderImpact(attackModifier, defenseModifier, controlModifier, 0.0, riskExposure);
        }

        private static double clampGoalIntent(double value) {
            return Math.max(-0.45, Math.min(0.75, value));
        }

        private static double clampRiskExposure(double value) {
            return Math.max(-0.30, Math.min(0.45, value));
        }
    }

    private record SliderImpact(double attackModifier, double defenseModifier, double controlModifier,
                                double fitnessModifier, double riskExposure) {
    }

    private record FormationImpact(double attackModifier, double defenseModifier, double controlModifier,
                                   double riskExposure) {
        private static final FormationImpact NEUTRAL = new FormationImpact(0.0, 0.0, 0.0, 0.0);

        private static FormationImpact from(String formation) {
            int[] lines = parseFormation(formation);
            if (lines.length == 0) {
                return NEUTRAL;
            }

            int defenders = lines[0];
            int forwards = lines[lines.length - 1];
            int midfielders = 0;
            for (int i = 1; i < lines.length - 1; i++) {
                midfielders += lines[i];
            }

            double attackModifier = (forwards - 2) * 1.40;
            double defenseModifier = (defenders - 4) * 1.25;
            double controlModifier = (midfielders - 4) * 0.80;
            double riskExposure = Math.max(0, forwards - 2) * 0.070 - Math.max(0, defenders - 4) * 0.055;

            return new FormationImpact(attackModifier, defenseModifier, controlModifier, riskExposure);
        }

        private static int[] parseFormation(String formation) {
            if (formation == null || formation.isBlank()) {
                return new int[0];
            }

            String[] parts = formation.trim().split("-");
            if (parts.length < 2) {
                return new int[0];
            }

            int[] values = new int[parts.length];
            int totalOutfieldPlayers = 0;
            for (int i = 0; i < parts.length; i++) {
                try {
                    values[i] = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException ex) {
                    return new int[0];
                }

                if (values[i] < 0) {
                    return new int[0];
                }
                totalOutfieldPlayers += values[i];
            }

            return totalOutfieldPlayers == 10 ? values : new int[0];
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
