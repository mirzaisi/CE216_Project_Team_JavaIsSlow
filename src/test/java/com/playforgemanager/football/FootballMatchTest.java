package com.playforgemanager.football;

import com.playforgemanager.core.Match;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballMatchTest {

    @Test
    void freshMatchIsNotPlayedAndHasZeroScores() {
        FootballTeam home = teamWithPlayers("home", "Home FC", 11);
        FootballTeam away = teamWithPlayers("away", "Away FC", 11);

        FootballMatch match = new FootballMatch(home, away);

        assertFalse(match.isPlayed());
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
        assertSame(home, match.getHomeTeam());
        assertSame(away, match.getAwayTeam());
    }

    @Test
    void setResultMarksMatchPlayedAndStoresScores() {
        FootballMatch match = new FootballMatch(
                teamWithPlayers("home", "Home FC", 11),
                teamWithPlayers("away", "Away FC", 11)
        );

        match.setResult(3, 1);

        assertTrue(match.isPlayed());
        assertEquals(3, match.getHomeScore());
        assertEquals(1, match.getAwayScore());
    }

    @Test
    void completedMatchRejectsSecondResultWrite() {
        FootballMatch match = new FootballMatch(
                teamWithPlayers("home", "Home FC", 11),
                teamWithPlayers("away", "Away FC", 11)
        );
        match.setResult(2, 2);

        assertThrows(IllegalStateException.class, () -> match.setResult(4, 0));

        assertEquals(2, match.getHomeScore());
        assertEquals(2, match.getAwayScore());
    }

    @Test
    void constructorRejectsSameTeamForHomeAndAway() {
        FootballTeam team = teamWithPlayers("t1", "Mirror FC", 11);

        assertThrows(IllegalArgumentException.class, () -> new FootballMatch(team, team));
    }

    @Test
    void setHomeAndAwaySetupStoreLineupAndTactic() {
        FootballTeam home = teamWithPlayers("home", "Home FC", 11);
        FootballTeam away = teamWithPlayers("away", "Away FC", 11);
        FootballMatch match = new FootballMatch(home, away);

        FootballLineup homeLineup = new FootballLineup(home.getFootballPlayers());
        FootballLineup awayLineup = new FootballLineup(away.getFootballPlayers());
        FootballTactic tactic = new FootballTactic(
                "Direct", "4-4-2", FootballTactic.Mentality.ATTACKING, 60, 55
        );

        match.setHomeSetup(homeLineup, tactic);
        match.setAwaySetup(awayLineup, tactic);

        assertSame(homeLineup, match.getHomeLineup());
        assertSame(awayLineup, match.getAwayLineup());
        assertSame(tactic, match.getHomeTactic());
        assertSame(tactic, match.getAwayTactic());
    }

    @Test
    void footballMatchIsUsableAsGenericCoreMatch() {
        Match match = new FootballMatch(
                teamWithPlayers("home", "Home FC", 11),
                teamWithPlayers("away", "Away FC", 11)
        );
        match.setResult(1, 0);

        assertNotNull(match);
        assertTrue(match.isPlayed());
        assertEquals(1, match.getHomeScore());
    }

    private FootballTeam teamWithPlayers(String id, String name, int count) {
        FootballTeam team = new FootballTeam(id, name);
        for (int i = 0; i < count; i++) {
            team.addPlayer(new FootballPlayer(
                    id + "-player-" + i,
                    name + " Player " + i,
                    positionForIndex(i),
                    new FootballAttributeProfile(70, 70, 70, 70, 70)
            ));
        }
        return team;
    }

    private FootballPosition positionForIndex(int index) {
        if (index == 0) return FootballPosition.GOALKEEPER;
        if (index < 5) return FootballPosition.DEFENDER;
        if (index < 9) return FootballPosition.MIDFIELDER;
        return FootballPosition.FORWARD;
    }
}
