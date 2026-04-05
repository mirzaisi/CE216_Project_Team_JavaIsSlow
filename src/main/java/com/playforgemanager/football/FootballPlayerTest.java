package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FootballPlayerTest {

    @Test
    void storesAndExposesFootballPosition() {
        FootballAttributeProfile profile = new FootballAttributeProfile(82, 48, 77, 81, 79);
        FootballPlayer player = new FootballPlayer("p-1", "Arda Yilmaz", FootballPosition.MIDFIELDER, profile);

        assertEquals(FootballPosition.MIDFIELDER, player.getPosition());
        assertFalse(player.getPosition().isGoalkeeper());
    }

    @Test
    void storesAttachedAttributeProfileReference() {
        FootballAttributeProfile profile = new FootballAttributeProfile(90, 35, 84, 88, 91);
        FootballPlayer player = new FootballPlayer("p-2", "Kerem Demir", FootballPosition.FORWARD, profile);

        assertSame(profile, player.getAttributeProfile());
        assertEquals(90, player.getAttributeProfile().getAttack());
        assertEquals(88, player.getAttributeProfile().getPassing());
        assertEquals(78, player.getAttributeProfile().getOverallRating());
    }

    @Test
    void updatesAvailabilityAndInjuryStateCorrectly() {
        FootballPlayer player = new FootballPlayer(
                "p-3",
                "Mert Kaya",
                FootballPosition.DEFENDER,
                new FootballAttributeProfile(55, 83, 80, 60, 66)
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
