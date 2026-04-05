package com.playforgemanager.football;

import com.playforgemanager.core.Player;
import java.util.Objects;

public class FootballPlayer extends Player {
    private final FootballPosition position;
    private final FootballAttributeProfile attributeProfile;

    public FootballPlayer(String id, String name, FootballPosition position, FootballAttributeProfile attributeProfile) {
        super(id, name);
        this.position = Objects.requireNonNull(position, "Football position cannot be null.");
        this.attributeProfile = Objects.requireNonNull(attributeProfile, "Attribute profile cannot be null.");
    }

    public FootballPosition getPosition() { return position; }
    public FootballAttributeProfile getAttributeProfile() { return attributeProfile; }
}
