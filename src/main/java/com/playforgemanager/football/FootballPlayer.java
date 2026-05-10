package com.playforgemanager.football;

import com.playforgemanager.core.Player;

import java.util.Objects;

public class FootballPlayer extends Player {
    private final FootballPosition position;
    private final FootballAttributeProfile attributeProfile;
    private FootballTrainingEffect weeklyTrainingEffect;

    public FootballPlayer(
            String id,
            String name,
            FootballPosition position,
            FootballAttributeProfile attributeProfile
    ) {
        super(id, name);

        // Stores football-specific player data after validation.
        this.position = Objects.requireNonNull(position, "Football position cannot be null.");
        this.attributeProfile = Objects.requireNonNull(attributeProfile, "Attribute profile cannot be null.");
        this.weeklyTrainingEffect = FootballTrainingEffect.none();
    }

    public FootballPosition getPosition() {
        return position;
    }

    public FootballAttributeProfile getAttributeProfile() {
        return attributeProfile;
    }

    public FootballTrainingEffect getWeeklyTrainingEffect() {
        return weeklyTrainingEffect;
    }

    public void applyWeeklyTrainingEffect(FootballTrainingEffect weeklyTrainingEffect) {
        this.weeklyTrainingEffect = Objects.requireNonNull(
                weeklyTrainingEffect,
                "Weekly training effect cannot be null."
        );
    }

    public void clearWeeklyTrainingEffect() {
        this.weeklyTrainingEffect = FootballTrainingEffect.none();
    }

    public FootballAttributeProfile getEffectiveAttributeProfile() {
        // Combines base attributes with the current weekly training effect.
        return new FootballAttributeProfile(
                clamp(attributeProfile.getAttack() + weeklyTrainingEffect.attackDelta()),
                clamp(attributeProfile.getDefense() + weeklyTrainingEffect.defenseDelta()),
                clamp(attributeProfile.getStamina() + weeklyTrainingEffect.staminaDelta()),
                clamp(attributeProfile.getPassing() + weeklyTrainingEffect.passingDelta()),
                clamp(attributeProfile.getSpeed() + weeklyTrainingEffect.speedDelta())
        );
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
