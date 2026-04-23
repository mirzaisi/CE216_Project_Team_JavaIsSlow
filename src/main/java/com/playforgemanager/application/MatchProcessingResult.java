package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Team;

import java.util.List;
import java.util.Objects;

public final class MatchProcessingResult {
    private final String sportId;
    private final int weekNumber;
    private final Fixture fixture;
    private final String controlledTeamName;
    private final String opponentTeamName;
    private final boolean controlledTeamHome;
    private final int controlledTeamScore;
    private final int opponentScore;
    private final List<Team> rankedTeamsAfterMatch;
    private final int controlledTeamRankAfterMatch;
    private final List<TeamAvailabilityChange> availabilityChanges;
    private final ProgressionState progressionState;

    public MatchProcessingResult(
            String sportId,
            int weekNumber,
            Fixture fixture,
            String controlledTeamName,
            String opponentTeamName,
            boolean controlledTeamHome,
            int controlledTeamScore,
            int opponentScore,
            List<Team> rankedTeamsAfterMatch,
            int controlledTeamRankAfterMatch,
            List<TeamAvailabilityChange> availabilityChanges,
            ProgressionState progressionState
    ) {
        if (weekNumber < 1) {
            throw new IllegalArgumentException("Week number must be at least 1.");
        }
        if (controlledTeamScore < 0 || opponentScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative.");
        }
        this.sportId = validateText(sportId, "Sport id cannot be blank.");
        this.weekNumber = weekNumber;
        this.fixture = Objects.requireNonNull(fixture, "Fixture cannot be null.");
        this.controlledTeamName = validateText(controlledTeamName, "Controlled team name cannot be blank.");
        this.opponentTeamName = validateText(opponentTeamName, "Opponent team name cannot be blank.");
        this.controlledTeamHome = controlledTeamHome;
        this.controlledTeamScore = controlledTeamScore;
        this.opponentScore = opponentScore;
        this.rankedTeamsAfterMatch = List.copyOf(Objects.requireNonNull(
                rankedTeamsAfterMatch,
                "Ranked teams cannot be null."
        ));
        if (controlledTeamRankAfterMatch < 1) {
            throw new IllegalArgumentException("Controlled team rank must be at least 1.");
        }
        this.controlledTeamRankAfterMatch = controlledTeamRankAfterMatch;
        this.availabilityChanges = List.copyOf(Objects.requireNonNull(
                availabilityChanges,
                "Availability changes cannot be null."
        ));
        this.progressionState = Objects.requireNonNull(progressionState, "Progression state cannot be null.");
    }

    public String getSportId() {
        return sportId;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public String getControlledTeamName() {
        return controlledTeamName;
    }

    public String getOpponentTeamName() {
        return opponentTeamName;
    }

    public boolean isControlledTeamHome() {
        return controlledTeamHome;
    }

    public int getControlledTeamScore() {
        return controlledTeamScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public List<Team> getRankedTeamsAfterMatch() {
        return rankedTeamsAfterMatch;
    }

    public int getControlledTeamRankAfterMatch() {
        return controlledTeamRankAfterMatch;
    }

    public List<TeamAvailabilityChange> getAvailabilityChanges() {
        return availabilityChanges;
    }

    public ProgressionState getProgressionState() {
        return progressionState;
    }

    private String validateText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
