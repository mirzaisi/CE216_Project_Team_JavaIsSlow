package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballTacticAwareMatchEngineTest {
    private final FootballRuleset ruleset = new FootballRuleset();

    @Test
    void attackingTacticIncreasesHomeScoringTendencyWithSamePlayers() {
        FootballTeam attackingHome = team("home", "Home FC", 70, 70, 70, 70, 70);
        FootballTeam defensiveHome = team("home", "Home FC", 70, 70, 70, 70, 70);
        FootballTeam awayAgainstAttack = team("away", "Away FC", 70, 70, 70, 70, 70);
        FootballTeam awayAgainstDefense = team("away", "Away FC", 70, 70, 70, 70, 70);

        FootballMatch attackingMatch = readyMatch(
                attackingHome,
                awayAgainstAttack,
                new FootballTactic("Aggressive Width", "4-3-3", FootballTactic.Mentality.ATTACKING, 72, 82),
                balancedTactic()
        );
        FootballMatch defensiveMatch = readyMatch(
                defensiveHome,
                awayAgainstDefense,
                new FootballTactic("Deep Block", "5-4-1", FootballTactic.Mentality.DEFENSIVE, 35, 30),
                balancedTactic()
        );

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(attackingMatch, ruleset);
        engine.simulate(defensiveMatch, ruleset);

        assertTrue(attackingMatch.getHomeScore() > defensiveMatch.getHomeScore());
    }

    @Test
    void defensiveTacticReducesOpponentScoringTendencyWithSamePlayers() {
        FootballTeam homeAgainstAttackingAway = team("home", "Home FC", 70, 70, 70, 70, 70);
        FootballTeam homeAgainstDefensiveAway = team("home", "Home FC", 70, 70, 70, 70, 70);
        FootballTeam attackingAway = team("away", "Away FC", 70, 70, 70, 70, 70);
        FootballTeam defensiveAway = team("away", "Away FC", 70, 70, 70, 70, 70);

        FootballMatch againstAttacking = readyMatch(
                homeAgainstAttackingAway,
                attackingAway,
                balancedTactic(),
                new FootballTactic("Wide Attack", "4-3-3", FootballTactic.Mentality.ATTACKING, 75, 85)
        );
        FootballMatch againstDefensive = readyMatch(
                homeAgainstDefensiveAway,
                defensiveAway,
                balancedTactic(),
                new FootballTactic("Compact Defense", "5-4-1", FootballTactic.Mentality.DEFENSIVE, 38, 28)
        );

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(againstAttacking, ruleset);
        engine.simulate(againstDefensive, ruleset);

        assertTrue(againstAttacking.getHomeScore() > againstDefensive.getHomeScore());
    }

    @Test
    void playerAttributesStillMatterWhenTacticsAreDifferent() {
        FootballTeam strongDefensiveHome = team("home", "Home FC", 90, 90, 90, 90, 90);
        FootballTeam weakAttackingHome = team("home", "Home FC", 42, 42, 42, 42, 42);
        FootballTeam sameAwayForStrong = team("away", "Away FC", 66, 66, 66, 66, 66);
        FootballTeam sameAwayForWeak = team("away", "Away FC", 66, 66, 66, 66, 66);

        FootballMatch strongButDefensive = readyMatch(
                strongDefensiveHome,
                sameAwayForStrong,
                new FootballTactic("Controlled Defense", "5-4-1", FootballTactic.Mentality.DEFENSIVE, 40, 35),
                balancedTactic()
        );
        FootballMatch weakButAttacking = readyMatch(
                weakAttackingHome,
                sameAwayForWeak,
                new FootballTactic("Risky Attack", "4-3-3", FootballTactic.Mentality.ATTACKING, 80, 90),
                balancedTactic()
        );

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(strongButDefensive, ruleset);
        engine.simulate(weakButAttacking, ruleset);

        assertTrue(strongButDefensive.getHomeScore() > weakButAttacking.getHomeScore());
    }

    @Test
    void sameTeamsLineupsTacticsAndSeedProduceStableOutput() {
        FootballMatch first = readyMatch(
                team("home", "Home FC", 72, 70, 71, 73, 69),
                team("away", "Away FC", 70, 72, 69, 68, 71),
                new FootballTactic("High Press", "4-2-3-1", FootballTactic.Mentality.ATTACKING, 78, 70),
                new FootballTactic("Counter Block", "4-4-2", FootballTactic.Mentality.DEFENSIVE, 42, 35)
        );
        FootballMatch second = readyMatch(
                team("home", "Home FC", 72, 70, 71, 73, 69),
                team("away", "Away FC", 70, 72, 69, 68, 71),
                new FootballTactic("High Press", "4-2-3-1", FootballTactic.Mentality.ATTACKING, 78, 70),
                new FootballTactic("Counter Block", "4-4-2", FootballTactic.Mentality.DEFENSIVE, 42, 35)
        );

        FootballMatchEngine engine = new FootballMatchEngine(123L);
        engine.simulate(first, ruleset);
        engine.simulate(second, ruleset);

        assertEquals(first.getHomeScore(), second.getHomeScore());
        assertEquals(first.getAwayScore(), second.getAwayScore());
    }

    private FootballMatch readyMatch(
            FootballTeam home,
            FootballTeam away,
            FootballTactic homeTactic,
            FootballTactic awayTactic
    ) {
        FootballMatch match = new FootballMatch(home, away);
        match.setHomeSetup(ruleset.buildLineup(home.getAvailablePlayers()), homeTactic);
        match.setAwaySetup(ruleset.buildLineup(away.getAvailablePlayers()), awayTactic);
        return match;
    }

    private FootballTactic balancedTactic() {
        return new FootballTactic("Balanced", "4-4-2", FootballTactic.Mentality.BALANCED, 55, 55);
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
