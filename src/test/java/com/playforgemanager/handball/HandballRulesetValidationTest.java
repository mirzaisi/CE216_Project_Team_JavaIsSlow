package com.playforgemanager.handball;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballRulesetValidationTest {
    private final HandballRuleset ruleset = new HandballRuleset();

    @Test
    void validLineupIsAccepted() {
        HandballLineup lineup = createStandardValidLineup();

        assertTrue(ruleset.isValidLineup(lineup));
        assertDoesNotThrow(() -> ruleset.validateLineupOrThrow(lineup));
    }

    @Test
    void invalidLineupSizeIsRejected() {
        HandballLineup lineup = new HandballLineup(
                List.of(
                        createPlayer("gk-1", "Goalkeeper", HandballPosition.GOALKEEPER),
                        createPlayer("lw-1", "Left Wing", HandballPosition.LEFT_WING),
                        createPlayer("rw-1", "Right Wing", HandballPosition.RIGHT_WING)
                )
        );

        assertFalse(ruleset.isValidLineup(lineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(lineup));
    }

    @Test
    void unavailablePlayerIsRejected() {
        List<HandballPlayer> starters = new ArrayList<>(createStandardStarters());
        starters.get(2).injureForMatches(2);
        HandballLineup lineup = new HandballLineup(starters);

        assertFalse(ruleset.isValidLineup(lineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(lineup));
    }

    @Test
    void duplicatePlayerEntriesAreRejected() {
        HandballPlayer goalkeeper = createPlayer("gk-1", "Goalkeeper", HandballPosition.GOALKEEPER);
        HandballPlayer leftWing = createPlayer("lw-1", "Left Wing", HandballPosition.LEFT_WING);
        HandballPlayer rightWing = createPlayer("rw-1", "Right Wing", HandballPosition.RIGHT_WING);
        HandballPlayer leftBack = createPlayer("lb-1", "Left Back", HandballPosition.LEFT_BACK);
        HandballPlayer centerBack = createPlayer("cb-1", "Center Back", HandballPosition.CENTER_BACK);
        HandballPlayer rightBack = createPlayer("rb-1", "Right Back", HandballPosition.RIGHT_BACK);

        assertThrows(
                IllegalArgumentException.class,
                () -> new HandballLineup(
                        List.of(goalkeeper, leftWing, rightWing, leftBack, centerBack, rightBack, rightBack)
                )
        );
    }

    @Test
    void lineupWithoutPivotIsRejected() {
        List<HandballPlayer> starters = new ArrayList<>();
        starters.add(createPlayer("gk-1", "Goalkeeper", HandballPosition.GOALKEEPER));
        starters.add(createPlayer("lw-1", "Left Wing", HandballPosition.LEFT_WING));
        starters.add(createPlayer("rw-1", "Right Wing", HandballPosition.RIGHT_WING));
        starters.add(createPlayer("lb-1", "Left Back", HandballPosition.LEFT_BACK));
        starters.add(createPlayer("cb-1", "Center Back", HandballPosition.CENTER_BACK));
        starters.add(createPlayer("rb-1", "Right Back", HandballPosition.RIGHT_BACK));
        starters.add(createPlayer("rb-2", "Right Back Two", HandballPosition.RIGHT_BACK));

        HandballLineup lineup = new HandballLineup(starters);

        assertFalse(ruleset.isValidLineup(lineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(lineup));
    }

    @Test
    void buildLineupSelectsOnePlayerForEachRequiredPosition() {
        HandballLineup lineup = ruleset.buildLineup(createSquad());

        assertTrue(ruleset.isValidLineup(lineup));
        assertTrue(lineup.getBenchPlayers().size() <= ruleset.getBenchSize());
    }

    private HandballLineup createStandardValidLineup() {
        return new HandballLineup(createStandardStarters(), createBench());
    }

    private List<HandballPlayer> createStandardStarters() {
        return List.of(
                createPlayer("gk-1", "Goalkeeper", HandballPosition.GOALKEEPER),
                createPlayer("lw-1", "Left Wing", HandballPosition.LEFT_WING),
                createPlayer("rw-1", "Right Wing", HandballPosition.RIGHT_WING),
                createPlayer("lb-1", "Left Back", HandballPosition.LEFT_BACK),
                createPlayer("cb-1", "Center Back", HandballPosition.CENTER_BACK),
                createPlayer("rb-1", "Right Back", HandballPosition.RIGHT_BACK),
                createPlayer("pv-1", "Pivot", HandballPosition.PIVOT)
        );
    }

    private List<HandballPlayer> createBench() {
        return List.of(
                createPlayer("gk-2", "Bench Goalkeeper", HandballPosition.GOALKEEPER),
                createPlayer("lw-2", "Bench Left Wing", HandballPosition.LEFT_WING),
                createPlayer("rw-2", "Bench Right Wing", HandballPosition.RIGHT_WING)
        );
    }

    private List<HandballPlayer> createSquad() {
        List<HandballPlayer> squad = new ArrayList<>(createStandardStarters());
        squad.addAll(createBench());
        squad.add(createPlayer("lb-2", "Bench Left Back", HandballPosition.LEFT_BACK));
        squad.add(createPlayer("cb-2", "Bench Center Back", HandballPosition.CENTER_BACK));
        squad.add(createPlayer("rb-2", "Bench Right Back", HandballPosition.RIGHT_BACK));
        squad.add(createPlayer("pv-2", "Bench Pivot", HandballPosition.PIVOT));
        return squad;
    }

    private HandballPlayer createPlayer(String id, String name, HandballPosition position) {
        return new HandballPlayer(id, name, position, new HandballAttributeProfile(70, 70, 70, 70, 70));
    }
}
