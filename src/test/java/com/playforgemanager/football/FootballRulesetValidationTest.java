package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballRulesetValidationTest {
    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void validLineup_isAccepted() {
        FootballLineup lineup = createStandardValidLineup();

        assertTrue(ruleset.isValidLineup(lineup));
        assertDoesNotThrow(() -> ruleset.validateLineupOrThrow(lineup));
    }

    @Test
    void invalidLineupSize_isRejected() {
        FootballLineup lineup = new FootballLineup(createPlayers("DEF", 10, FootballPosition.DEFENDER));

        assertFalse(ruleset.isValidLineup(lineup));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ruleset.validateLineupOrThrow(lineup)
        );
        assertTrue(exception.getMessage().contains("exactly " + ruleset.getStartingLineupSize()));
    }

    @Test
    void unavailablePlayer_isRejected() {
        List<FootballPlayer> starters = new ArrayList<>(createStandardStarters());
        starters.get(4).injureForMatches(2);
        FootballLineup lineup = new FootballLineup(starters);

        assertFalse(ruleset.isValidLineup(lineup));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ruleset.validateLineupOrThrow(lineup)
        );
        assertTrue(exception.getMessage().contains("Unavailable football player"));
    }

    @Test
    void duplicatePlayerEntries_areRejected() {
        FootballPlayer goalkeeper = createPlayer("GK-1", "Goalkeeper One", FootballPosition.GOALKEEPER);
        FootballPlayer defenderOne = createPlayer("DF-1", "Defender One", FootballPosition.DEFENDER);
        FootballPlayer defenderTwo = createPlayer("DF-2", "Defender Two", FootballPosition.DEFENDER);
        FootballPlayer midfielderOne = createPlayer("MF-1", "Midfielder One", FootballPosition.MIDFIELDER);
        FootballPlayer midfielderTwo = createPlayer("MF-2", "Midfielder Two", FootballPosition.MIDFIELDER);
        FootballPlayer midfielderThree = createPlayer("MF-3", "Midfielder Three", FootballPosition.MIDFIELDER);
        FootballPlayer forwardOne = createPlayer("FW-1", "Forward One", FootballPosition.FORWARD);
        FootballPlayer forwardTwo = createPlayer("FW-2", "Forward Two", FootballPosition.FORWARD);
        FootballPlayer forwardThree = createPlayer("FW-3", "Forward Three", FootballPosition.FORWARD);
        FootballPlayer benchPlayer = createPlayer("BN-1", "Bench Player", FootballPosition.DEFENDER);

        assertThrows(
                IllegalArgumentException.class,
                () -> new FootballLineup(
                        List.of(
                                goalkeeper,
                                defenderOne,
                                defenderTwo,
                                midfielderOne,
                                midfielderTwo,
                                midfielderThree,
                                forwardOne,
                                forwardTwo,
                                forwardThree,
                                benchPlayer,
                                benchPlayer
                        )
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new FootballLineup(
                        List.of(
                                goalkeeper,
                                defenderOne,
                                defenderTwo,
                                midfielderOne,
                                midfielderTwo,
                                midfielderThree,
                                forwardOne,
                                forwardTwo,
                                forwardThree,
                                createPlayer("EX-1", "Extra One", FootballPosition.DEFENDER),
                                createPlayer("EX-2", "Extra Two", FootballPosition.DEFENDER)
                        ),
                        List.of(benchPlayer, benchPlayer)
                )
        );
    }

    @Test
    void lineupWithoutGoalkeeper_isRejected() {
        List<FootballPlayer> starters = new ArrayList<>();
        starters.addAll(createPlayers("DF", 5, FootballPosition.DEFENDER));
        starters.addAll(createPlayers("MF", 4, FootballPosition.MIDFIELDER));
        starters.addAll(createPlayers("FW", 2, FootballPosition.FORWARD));
        FootballLineup noGoalkeeperLineup = new FootballLineup(starters);

        assertFalse(ruleset.isValidLineup(noGoalkeeperLineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(noGoalkeeperLineup));
    }

    @Test
    void lineupWithTwoGoalkeepers_isRejected() {
        List<FootballPlayer> starters = new ArrayList<>();
        starters.addAll(createPlayers("GK", 2, FootballPosition.GOALKEEPER));
        starters.addAll(createPlayers("DF", 4, FootballPosition.DEFENDER));
        starters.addAll(createPlayers("MF", 4, FootballPosition.MIDFIELDER));
        starters.add(createPlayer("FW-1", "Forward One", FootballPosition.FORWARD));
        FootballLineup twoGoalkeeperLineup = new FootballLineup(starters);

        assertFalse(ruleset.isValidLineup(twoGoalkeeperLineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(twoGoalkeeperLineup));
    }

    @Test
    void lineupWithoutForwards_isRejected() {
        List<FootballPlayer> starters = new ArrayList<>();
        starters.add(createPlayer("GK-1", "Goalkeeper One", FootballPosition.GOALKEEPER));
        starters.addAll(createPlayers("DF", 4, FootballPosition.DEFENDER));
        starters.addAll(createPlayers("MF", 6, FootballPosition.MIDFIELDER));
        FootballLineup noForwardLineup = new FootballLineup(starters);

        assertFalse(ruleset.isValidLineup(noForwardLineup));
        assertThrows(IllegalArgumentException.class, () -> ruleset.validateLineupOrThrow(noForwardLineup));
    }

    private FootballLineup createStandardValidLineup() {
        return new FootballLineup(createStandardStarters(), createStandardBench());
    }

    private List<FootballPlayer> createStandardStarters() {
        List<FootballPlayer> starters = new ArrayList<>();
        starters.add(createPlayer("GK-1", "Goalkeeper One", FootballPosition.GOALKEEPER));
        starters.addAll(createPlayers("DF", 4, FootballPosition.DEFENDER));
        starters.addAll(createPlayers("MF", 4, FootballPosition.MIDFIELDER));
        starters.addAll(createPlayers("FW", 2, FootballPosition.FORWARD));
        return starters;
    }

    private List<FootballPlayer> createStandardBench() {
        List<FootballPlayer> bench = new ArrayList<>();
        bench.addAll(createPlayers("BDF", 2, FootballPosition.DEFENDER));
        bench.addAll(createPlayers("BMF", 2, FootballPosition.MIDFIELDER));
        bench.addAll(createPlayers("BFW", 2, FootballPosition.FORWARD));
        bench.add(createPlayer("BGK-1", "Bench Goalkeeper", FootballPosition.GOALKEEPER));
        return bench;
    }

    private List<FootballPlayer> createPlayers(String prefix, int count, FootballPosition position) {
        List<FootballPlayer> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(createPlayer(prefix + "-" + i, position.name() + " " + i, position));
        }
        return players;
    }

    private FootballPlayer createPlayer(String id, String name, FootballPosition position) {
        return new FootballPlayer(id, name, position, new FootballAttributeProfile(70, 70, 70, 70, 70));
    }
}
