package com.playforgemanager.application.save;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveGameFormatTest {

    @Test
    void formatDefinesStableVersionedRootMetadata() {
        SaveGameDocument document = SaveGameFormat.emptyDocumentTemplate();

        assertEquals("playforge-save", document.formatId());
        assertEquals(1, document.formatVersion());
        assertEquals(".pfm-save.json", SaveGameFormat.FILE_EXTENSION);
        assertNotNull(document.session());
    }

    @Test
    void fieldPoliciesMarkStoredAndRecomputedSessionAreas() {
        List<SaveFieldPolicy> policies = SaveGameFormat.fieldPolicies();

        assertFalse(policies.isEmpty());
        assertTrue(policies.stream().anyMatch(policy ->
                policy.fieldPath().equals("session.sportId") && policy.mode() == PersistenceMode.STORED
        ));
        assertTrue(policies.stream().anyMatch(policy ->
                policy.fieldPath().equals("session.season.fixtures[].playedMatch")
                        && policy.mode() == PersistenceMode.STORED
        ));
        assertTrue(policies.stream().anyMatch(policy ->
                policy.fieldPath().equals("session.season.standings")
                        && policy.mode() == PersistenceMode.RECOMPUTED
        ));
        assertTrue(policies.stream().anyMatch(policy ->
                policy.fieldPath().equals("application.queryViews")
                        && policy.mode() == PersistenceMode.RECOMPUTED
        ));
    }

    @Test
    void saveModelCapturesRequiredRunningSessionPieces() {
        SaveGameDocument document = new SaveGameDocument(
                SaveGameFormat.FORMAT_ID,
                SaveGameFormat.CURRENT_VERSION,
                new SaveSessionData(
                        "football",
                        "IN_PROGRESS",
                        "football-team-1",
                        new SaveSeasonData(
                                "Integration League",
                                3,
                                false,
                                List.of(new SaveTeamData(
                                        "football-team-1",
                                        "Red Hawks",
                                        List.of(new SaveCoachData(
                                                "coach-1",
                                                "Ali Coach",
                                                "Head Coach",
                                                List.of(new SavePropertyValue("specialization", "string", "Attacking Play"))
                                        )),
                                        List.of(new SavePlayerData(
                                                "player-1",
                                                "Ali 1",
                                                false,
                                                2,
                                                List.of(
                                                        new SavePropertyValue("position", "string", "GOALKEEPER"),
                                                        new SavePropertyValue("attack", "integer", "40")
                                                )
                                        )),
                                        new SaveLineupData(List.of("player-1"), List.of()),
                                        new SaveTacticData(
                                                "Balanced Control",
                                                List.of(new SavePropertyValue("formation", "string", "4-2-3-1"))
                                        ),
                                        new SaveTrainingPlanData(
                                                "Balanced Development",
                                                60,
                                                List.of(new SavePropertyValue("recoveryIncluded", "boolean", "true"))
                                        ),
                                        List.of(new SavePropertyValue("sportModule", "string", "football"))
                                )),
                                List.of(new SaveFixtureData(
                                        3,
                                        "football-team-1",
                                        "football-team-2",
                                        new SavePlayedMatchData(
                                                2,
                                                1,
                                                new SaveLineupData(List.of("player-1"), List.of()),
                                                new SaveLineupData(List.of("player-2"), List.of()),
                                                new SaveTacticData("Control", List.of()),
                                                new SaveTacticData("Counter", List.of())
                                        )
                                ))
                        ),
                        SaveGameFormat.fieldPolicies()
                )
        );

        assertEquals("football", document.session().sportId());
        assertEquals("football-team-1", document.session().controlledTeamId());
        assertEquals(3, document.session().season().currentWeek());
        assertEquals(1, document.session().season().teams().size());
        assertEquals(1, document.session().season().fixtures().size());
        assertEquals(2, document.session().season().teams().get(0).players().get(0).injuryMatchesRemaining());
        assertEquals("Balanced Control", document.session().season().teams().get(0).selectedTactic().name());
    }
}
