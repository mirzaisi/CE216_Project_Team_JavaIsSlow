package com.playforgemanager.football;

import com.playforgemanager.core.Coach;

import java.util.Objects;

public class FootballCoach extends Coach {
    private static final int MIN_RATING = 0;
    private static final int MAX_RATING = 100;

    private final String specialization;
    private final int coachingRating;

    public FootballCoach(String id, String name, String role, String specialization, int coachingRating) {
        super(id, name, role);

        // Stores football-specific coach details after validation.
        this.specialization = validateSpecialization(specialization);
        this.coachingRating = validateRating(coachingRating);
    }

    public String getSpecialization() {
        return specialization;
    }

    public int getCoachingRating() {
        return coachingRating;
    }

    private String validateSpecialization(String specialization) {
        String value = Objects.requireNonNull(specialization, "Coach specialization cannot be null.").trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Coach specialization cannot be blank.");
        }

        return value;
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
