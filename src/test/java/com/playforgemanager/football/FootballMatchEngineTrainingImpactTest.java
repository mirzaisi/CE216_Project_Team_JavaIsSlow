package com.playforgemanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballMatchEngineTrainingImpactTest {
    @Test
    void attackingTrainingCanImproveDeterministicMatchOutput() {
        FootballRuleset ruleset = new FootballRuleset();
        FootballTrainingEffectService trainingEffectService = new FootballTrainingEffectService();

        FootballTeam trainedHome = createComparableTeam("HOME", "Home FC");
        FootballTeam plainHome = createComparableTeam("HOME", "Home FC");
        FootballTeam awayForTrainedMatch = createComparableTeam("AWAY", "Away FC");
        FootballTeam awayForPlainMatch = createComparableTeam("AWAY", "Away FC");

        trainedHome.assignTrainingPlan(new FootballTrainingPlan("Attacking Play", 85, 70, 70, false));
        plainHome.assignTrainingPlan(new FootballTrainingPlan("Recovery", 10, 10, 10, false));

        trainingEffectService.applyWeeklyTraining(trainedHome);
        trainingEffectService.applyWeeklyTraining(plainHome);
        trainingEffectService.applyWeeklyTraining(awayForTrainedMatch);
        trainingEffectService.applyWeeklyTraining(awayForPlainMatch);

        FootballMatch trainedMatch = new FootballMatch(trainedHome, awayForTrainedMatch);
        FootballMatch plainMatch = new FootballMatch(plainHome, awayForPlainMatch);

        trainedMatch.setHomeSetup(ruleset.buildLineup(trainedHome.getAvailablePlayers()), defaultTactic());
        trainedMatch.setAwaySetup(ruleset.buildLineup(awayForTrainedMatch.getAvailablePlayers()), defaultTactic());
        plainMatch.setHomeSetup(ruleset.buildLineup(plainHome.getAvailablePlayers()), defaultTactic());
        plainMatch.setAwaySetup(ruleset.buildLineup(awayForPlainMatch.getAvailablePlayers()), defaultTactic());

        FootballMatchEngine engine = new FootballMatchEngine(0L);
        engine.simulate(trainedMatch, ruleset);
        engine.simulate(plainMatch, ruleset);

        assertTrue(trainedMatch.getHomeScore() >= plainMatch.getHomeScore());
    }

    private FootballTeam createComparableTeam(String idPrefix, String name) {
        FootballTeam team = new FootballTeam(idPrefix, name);
        team.addPlayer(player(idPrefix + "-GK", FootballPosition.GOALKEEPER, 52, 80, 68, 62, 55));
        team.addPlayer(player(idPrefix + "-D1", FootballPosition.DEFENDER, 52, 78, 74, 66, 63));
        team.addPlayer(player(idPrefix + "-D2", FootballPosition.DEFENDER, 52, 78, 74, 66, 63));
        team.addPlayer(player(idPrefix + "-D3", FootballPosition.DEFENDER, 52, 78, 74, 66, 63));
        team.addPlayer(player(idPrefix + "-D4", FootballPosition.DEFENDER, 52, 78, 74, 66, 63));
        team.addPlayer(player(idPrefix + "-M1", FootballPosition.MIDFIELDER, 69, 67, 76, 81, 72));
        team.addPlayer(player(idPrefix + "-M2", FootballPosition.MIDFIELDER, 69, 67, 76, 81, 72));
        team.addPlayer(player(idPrefix + "-M3", FootballPosition.MIDFIELDER, 69, 67, 76, 81, 72));
        team.addPlayer(player(idPrefix + "-M4", FootballPosition.MIDFIELDER, 69, 67, 76, 81, 72));
        team.addPlayer(player(idPrefix + "-F1", FootballPosition.FORWARD, 84, 48, 73, 71, 79));
        team.addPlayer(player(idPrefix + "-F2", FootballPosition.FORWARD, 84, 48, 73, 71, 79));
        return team;
    }

    private FootballPlayer player(String id, FootballPosition position, int attack, int defense, int stamina, int passing, int speed) {
        return new FootballPlayer(id, id, position, new FootballAttributeProfile(attack, defense, stamina, passing, speed));
    }

    private FootballTactic defaultTactic() {
        return new FootballTactic("Balanced", "4-3-3", FootballTactic.Mentality.BALANCED, 55, 55);
    }
}
