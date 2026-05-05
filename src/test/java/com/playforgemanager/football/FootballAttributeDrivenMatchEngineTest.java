package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballAttributeDrivenMatchEngineTest {
    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void strongerLineupProducesBetterDeterministicHomeOutputAgainstSameOpponent() {
        FootballTeam strongHome = team("home", "Home FC", 92, 88, 90, 91, 89);
        FootballTeam weakHome = team("home", "Home FC", 38, 36, 41, 39, 37);
        FootballTeam sameAwayForStrong = team("away", "Away FC", 60, 60, 60, 60, 60);
        FootballTeam sameAwayForWeak = team("away", "Away FC", 60, 60, 60, 60, 60);

        FootballMatch strongMatch = readyMatch(strongHome, sameAwayForStrong);
        FootballMatch weakMatch = readyMatch(weakHome, sameAwayForWeak);

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(strongMatch, ruleset);
        engine.simulate(weakMatch, ruleset);

        assertTrue(strongMatch.getHomeScore() > weakMatch.getHomeScore());
    }

    @Test
    void playerTrainingEffectIsIncludedInMatchStrength() {
        FootballTeam trainedHome = team("home", "Home FC", 64, 60, 62, 63, 61);
        FootballTeam plainHome = team("home", "Home FC", 64, 60, 62, 63, 61);
        FootballTeam awayForTrained = team("away", "Away FC", 64, 60, 62, 63, 61);
        FootballTeam awayForPlain = team("away", "Away FC", 64, 60, 62, 63, 61);

        trainedHome.getFootballPlayers().forEach(player ->
                player.applyWeeklyTrainingEffect(new FootballTrainingEffect(10, 0, 0, 4, 4, false))
        );

        FootballMatch trainedMatch = readyMatch(trainedHome, awayForTrained);
        FootballMatch plainMatch = readyMatch(plainHome, awayForPlain);

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(trainedMatch, ruleset);
        engine.simulate(plainMatch, ruleset);

        assertTrue(trainedMatch.getHomeScore() >= plainMatch.getHomeScore());
    }

    @Test
    void sameTeamsLineupsAndSeedProduceStableOutput() {
        FootballMatch first = readyMatch(
                team("home", "Home FC", 70, 70, 70, 70, 70),
                team("away", "Away FC", 68, 68, 68, 68, 68)
        );
        FootballMatch second = readyMatch(
                team("home", "Home FC", 70, 70, 70, 70, 70),
                team("away", "Away FC", 68, 68, 68, 68, 68)
        );

        FootballMatchEngine engine = new FootballMatchEngine(99L);
        engine.simulate(first, ruleset);
        engine.simulate(second, ruleset);

        assertEquals(first.getHomeScore(), second.getHomeScore());
        assertEquals(first.getAwayScore(), second.getAwayScore());
    }

    private FootballMatch readyMatch(FootballTeam home, FootballTeam away) {
        FootballMatch match = new FootballMatch(home, away);
        FootballTactic tactic = new FootballTactic("Balanced", "4-3-3", FootballTactic.Mentality.BALANCED, 55, 55);
        match.setHomeSetup(ruleset.buildLineup(home.getAvailablePlayers()), tactic);
        match.setAwaySetup(ruleset.buildLineup(away.getAvailablePlayers()), tactic);
        return match;
    }

    private FootballTeam team(
            String idPrefix,
            String name,
            int attack,
            int defense,
            int stamina,
            int passing,
            int speed
    ) {
        FootballTeam team = new FootballTeam(idPrefix, name);
        List<FootballPosition> positions = List.of(
                FootballPosition.GOALKEEPER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.DEFENDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.FORWARD,
                FootballPosition.FORWARD
        );

        for (int i = 0; i < positions.size(); i++) {
            team.addPlayer(new FootballPlayer(
                    idPrefix + "-player-" + i,
                    name + " Player " + i,
                    positions.get(i),
                    new FootballAttributeProfile(attack, defense, stamina, passing, speed)
            ));
        }
        return team;
    }
}
