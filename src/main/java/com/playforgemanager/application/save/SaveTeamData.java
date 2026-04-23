package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveTeamData(
        String id,
        String name,
        List<SaveCoachData> coaches,
        List<SavePlayerData> players,
        SaveLineupData selectedLineup,
        SaveTacticData selectedTactic,
        SaveTrainingPlanData selectedTrainingPlan,
        List<SavePropertyValue> properties
) {
    public SaveTeamData {
        id = requireText(id, "Team id cannot be blank.");
        name = requireText(name, "Team name cannot be blank.");
        coaches = List.copyOf(Objects.requireNonNull(coaches, "Coaches cannot be null."));
        players = List.copyOf(Objects.requireNonNull(players, "Players cannot be null."));
        properties = List.copyOf(Objects.requireNonNull(properties, "Team properties cannot be null."));
    }

    private static String requireText(String value, String message) {
        String cleaned = Objects.requireNonNull(value, message).trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }
}
