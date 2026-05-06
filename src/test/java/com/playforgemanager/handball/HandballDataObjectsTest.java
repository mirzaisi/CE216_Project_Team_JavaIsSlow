package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandballDataObjectsTest {

    @Test
    void handballCoachStoresItsCoachingData() {
        HandballCoach coach = new HandballCoach(
                "hc-1",
                "Deniz Yildiz",
                "Head Coach",
                HandballCoachSpecialization.DEFENSE,
                82
        );

        assertEquals("Deniz Yildiz", coach.getName());
        assertEquals("Head Coach", coach.getRole());
        assertEquals(HandballCoachSpecialization.DEFENSE, coach.getSpecialization());
        assertEquals(82, coach.getCoachingRating());
        assertEquals(82, coach.getRating());
    }

    @Test
    void handballAttributeProfileCalculatesOverallRating() {
        HandballAttributeProfile profile = new HandballAttributeProfile(78, 66, 70, 74, 52);

        assertEquals(78, profile.getShooting());
        assertEquals(66, profile.getDefense());
        assertEquals(68, profile.getOverallRating());
    }
}
