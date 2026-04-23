package com.playforgemanager.application;

import java.util.Objects;

public class PostMatchSummaryQueryService {

    public PostMatchSummaryView build(MatchProcessingResult result) {
        MatchProcessingResult validatedResult = Objects.requireNonNull(result, "Match processing result cannot be null.");

        String outcomeLabel;
        if (validatedResult.getControlledTeamScore() > validatedResult.getOpponentScore()) {
            outcomeLabel = "WIN";
        } else if (validatedResult.getControlledTeamScore() < validatedResult.getOpponentScore()) {
            outcomeLabel = "LOSS";
        } else {
            outcomeLabel = "DRAW";
        }

        return new PostMatchSummaryView(
                validatedResult.getSportId(),
                validatedResult.getWeekNumber(),
                validatedResult.getControlledTeamName(),
                validatedResult.getOpponentTeamName(),
                validatedResult.isControlledTeamHome(),
                validatedResult.getControlledTeamScore(),
                validatedResult.getOpponentScore(),
                outcomeLabel,
                validatedResult.getControlledTeamRankAfterMatch(),
                validatedResult.getProgressionState(),
                validatedResult.getAvailabilityChanges(),
                QueryViewSupport.toFixtureSummary(validatedResult.getFixture(), null)
        );
    }
}
