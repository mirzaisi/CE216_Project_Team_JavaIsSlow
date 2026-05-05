package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Match;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FootballSeasonTrainingInjuryFlowTest {
    @Test
    void seasonRebuildsLineupAfterTrainingInjuryBeforeMatchSimulation() {
        FootballRuleset ruleset = new FootballRuleset();
        FootballSport sport = new FootballSport();
        FootballTeam home = createFullTeam("HOME", true);
        FootballTeam away = createFullTeam("AWAY", false);
        FootballPlayer vulnerableMidfielder = findPlayer(home, "HOME-M1");

        home.assignLineup(ruleset.buildLineup(home.getAvailablePlayers()), ruleset);
        away.assignLineup(ruleset.buildLineup(away.getAvailablePlayers()), ruleset);
        home.assignTrainingPlan(new FootballTrainingPlan("Fitness Overload", 90, 70, 40, false));
        away.assignTrainingPlan(new FootballTrainingPlan("Recovery", 30, 30, 30, true));

        FootballLeague league = new FootballLeague("Training Injury League");
        league.addTeam(home);
        league.addTeam(away);
        league.addFixture(new Fixture(1, home, away));

        FootballSeason season = new FootballSeason(league, new FootballTrainingEffectService());
        season.playCurrentWeek(sport, FootballMatch::new);

        Match playedMatch = league.getFixtures().get(0).getPlayedMatch();
        assertNotNull(playedMatch);

        FootballLineup rebuiltHomeLineup = (FootballLineup) playedMatch.getHomeLineup();
        assertFalse(rebuiltHomeLineup.containsPlayerId(vulnerableMidfielder.getId()));
        assertFalse(vulnerableMidfielder.isAvailable());
        assertEquals(2, vulnerableMidfielder.getInjuryMatchesRemaining());
    }

    private FootballTeam createFullTeam(String prefix, boolean lowFirstMidfielderStamina) {
        FootballTeam team = new FootballTeam(prefix + "-TEAM", prefix + " FC");
        team.addPlayer(player(prefix + "-GK1", FootballPosition.GOALKEEPER, 70));
        team.addPlayer(player(prefix + "-GK2", FootballPosition.GOALKEEPER, 70));

        for (int i = 1; i <= 5; i++) {
            team.addPlayer(player(prefix + "-D" + i, FootballPosition.DEFENDER, 70));
        }

        for (int i = 1; i <= 5; i++) {
            int stamina = lowFirstMidfielderStamina && i == 1 ? 5 : 70;
            team.addPlayer(player(prefix + "-M" + i, FootballPosition.MIDFIELDER, stamina));
        }

        for (int i = 1; i <= 3; i++) {
            team.addPlayer(player(prefix + "-F" + i, FootballPosition.FORWARD, 70));
        }

        return team;
    }

    private FootballPlayer player(String id, FootballPosition position, int stamina) {
        return new FootballPlayer(
                id,
                id,
                position,
                new FootballAttributeProfile(70, 70, stamina, 70, 70)
        );
    }

    private FootballPlayer findPlayer(FootballTeam team, String id) {
        return team.getFootballPlayers().stream()
                .filter(player -> player.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
