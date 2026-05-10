package com.playforgemanager.application.save;

import java.util.List;
import java.util.Objects;

public record SaveLineupData(
        List<String> selectedPlayerIds,
        List<String> reservePlayerIds
) {

    public SaveLineupData {
        // Stores selected player ids as a safe unmodifiable list.
        selectedPlayerIds = List.copyOf(Objects.requireNonNull(
                selectedPlayerIds,
                "Selected player ids cannot be null."
        ));

        // Stores reserve player ids as a safe unmodifiable list.
        reservePlayerIds = List.copyOf(Objects.requireNonNull(
                reservePlayerIds,
                "Reserve player ids cannot be null."
        ));
    }
}
