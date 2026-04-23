package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Team;

import java.util.List;
import java.util.Objects;

public final class WeekProgressionResult {
    private final String sportId;
    private final int playedWeek;
    private final int currentWeekAfterProgression;
    private final boolean seasonCompleted;
    private final ProgressionState progressionState;
    private final List<Fixture> playedFixtures;
    private final List<Team> rankedTeams;
    private final List<TeamAvailabilityChange> availabilityChanges;

    public WeekProgressionResult(
            String sportId,
            int playedWeek,
            int currentWeekAfterProgression,
            boolean seasonCompleted,
            ProgressionState progressionState,
            List<Fixture> playedFixtures,
            List<Team> rankedTeams,
            List<TeamAvailabilityChange> availabilityChanges
    ) {
        if (playedWeek < 1 || currentWeekAfterProgression < 1) {
            throw new IllegalArgumentException("Week numbers must be at least 1.");
        }
        this.sportId = validateSportId(sportId);
        this.playedWeek = playedWeek;
        this.currentWeekAfterProgression = currentWeekAfterProgression;
        this.seasonCompleted = seasonCompleted;
        this.progressionState = Objects.requireNonNull(progressionState, "Progression state cannot be null.");
        this.playedFixtures = List.copyOf(Objects.requireNonNull(playedFixtures, "Played fixtures cannot be null."));
        this.rankedTeams = List.copyOf(Objects.requireNonNull(rankedTeams, "Ranked teams cannot be null."));
        this.availabilityChanges = List.copyOf(
                Objects.requireNonNull(availabilityChanges, "Availability changes cannot be null.")
        );
    }

    public String getSportId() {
        return sportId;
    }

    public int getPlayedWeek() {
        return playedWeek;
    }

    public int getCurrentWeekAfterProgression() {
        return currentWeekAfterProgression;
    }

    public boolean isSeasonCompleted() {
        return seasonCompleted;
    }

    public ProgressionState getProgressionState() {
        return progressionState;
    }

    public List<Fixture> getPlayedFixtures() {
        return playedFixtures;
    }

    public List<Team> getRankedTeams() {
        return rankedTeams;
    }

    public List<TeamAvailabilityChange> getAvailabilityChanges() {
        return availabilityChanges;
    }

    private String validateSportId(String sportId) {
        String cleaned = Objects.requireNonNull(sportId, "Sport id cannot be null.").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Sport id cannot be blank.");
        }
        return cleaned;
    }
}
