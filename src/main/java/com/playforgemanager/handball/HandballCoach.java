package com.playforgemanager.handball;

import com.playforgemanager.core.Coach;

import java.util.Objects;

public class HandballCoach extends Coach {
    private static final int MIN_RATING = 0;
    private static final int MAX_RATING = 100;

    private final HandballCoachSpecialization specialization;
    private final int coachingRating;

    public HandballCoach(
            String id,
            String name,
            String role,
            HandballCoachSpecialization specialization,
            int coachingRating
    ) {
        super(id, name, role);

        // Stores handball-specific coach details after validation.
        this.specialization = Objects.requireNonNull(
                specialization,
                "Coach specialization cannot be null."
        );
        this.coachingRating = validateRating(coachingRating);
    }

    public HandballCoachSpecialization getSpecialization() {
        return specialization;
    }

    public int getCoachingRating() {
        return coachingRating;
    }

    public int getRating() {
        return getCoachingRating();
    }

    private int validateRating(int rating) {
        // Coaching rating must stay inside the shared 0-100 rating range.
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "Coaching rating must be between " + MIN_RATING + " and " + MAX_RATING + "."
            );
        }

        return rating;
    }
}
