package com.playforgemanager.football;

import java.util.List;
import java.util.Objects;

public final class FootballTrainingEffectService {
    public void applyWeeklyTraining(FootballTeam team) {
        Objects.requireNonNull(team, "Football team cannot be null.");

        List<FootballPlayer> players = team.getFootballPlayers();
        players.forEach(FootballPlayer::clearWeeklyTrainingEffect);

        FootballTrainingPlan trainingPlan = team.getSelectedFootballTrainingPlan();
        if (trainingPlan == null) {
            return;
        }

        for (FootballPlayer player : players) {
            FootballTrainingEffect effect = buildEffect(player, trainingPlan);
            player.applyWeeklyTrainingEffect(effect);

            if (effect.acceleratedRecovery() && player.getInjuryMatchesRemaining() > 0) {
                player.recoverOneMatch();
            }
        }
    }

    FootballTrainingEffect buildEffect(FootballPlayer player, FootballTrainingPlan trainingPlan) {
        Objects.requireNonNull(player, "Football player cannot be null.");
        Objects.requireNonNull(trainingPlan, "Football training plan cannot be null.");

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
}
