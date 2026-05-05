package com.playforgemanager.football;

import com.playforgemanager.core.Player;

import java.util.Objects;

public class FootballPlayer extends Player {
    private final FootballPosition position;
    private final FootballAttributeProfile attributeProfile;

    private int weeklyAttackModifier;
    private int weeklyDefenseModifier;
    private int weeklyStaminaModifier;
    private int weeklyPassingModifier;
    private int weeklySpeedModifier;

    public FootballPlayer(String id, String name, FootballPosition position, FootballAttributeProfile attributeProfile) {
        super(id, name);
        this.position = Objects.requireNonNull(position, "Football position cannot be null.");
        this.attributeProfile = Objects.requireNonNull(attributeProfile, "Attribute profile cannot be null.");
    }

    public FootballPosition getPosition() {
        return position;
    }

    public FootballAttributeProfile getAttributeProfile() {
        return attributeProfile;
    }

    public FootballAttributeProfile getEffectiveAttributeProfile() {
        return new FootballAttributeProfile(
                clamp(attributeProfile.getAttack() + weeklyAttackModifier),
                clamp(attributeProfile.getDefense() + weeklyDefenseModifier),
                clamp(attributeProfile.getStamina() + weeklyStaminaModifier),
                clamp(attributeProfile.getPassing() + weeklyPassingModifier),
                clamp(attributeProfile.getSpeed() + weeklySpeedModifier)
        );
    }

    public void clearWeeklyTrainingEffect() {
        weeklyAttackModifier = 0;
        weeklyDefenseModifier = 0;
        weeklyStaminaModifier = 0;
        weeklyPassingModifier = 0;
        weeklySpeedModifier = 0;
    }

    public void applyWeeklyTrainingEffect(FootballTrainingEffect effect) {
        Objects.requireNonNull(effect, "Training effect cannot be null.");
        weeklyAttackModifier = effect.attackDelta();
        weeklyDefenseModifier = effect.defenseDelta();
        weeklyStaminaModifier = effect.staminaDelta();
        weeklyPassingModifier = effect.passingDelta();
        weeklySpeedModifier = effect.speedDelta();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
