package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballPlayerTest {

    @Test
    void storesAndExposesHandballPosition() {
        HandballAttributeProfile profile = new HandballAttributeProfile(74, 58, 79, 83, 41);
        HandballPlayer player = new HandballPlayer("hp-1", "Eren Kaya", HandballPosition.CENTER_BACK, profile);

        assertEquals(HandballPosition.CENTER_BACK, player.getPosition());
        assertFalse(player.getPosition().isGoalkeeper());
    }

    @Test
    void storesAttachedAttributeProfileReference() {
        HandballAttributeProfile profile = new HandballAttributeProfile(80, 64, 76, 73, 44);
        HandballPlayer player = new HandballPlayer("hp-2", "Mert Demir", HandballPosition.RIGHT_BACK, profile);

        assertSame(profile, player.getAttributeProfile());
        assertEquals(80, player.getAttributeProfile().getShooting());
        assertEquals(67, player.getAttributeProfile().getOverallRating());
    }

    @Test
    void updatesAvailabilityAndInjuryStateCorrectly() {
        HandballPlayer player = new HandballPlayer(
                "hp-3",
                "Can Arslan",
                HandballPosition.LEFT_WING,
                new HandballAttributeProfile(71, 57, 69, 88, 36)
        );

        assertTrue(player.isAvailable());
        assertEquals(0, player.getInjuryMatchesRemaining());

        player.setAvailable(false);
        assertFalse(player.isAvailable());

        player.setAvailable(true);
        player.injureForMatches(2);

        assertFalse(player.isAvailable());
        assertEquals(2, player.getInjuryMatchesRemaining());

        player.recoverOneMatch();
        assertFalse(player.isAvailable());
        assertEquals(1, player.getInjuryMatchesRemaining());

        player.recoverOneMatch();
        assertTrue(player.isAvailable());
        assertEquals(0, player.getInjuryMatchesRemaining());
    }
}
