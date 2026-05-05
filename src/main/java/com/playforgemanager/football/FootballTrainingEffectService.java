package com.playforgemanager.football;

import com.playforgemanager.core.InjuryPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class FootballTrainingEffectService {
    private static final int TRAINING_INJURY_DURATION_MATCHES = 2;

    public void applyWeeklyTraining(FootballTeam team) {
        applyWeeklyTraining(team, new FootballRuleset(), null);
    }

    public void applyWeeklyTraining(FootballTeam team, FootballRuleset ruleset) {
        applyWeeklyTraining(team, ruleset, null);
    }

    public void applyWeeklyTraining(FootballTeam team, FootballRuleset ruleset, InjuryPolicy injuryPolicy) {
        Objects.requireNonNull(team, "Football team cannot be null.");
        FootballRuleset activeRuleset = ruleset == null ? new FootballRuleset() : ruleset;

        List<FootballPlayer> players = team.getFootballPlayers();
        players.forEach(FootballPlayer::clearWeeklyTrainingEffect);

        FootballTrainingPlan trainingPlan = team.getSelectedFootballTrainingPlan();
        if (trainingPlan == null) {
            clearInvalidSelectedLineup(team, activeRuleset);
            return;
        }

        FootballTrainingCoachImpact coachImpact = FootballTrainingCoachImpact.from(team.getCoaches(), trainingPlan);

        for (FootballPlayer player : players) {
            boolean injuredAtStart = player.getInjuryMatchesRemaining() > 0;
            boolean selectableAtStart = player.isAvailable();

            if (injuredAtStart) {
                FootballTrainingEffect recoveryEffect = buildRecoverySafeEffect(player, trainingPlan, team.getCoaches());
                player.applyWeeklyTrainingEffect(recoveryEffect);
                applyRecovery(player, recoveryEffect, coachImpact);
                continue;
            }

            if (!selectableAtStart) {
                player.clearWeeklyTrainingEffect();
                continue;
            }

            FootballTrainingEffect effect = buildEffect(player, trainingPlan, team.getCoaches());
            player.applyWeeklyTrainingEffect(effect);
        }

        applyIntensityInjuryRisk(team, trainingPlan, injuryPolicy);
        clearInvalidSelectedLineup(team, activeRuleset);
    }

    FootballTrainingEffect buildEffect(FootballPlayer player, FootballTrainingPlan trainingPlan) {
        return buildEffect(player, trainingPlan, List.of());
    }

    FootballTrainingEffect buildEffect(FootballPlayer player, FootballTrainingPlan trainingPlan, List<FootballCoach> coaches) {
        Objects.requireNonNull(player, "Football player cannot be null.");
        Objects.requireNonNull(trainingPlan, "Football training plan cannot be null.");
        Objects.requireNonNull(coaches, "Coach list cannot be null.");

        FootballTrainingEffect baseEffect = buildBaseEffect(player, trainingPlan);
        FootballTrainingCoachImpact coachImpact = FootballTrainingCoachImpact.from(coaches, trainingPlan);
        return coachImpact.applyTo(baseEffect, player.getPosition(), trainingPlan.resolveFocusType());
    }

    private FootballTrainingEffect buildRecoverySafeEffect(
            FootballPlayer player,
            FootballTrainingPlan trainingPlan,
            List<FootballCoach> coaches
    ) {
        FootballTrainingEffect fullEffect = buildEffect(player, trainingPlan, coaches);
        FootballTrainingPlan.FocusType focusType = trainingPlan.resolveFocusType();

        if (focusType == FootballTrainingPlan.FocusType.RECOVERY || trainingPlan.isRecoveryIncluded()) {
            return new FootballTrainingEffect(
                    0,
                    Math.min(1, fullEffect.defenseDelta()),
                    fullEffect.staminaDelta(),
                    0,
                    0,
                    true
            );
        }

        return FootballTrainingEffect.none();
    }

    private FootballTrainingEffect buildBaseEffect(FootballPlayer player, FootballTrainingPlan trainingPlan) {
        int intensityTier = toTier(trainingPlan.getIntensity());
        int conditioningTier = toTier(trainingPlan.getConditioningLoad());
        int tacticalTier = toTier(trainingPlan.getTacticalLoad());

        int attack = 0;
        int defense = 0;
        int stamina = 0;
        int passing = 0;
        int speed = 0;
        boolean acceleratedRecovery = trainingPlan.isRecoveryIncluded()
                || trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.RECOVERY;

        switch (trainingPlan.resolveFocusType()) {
            case ATTACKING -> {
                attack += scaledBoost(intensityTier);
                passing += scaledBoost(tacticalTier);
                speed += conditioningTier >= 2 ? 1 : 0;
            }
            case DEFENSIVE -> {
                defense += scaledBoost(intensityTier);
                stamina += conditioningTier >= 1 ? 1 : 0;
                passing += tacticalTier >= 2 ? 1 : 0;
            }
            case FITNESS -> {
                stamina += 1 + scaledBoost(conditioningTier);
                speed += intensityTier >= 1 ? 1 : 0;
            }
            case POSSESSION -> {
                passing += 1 + scaledBoost(tacticalTier);
                stamina += conditioningTier >= 2 ? 1 : 0;
                attack += intensityTier >= 3 ? 1 : 0;
            }
            case RECOVERY -> {
                stamina += 1 + (trainingPlan.isRecoveryIncluded() ? 1 : 0);
                defense += tacticalTier >= 2 ? 1 : 0;
            }
            case BALANCED -> {
                attack += intensityTier >= 2 ? 1 : 0;
                defense += intensityTier >= 2 ? 1 : 0;
                passing += tacticalTier >= 1 ? 1 : 0;
                stamina += conditioningTier >= 1 ? 1 : 0;
            }
        }

        switch (player.getPosition()) {
            case GOALKEEPER -> {
                defense += 1;
                passing += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.POSSESSION ? 1 : 0;
                attack = Math.min(attack, 1);
                speed = Math.min(speed, 1);
            }
            case DEFENDER -> {
                defense += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.DEFENSIVE ? 1 : 0;
                stamina += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.FITNESS ? 1 : 0;
            }
            case MIDFIELDER -> {
                passing += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.POSSESSION ? 1 : 0;
                stamina += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.FITNESS ? 1 : 0;
            }
            case FORWARD -> {
                attack += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.ATTACKING ? 1 : 0;
                speed += trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.FITNESS ? 1 : 0;
            }
        }

        if (trainingPlan.isRecoveryIncluded()) {
            stamina += 1;
        }

        return new FootballTrainingEffect(attack, defense, stamina, passing, speed, acceleratedRecovery);
    }

    private void applyRecovery(FootballPlayer player, FootballTrainingEffect effect, FootballTrainingCoachImpact coachImpact) {
        if (player.getInjuryMatchesRemaining() <= 0) {
            return;
        }

        int recoverySteps = effect.acceleratedRecovery() ? 1 : 0;
        if (coachImpact.extraRecoveryStep()) {
            recoverySteps++;
        }

        for (int i = 0; i < recoverySteps && player.getInjuryMatchesRemaining() > 0; i++) {
            player.recoverOneMatch();
        }
    }

    private void applyIntensityInjuryRisk(
            FootballTeam team,
            FootballTrainingPlan trainingPlan,
            InjuryPolicy injuryPolicy
    ) {
        int injuryCount = trainingInjuryCount(trainingPlan);
        if (injuryCount == 0) {
            return;
        }

        List<FootballPlayer> candidates = team.getFootballPlayers().stream()
                .filter(FootballPlayer::isAvailable)
                .filter(player -> player.getInjuryMatchesRemaining() == 0)
                .sorted(Comparator
                        .comparingInt((FootballPlayer player) -> player.getAttributeProfile().getStamina())
                        .thenComparing(FootballPlayer::getId))
                .limit(injuryCount)
                .toList();

        for (FootballPlayer player : candidates) {
            applyTrainingInjury(team, player, injuryPolicy);
        }
    }

    private int trainingInjuryCount(FootballTrainingPlan trainingPlan) {
        if (trainingPlan.isRecoveryIncluded()
                || trainingPlan.resolveFocusType() == FootballTrainingPlan.FocusType.RECOVERY) {
            return 0;
        }

        if (trainingPlan.getIntensity() >= 95 && trainingPlan.getConditioningLoad() >= 85) {
            return 2;
        }

        if (trainingPlan.getIntensity() >= 90
                || (trainingPlan.getIntensity() >= 85 && trainingPlan.getConditioningLoad() >= 80)) {
            return 1;
        }

        return 0;
    }

    private void applyTrainingInjury(FootballTeam team, FootballPlayer player, InjuryPolicy injuryPolicy) {
        player.clearWeeklyTrainingEffect();

        if (injuryPolicy instanceof FootballInjuryPolicy footballInjuryPolicy) {
            footballInjuryPolicy.applyTrainingInjury(team, player, TRAINING_INJURY_DURATION_MATCHES);
            return;
        }

        if (player.getInjuryMatchesRemaining() == 0) {
            player.injureForMatches(TRAINING_INJURY_DURATION_MATCHES);
            team.setSelectedLineup(null);
        }
    }

    private void clearInvalidSelectedLineup(FootballTeam team, FootballRuleset ruleset) {
        FootballLineup selectedLineup = team.getSelectedFootballLineup();
        if (selectedLineup == null) {
            return;
        }

        try {
            ruleset.validateLineupOrThrow(selectedLineup);
        } catch (RuntimeException ex) {
            team.setSelectedLineup(null);
        }
    }

    private int toTier(int load) {
        if (load >= 75) {
            return 3;
        }
        if (load >= 50) {
            return 2;
        }
        if (load >= 25) {
            return 1;
        }
        return 0;
    }

    private int scaledBoost(int tier) {
        return switch (tier) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
    }

    private enum CoachingArea {
        GENERAL,
        ATTACKING,
        DEFENSIVE,
        FITNESS,
        POSSESSION,
        RECOVERY;

        static CoachingArea fromTrainingFocus(FootballTrainingPlan.FocusType focusType) {
            return switch (focusType) {
                case ATTACKING -> ATTACKING;
                case DEFENSIVE -> DEFENSIVE;
                case FITNESS -> FITNESS;
                case POSSESSION -> POSSESSION;
                case RECOVERY -> RECOVERY;
                case BALANCED -> GENERAL;
            };
        }

        static CoachingArea fromSpecialization(String specialization) {
            String normalized = Objects.requireNonNull(specialization, "Coach specialization cannot be null.")
                    .toLowerCase(Locale.ROOT)
                    .trim();

            if (normalized.contains("attack") || normalized.contains("finish") || normalized.contains("press")
                    || normalized.contains("counter") || normalized.contains("offensive")) {
                return ATTACKING;
            }
            if (normalized.contains("defen") || normalized.contains("compact") || normalized.contains("organiz")) {
                return DEFENSIVE;
            }
            if (normalized.contains("fit") || normalized.contains("condition") || normalized.contains("stamina")
                    || normalized.contains("physical")) {
                return FITNESS;
            }
            if (normalized.contains("possess") || normalized.contains("control") || normalized.contains("pass")
                    || normalized.contains("tactic")) {
                return POSSESSION;
            }
            if (normalized.contains("recover") || normalized.contains("medical") || normalized.contains("rehab")) {
                return RECOVERY;
            }
            return GENERAL;
        }
    }

    private record FootballTrainingCoachImpact(
            CoachingArea planArea,
            CoachingArea coachArea,
            int rating,
            boolean exactSpecializationMatch,
            boolean generalSupport
    ) {
        private static FootballTrainingCoachImpact from(List<FootballCoach> coaches, FootballTrainingPlan trainingPlan) {
            Objects.requireNonNull(coaches, "Coach list cannot be null.");
            Objects.requireNonNull(trainingPlan, "Football training plan cannot be null.");

            CoachingArea planArea = CoachingArea.fromTrainingFocus(trainingPlan.resolveFocusType());

            FootballCoach bestExactCoach = coaches.stream()
                    .filter(Objects::nonNull)
                    .filter(coach -> CoachingArea.fromSpecialization(coach.getSpecialization()) == planArea)
                    .max(Comparator.comparingInt(FootballCoach::getCoachingRating))
                    .orElse(null);

            if (bestExactCoach != null) {
                return new FootballTrainingCoachImpact(planArea, planArea, bestExactCoach.getCoachingRating(), true, false);
            }

            FootballCoach bestGeneralCoach = coaches.stream()
                    .filter(Objects::nonNull)
                    .filter(coach -> CoachingArea.fromSpecialization(coach.getSpecialization()) == CoachingArea.GENERAL)
                    .max(Comparator.comparingInt(FootballCoach::getCoachingRating))
                    .orElse(null);

            if (bestGeneralCoach != null) {
                return new FootballTrainingCoachImpact(planArea, CoachingArea.GENERAL, bestGeneralCoach.getCoachingRating(), false, true);
            }

            FootballCoach bestAvailableCoach = coaches.stream()
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingInt(FootballCoach::getCoachingRating))
                    .orElse(null);

            if (bestAvailableCoach != null) {
                return new FootballTrainingCoachImpact(
                        planArea,
                        CoachingArea.fromSpecialization(bestAvailableCoach.getSpecialization()),
                        bestAvailableCoach.getCoachingRating(),
                        false,
                        false
                );
            }

            return new FootballTrainingCoachImpact(planArea, CoachingArea.GENERAL, 0, false, false);
        }

        private FootballTrainingEffect applyTo(FootballTrainingEffect baseEffect, FootballPosition position, FootballTrainingPlan.FocusType focusType) {
            Objects.requireNonNull(baseEffect, "Base training effect cannot be null.");
            Objects.requireNonNull(position, "Football position cannot be null.");
            Objects.requireNonNull(focusType, "Training focus type cannot be null.");

            int tier = ratingTier();
            int specializationBonus = effectiveSpecializationBonus(tier);
            int attack = baseEffect.attackDelta();
            int defense = baseEffect.defenseDelta();
            int stamina = baseEffect.staminaDelta();
            int passing = baseEffect.passingDelta();
            int speed = baseEffect.speedDelta();
            boolean acceleratedRecovery = baseEffect.acceleratedRecovery();

            if (specializationBonus > 0) {
                switch (planArea) {
                    case ATTACKING -> {
                        attack += specializationBonus;
                        passing += tier >= 2 ? 1 : 0;
                        speed += tier >= 3 ? 1 : 0;
                    }
                    case DEFENSIVE -> {
                        defense += specializationBonus;
                        stamina += tier >= 2 ? 1 : 0;
                        passing += tier >= 3 ? 1 : 0;
                    }
                    case FITNESS -> {
                        stamina += specializationBonus;
                        speed += tier >= 2 ? 1 : 0;
                    }
                    case POSSESSION -> {
                        passing += specializationBonus;
                        stamina += tier >= 2 ? 1 : 0;
                        attack += tier >= 3 ? 1 : 0;
                    }
                    case RECOVERY -> {
                        stamina += Math.max(1, specializationBonus - 1);
                        defense += tier >= 3 ? 1 : 0;
                        acceleratedRecovery = acceleratedRecovery || tier >= 2;
                    }
                    case GENERAL -> {
                        attack += tier >= 2 ? 1 : 0;
                        defense += tier >= 2 ? 1 : 0;
                        stamina += tier >= 1 ? 1 : 0;
                        passing += tier >= 2 ? 1 : 0;
                    }
                }
            }

            if (!exactSpecializationMatch && !generalSupport && tier == 0) {
                attack = Math.max(0, attack - reliabilityPenalty(attack, focusType, FootballTrainingPlan.FocusType.ATTACKING));
                defense = Math.max(0, defense - reliabilityPenalty(defense, focusType, FootballTrainingPlan.FocusType.DEFENSIVE));
                stamina = Math.max(0, stamina - reliabilityPenalty(stamina, focusType, FootballTrainingPlan.FocusType.FITNESS));
                passing = Math.max(0, passing - reliabilityPenalty(passing, focusType, FootballTrainingPlan.FocusType.POSSESSION));
            }

            switch (position) {
                case GOALKEEPER -> {
                    attack = Math.min(attack, 2);
                    speed = Math.min(speed, 2);
                }
                case DEFENDER -> defense += exactSpecializationMatch && planArea == CoachingArea.DEFENSIVE && tier >= 3 ? 1 : 0;
                case MIDFIELDER -> passing += exactSpecializationMatch && planArea == CoachingArea.POSSESSION && tier >= 3 ? 1 : 0;
                case FORWARD -> attack += exactSpecializationMatch && planArea == CoachingArea.ATTACKING && tier >= 3 ? 1 : 0;
            }

            return new FootballTrainingEffect(attack, defense, stamina, passing, speed, acceleratedRecovery);
        }

        private int ratingTier() {
            if (rating >= 85) {
                return 3;
            }
            if (rating >= 70) {
                return 2;
            }
            if (rating >= 50) {
                return 1;
            }
            return 0;
        }

        private int effectiveSpecializationBonus(int tier) {
            if (exactSpecializationMatch) {
                return tier;
            }
            if (generalSupport) {
                return Math.min(1, tier);
            }
            return 0;
        }

        private boolean extraRecoveryStep() {
            return planArea == CoachingArea.RECOVERY && exactSpecializationMatch && rating >= 85;
        }

        private int reliabilityPenalty(int currentDelta, FootballTrainingPlan.FocusType activeFocus, FootballTrainingPlan.FocusType protectedFocus) {
            if (currentDelta == 0 || activeFocus == protectedFocus) {
                return 0;
            }
            return 1;
        }
    }
}
