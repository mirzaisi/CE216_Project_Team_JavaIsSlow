package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HandballMatchTest {

    @Test
    void matchStartsUnplayedWithZeroScore() {
        HandballMatch match = new HandballMatch(
                new HandballTeam("home", "Home HB"),
                new HandballTeam("away", "Away HB")
        );

        assertFalse(match.isPlayed());
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
    }

    @Test
    void matchRejectsSameTeamOnBothSides() {
        HandballTeam team = new HandballTeam("same", "Same Team");

        assertThrows(IllegalArgumentException.class, () -> new HandballMatch(team, team));
    }
}
