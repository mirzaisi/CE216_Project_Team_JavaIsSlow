package com.playforgemanager.handball;

import com.playforgemanager.core.Player;

import java.util.Objects;

public class HandballPlayer extends Player {
    private final HandballPosition position;
    private final HandballAttributeProfile attributeProfile;

    public HandballPlayer(
            String id,
            String name,
            HandballPosition position,
            HandballAttributeProfile attributeProfile
    ) {
        super(id, name);

        // Stores handball-specific player data after validation.
        this.position = Objects.requireNonNull(position, "Handball position cannot be null.");
        this.attributeProfile = Objects.requireNonNull(attributeProfile, "Attribute profile cannot be null.");
    }

    public HandballPosition getPosition() {
        return position;
    }

    public HandballAttributeProfile getAttributeProfile() {
        return attributeProfile;
    }
}
