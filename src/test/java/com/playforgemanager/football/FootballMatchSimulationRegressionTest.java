package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballMatchSimulationRegressionTest {
    @Test
    void strongerLineupCreatesBetterDeterministicMatchTendency() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballTeam strongHome = FootballRegressionTestSupport.team("strong-home", "Strong Home", 88);
        FootballTeam weakAway = FootballRegressionTestSupport.team("weak-away", "Weak Away", 38);
        FootballTeam weakHome = FootballRegressionTestSupport.team("weak-home", "Weak Home", 38);
        FootballTeam strongAway = FootballRegressionTestSupport.team("strong-away", "Strong Away", 88);

        FootballMatch strongHomeMatch = FootballRegressionTestSupport.preparedMatch(sport, strongHome, weakAway);
        FootballMatch weakHomeMatch = FootballRegressionTestSupport.preparedMatch(sport, weakHome, strongAway);

        sport.getMatchEngine().simulate(strongHomeMatch, sport.getRuleset());
        sport.getMatchEngine().simulate(weakHomeMatch, sport.getRuleset());

        assertTrue(
                FootballRegressionTestSupport.goalDifference(strongHomeMatch)
                        > FootballRegressionTestSupport.goalDifference(weakHomeMatch),
                "A much stronger lineup should produce a better deterministic match tendency than a weak lineup."
        );
    }

    @Test
    void tacticChangesOutcomeTendencyWithoutReplacingPlayerQuality() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballTeam attackingHome = FootballRegressionTestSupport.team(
                "home-attack",
                "Same Home",
                70,
                FootballRegressionTestSupport.attackingTactic("Aggressive Press"),
                FootballRegressionTestSupport.balancedTraining(),
                FootballRegressionTestSupport.coach("attack-coach", "Attacking Play", 84)
        );
        FootballTeam defensiveHome = FootballRegressionTestSupport.team(
                "home-defense",
                "Same Home",
                70,
                FootballRegressionTestSupport.defensiveTactic("Low Block"),
                FootballRegressionTestSupport.balancedTraining(),
                FootballRegressionTestSupport.coach("defense-coach", "Defensive Organization", 84)
        );
        FootballTeam balancedAwayA = FootballRegressionTestSupport.team("away-a", "Same Away", 70);
        FootballTeam balancedAwayB = FootballRegressionTestSupport.team("away-b", "Same Away", 70);

        FootballMatch attackingMatch = FootballRegressionTestSupport.preparedMatch(sport, attackingHome, balancedAwayA);
        FootballMatch defensiveMatch = FootballRegressionTestSupport.preparedMatch(sport, defensiveHome, balancedAwayB);

        sport.getMatchEngine().simulate(attackingMatch, sport.getRuleset());
        sport.getMatchEngine().simulate(defensiveMatch, sport.getRuleset());

        assertTrue(attackingMatch.getHomeScore() >= defensiveMatch.getHomeScore());
    }

    @Test
    void repeatedSimulationWithSameStateIsStable() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballTeam homeA = FootballRegressionTestSupport.team("home-a", "Stable Home", 76);
        FootballTeam awayA = FootballRegressionTestSupport.team("away-a", "Stable Away", 72);
        FootballTeam homeB = FootballRegressionTestSupport.team("home-b", "Stable Home", 76);
        FootballTeam awayB = FootballRegressionTestSupport.team("away-b", "Stable Away", 72);

        FootballMatch first = FootballRegressionTestSupport.preparedMatch(sport, homeA, awayA);
        FootballMatch second = FootballRegressionTestSupport.preparedMatch(sport, homeB, awayB);

        sport.getMatchEngine().simulate(first, sport.getRuleset());
        sport.getMatchEngine().simulate(second, sport.getRuleset());

        assertEquals(first.getHomeScore(), second.getHomeScore());
        assertEquals(first.getAwayScore(), second.getAwayScore());
    }
}
