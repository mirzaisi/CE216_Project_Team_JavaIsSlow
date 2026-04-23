package com.playforgemanager.application.save;

import java.util.List;

public final class SaveGameFormat {
    public static final String FORMAT_ID = "playforge-save";
    public static final int CURRENT_VERSION = 1;
    public static final String FILE_EXTENSION = ".pfm-save.json";

    private SaveGameFormat() {
    }

    public static List<SaveFieldPolicy> fieldPolicies() {
        return List.of(
                new SaveFieldPolicy(
                        "session.sportId",
                        PersistenceMode.STORED,
                        "The selected sport determines which sport module is rebuilt on load."
                ),
                new SaveFieldPolicy(
                        "session.progressionState",
                        PersistenceMode.STORED,
                        "The session progression state may differ from the season completion flag."
                ),
                new SaveFieldPolicy(
                        "session.controlledTeamId",
                        PersistenceMode.STORED,
                        "The user-controlled team must survive across saves."
                ),
                new SaveFieldPolicy(
                        "session.season.leagueName",
                        PersistenceMode.STORED,
                        "League naming is user-facing session data."
                ),
                new SaveFieldPolicy(
                        "session.season.currentWeek",
                        PersistenceMode.STORED,
                        "The load point must resume from the same week."
                ),
                new SaveFieldPolicy(
                        "session.season.completed",
                        PersistenceMode.STORED,
                        "Completion status is part of the running session state."
                ),
                new SaveFieldPolicy(
                        "session.season.teams",
                        PersistenceMode.STORED,
                        "Team, roster, staff, lineup, tactic, and training state are part of the live session."
                ),
                new SaveFieldPolicy(
                        "session.season.fixtures",
                        PersistenceMode.STORED,
                        "Fixture order and played-match history define the season timeline."
                ),
                new SaveFieldPolicy(
                        "session.season.fixtures[].playedMatch",
                        PersistenceMode.STORED,
                        "Played matches preserve results and the setups used when they happened."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].players[].available",
                        PersistenceMode.STORED,
                        "Availability can be manually or policy-driven and should not be guessed."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].players[].injuryMatchesRemaining",
                        PersistenceMode.STORED,
                        "Recovery countdown is needed to resume the same future availability state."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].selectedLineup",
                        PersistenceMode.STORED,
                        "Current team setup should remain intact when loading a running session."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].selectedTactic",
                        PersistenceMode.STORED,
                        "Current tactical choice is user-facing session state."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].selectedTrainingPlan",
                        PersistenceMode.STORED,
                        "Training choice is part of weekly planning and should survive a save."
                ),
                new SaveFieldPolicy(
                        "session.activeSport",
                        PersistenceMode.RECOMPUTED,
                        "The active sport object is rebuilt from the registered sport id."
                ),
                new SaveFieldPolicy(
                        "session.season.standings",
                        PersistenceMode.RECOMPUTED,
                        "Standings can be rebuilt from persisted fixtures and match results."
                ),
                new SaveFieldPolicy(
                        "session.season.currentWeekFixtures",
                        PersistenceMode.RECOMPUTED,
                        "Current-week fixture subsets are derived from week numbers."
                ),
                new SaveFieldPolicy(
                        "session.season.teams[].availablePlayers",
                        PersistenceMode.RECOMPUTED,
                        "Availability lists are derived from persisted roster state."
                ),
                new SaveFieldPolicy(
                        "session.season.fixtures[].played",
                        PersistenceMode.RECOMPUTED,
                        "Played state is implied by whether a played match exists."
                ),
                new SaveFieldPolicy(
                        "application.queryViews",
                        PersistenceMode.RECOMPUTED,
                        "UI-facing query models are rebuilt from the persisted session."
                )
        );
    }

    public static SaveGameDocument emptyDocumentTemplate() {
        return new SaveGameDocument(
                FORMAT_ID,
                CURRENT_VERSION,
                new SaveSessionData(
                        "sport-id",
                        "READY_TO_START",
                        "controlled-team-id",
                        new SaveSeasonData(
                                "League Name",
                                1,
                                false,
                                List.of(),
                                List.of()
                        ),
                        fieldPolicies()
                )
        );
    }
}
