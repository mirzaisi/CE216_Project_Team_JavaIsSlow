package com.playforgemanager.football;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.save.SaveCoachData;
import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameDocumentMapper;
import com.playforgemanager.application.save.SavePlayerData;
import com.playforgemanager.application.save.SavePropertyValue;
import com.playforgemanager.application.save.SaveTeamData;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Sport;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballPersistenceSafetyTest {
    @Test
    void footballSpecificFieldsAreMappedIntoSharedSaveProperties() {
        GameSession session = buildSessionWithFootballState();
        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(session);

        SaveTeamData savedTeam = document.session().season().teams().get(0);
        SavePlayerData trainedPlayer = savedTeam.players().get(0);
        SavePlayerData injuredPlayer = savedTeam.players().get(18);
        SaveCoachData coach = savedTeam.coaches().get(0);

        Map<String, String> playerProperties = propertiesByKey(trainedPlayer.properties());
        assertEquals("GOALKEEPER", playerProperties.get("position"));
        assertEquals("70", playerProperties.get("attributeProfile.attack"));
        assertEquals("81", playerProperties.get("attributeProfile.defense"));
        assertEquals("73", playerProperties.get("attributeProfile.stamina"));
        assertEquals("66", playerProperties.get("attributeProfile.passing"));
        assertEquals("58", playerProperties.get("attributeProfile.speed"));
        assertEquals("2", playerProperties.get("weeklyTrainingEffect.attackDelta"));
        assertEquals("1", playerProperties.get("weeklyTrainingEffect.defenseDelta"));
        assertEquals("3", playerProperties.get("weeklyTrainingEffect.staminaDelta"));
        assertEquals("4", playerProperties.get("weeklyTrainingEffect.passingDelta"));
        assertEquals("5", playerProperties.get("weeklyTrainingEffect.speedDelta"));
        assertEquals("true", playerProperties.get("weeklyTrainingEffect.acceleratedRecovery"));
        assertFalse(playerProperties.containsKey("effectiveAttributeProfile.attack"));
        assertFalse(playerProperties.containsKey("attributeProfile.overallRating"));

        assertEquals(2, injuredPlayer.injuryMatchesRemaining());
        assertFalse(injuredPlayer.available());

        Map<String, String> coachProperties = propertiesByKey(coach.properties());
        assertEquals("Attacking Play", coachProperties.get("specialization"));
        assertEquals("88", coachProperties.get("coachingRating"));
        assertFalse(coachProperties.containsKey("rating"));

        Map<String, String> tacticProperties = propertiesByKey(savedTeam.selectedTactic().properties());
        assertEquals("4-3-3", tacticProperties.get("formation"));
        assertEquals("ATTACKING", tacticProperties.get("mentality"));
        assertEquals("82", tacticProperties.get("pressingIntensity"));
        assertEquals("74", tacticProperties.get("attackingWidth"));

        Map<String, String> trainingProperties = propertiesByKey(savedTeam.selectedTrainingPlan().properties());
        assertEquals("68", trainingProperties.get("conditioningLoad"));
        assertEquals("71", trainingProperties.get("tacticalLoad"));
        assertEquals("true", trainingProperties.get("recoveryIncluded"));
    }

    @Test
    void footballSpecificStateRestoresWithoutRegenerationOrHiddenRecomputation() {
        GameSession originalSession = buildSessionWithFootballState();
        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(originalSession);

        FootballSaveGameRestorer restorer = new FootballSaveGameRestorer();
        GameSession restoredSession = restorer.restore(
                document,
                new SportRegistration(
                        "football",
                        "Football",
                        new FootballSportFactory(new InMemoryAssetProvider(), 2)
                )
        );

        FootballTeam originalTeam = (FootballTeam) originalSession.getControlledTeam();
        FootballTeam restoredTeam = (FootballTeam) restoredSession.getControlledTeam();

        assertEquals(originalTeam.getId(), restoredTeam.getId());
        assertEquals(originalTeam.getName(), restoredTeam.getName());
        assertEquals(originalTeam.getFootballPlayers().size(), restoredTeam.getFootballPlayers().size());
        assertEquals(originalSession.getCurrentSeason().getCurrentWeek(), restoredSession.getCurrentSeason().getCurrentWeek());
        assertEquals(originalSession.getCurrentSeason().isCompleted(), restoredSession.getCurrentSeason().isCompleted());

        FootballPlayer restoredTrainedPlayer = restoredTeam.getFootballPlayers().get(0);
        assertEquals(FootballPosition.GOALKEEPER, restoredTrainedPlayer.getPosition());
        assertEquals(70, restoredTrainedPlayer.getAttributeProfile().getAttack());
        assertEquals(81, restoredTrainedPlayer.getAttributeProfile().getDefense());
        assertEquals(73, restoredTrainedPlayer.getAttributeProfile().getStamina());
        assertEquals(66, restoredTrainedPlayer.getAttributeProfile().getPassing());
        assertEquals(58, restoredTrainedPlayer.getAttributeProfile().getSpeed());
        assertEquals(2, restoredTrainedPlayer.getWeeklyTrainingEffect().attackDelta());
        assertEquals(1, restoredTrainedPlayer.getWeeklyTrainingEffect().defenseDelta());
        assertEquals(3, restoredTrainedPlayer.getWeeklyTrainingEffect().staminaDelta());
        assertEquals(4, restoredTrainedPlayer.getWeeklyTrainingEffect().passingDelta());
        assertEquals(5, restoredTrainedPlayer.getWeeklyTrainingEffect().speedDelta());
        assertTrue(restoredTrainedPlayer.getWeeklyTrainingEffect().acceleratedRecovery());

        FootballPlayer restoredInjuredPlayer = restoredTeam.getFootballPlayers().get(18);
        assertEquals(2, restoredInjuredPlayer.getInjuryMatchesRemaining());
        assertFalse(restoredInjuredPlayer.isAvailable());

        FootballPlayer restoredUnavailablePlayer = restoredTeam.getFootballPlayers().get(19);
        assertEquals(0, restoredUnavailablePlayer.getInjuryMatchesRemaining());
        assertFalse(restoredUnavailablePlayer.isAvailable());

        FootballCoach restoredCoach = restoredTeam.getCoaches().get(0);
        assertEquals("Attacking Play", restoredCoach.getSpecialization());
        assertEquals(88, restoredCoach.getCoachingRating());

        FootballTactic restoredTactic = restoredTeam.getSelectedFootballTactic();
        assertNotNull(restoredTactic);
        assertEquals("High Press", restoredTactic.getName());
        assertEquals("4-3-3", restoredTactic.getFormation());
        assertEquals(FootballTactic.Mentality.ATTACKING, restoredTactic.getMentality());
        assertEquals(82, restoredTactic.getPressingIntensity());
        assertEquals(74, restoredTactic.getAttackingWidth());

        FootballTrainingPlan restoredTrainingPlan = restoredTeam.getSelectedFootballTrainingPlan();
        assertNotNull(restoredTrainingPlan);
        assertEquals("Attacking Press", restoredTrainingPlan.getFocus());
        assertEquals(76, restoredTrainingPlan.getIntensity());
        assertEquals(68, restoredTrainingPlan.getConditioningLoad());
        assertEquals(71, restoredTrainingPlan.getTacticalLoad());
        assertTrue(restoredTrainingPlan.isRecoveryIncluded());

        FootballLineup restoredLineup = restoredTeam.getSelectedFootballLineup();
        assertNotNull(restoredLineup);
        assertEquals(11, restoredLineup.getStartingPlayers().size());
        assertEquals(7, restoredLineup.getBenchPlayers().size());
        assertTrue(new FootballRuleset().isValidLineup(restoredLineup));
    }

    @Test
    void restoredFootballSessionProducesSameImmediateSimulationAsUnsavedSession() {
        GameSession originalSession = buildSessionWithFootballState();
        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(originalSession);

        GameSession restoredSession = new FootballSaveGameRestorer().restore(
                document,
                new SportRegistration(
                        "football",
                        "Football",
                        new FootballSportFactory(new InMemoryAssetProvider(), 2)
                )
        );

        FootballTeam originalHome = (FootballTeam) originalSession.getControlledTeam();
        FootballTeam originalAway = (FootballTeam) originalSession.getCurrentSeason().getLeague().getTeams().stream()
                .filter(team -> !team.getId().equals(originalHome.getId()))
                .findFirst()
                .map(FootballTeam.class::cast)
                .orElseThrow();

        FootballTeam restoredHome = (FootballTeam) restoredSession.getControlledTeam();
        FootballTeam restoredAway = (FootballTeam) restoredSession.getCurrentSeason().getLeague().getTeams().stream()
                .filter(team -> !team.getId().equals(restoredHome.getId()))
                .findFirst()
                .map(FootballTeam.class::cast)
                .orElseThrow();

        FootballMatch originalMatch = buildPreparedMatch(originalSession.getActiveSport(), originalHome, originalAway);
        FootballMatch restoredMatch = buildPreparedMatch(restoredSession.getActiveSport(), restoredHome, restoredAway);

        originalSession.getActiveSport().getMatchEngine().simulate(originalMatch, originalSession.getActiveSport().getRuleset());
        restoredSession.getActiveSport().getMatchEngine().simulate(restoredMatch, restoredSession.getActiveSport().getRuleset());

        assertEquals(originalMatch.getHomeScore(), restoredMatch.getHomeScore());
        assertEquals(originalMatch.getAwayScore(), restoredMatch.getAwayScore());
    }

    private GameSession buildSessionWithFootballState() {
        FootballSport sport = new FootballSport();
        FootballRuleset ruleset = sport.getFootballRuleset();
        FootballLeague league = new FootballLeague("Persistence League");

        FootballTeam homeTeam = buildTeam("home", "Home FC", 0);
        FootballTeam awayTeam = buildTeam("away", "Away FC", 100);

        league.addTeam(homeTeam);
        league.addTeam(awayTeam);
        league.addFixture(new Fixture(1, homeTeam, awayTeam));

        homeTeam.assignTactic(new FootballTactic(
                "High Press",
                "4-3-3",
                FootballTactic.Mentality.ATTACKING,
                82,
                74
        ));
        homeTeam.assignTrainingPlan(new FootballTrainingPlan(
                "Attacking Press",
                76,
                68,
                71,
                true
        ));
        homeTeam.addCoach(new FootballCoach(
                "home-coach-1",
                "Coach Home",
                "Head Coach",
                "Attacking Play",
                88
        ));
        homeTeam.getFootballPlayers().get(18).injureForMatches(2);
        homeTeam.getFootballPlayers().get(19).setAvailable(false);
        homeTeam.assignLineup(ruleset.buildLineup(homeTeam.getAvailablePlayers()), ruleset);
        homeTeam.getFootballPlayers().get(0).applyWeeklyTrainingEffect(
                new FootballTrainingEffect(2, 1, 3, 4, 5, true)
        );

        awayTeam.assignTactic(new FootballTactic(
                "Balanced Control",
                "4-2-3-1",
                FootballTactic.Mentality.BALANCED,
                56,
                58
        ));
        awayTeam.assignTrainingPlan(new FootballTrainingPlan(
                "Balanced Development",
                60,
                55,
                52,
                false
        ));
        awayTeam.addCoach(new FootballCoach(
                "away-coach-1",
                "Coach Away",
                "Head Coach",
                "General Management",
                73
        ));
        awayTeam.assignLineup(ruleset.buildLineup(awayTeam.getAvailablePlayers()), ruleset);

        FootballSeason season = new FootballSeason(league);
        season.refreshStandings(sport.getStandingsPolicy());
        return new GameSession(sport, season, homeTeam, ProgressionState.IN_PROGRESS, "football");
    }

    private FootballTeam buildTeam(String idPrefix, String teamName, int seed) {
        FootballTeam team = new FootballTeam(idPrefix + "-team", teamName);
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
                FootballPosition.FORWARD,
                FootballPosition.GOALKEEPER,
                FootballPosition.DEFENDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.FORWARD,
                FootballPosition.DEFENDER,
                FootballPosition.MIDFIELDER,
                FootballPosition.FORWARD,
                FootballPosition.GOALKEEPER,
                FootballPosition.FORWARD
        );

        for (int i = 0; i < positions.size(); i++) {
            FootballPosition position = positions.get(i);
            team.addPlayer(new FootballPlayer(
                    idPrefix + "-player-" + (i + 1),
                    teamName + " Player " + (i + 1),
                    position,
                    profileFor(position, seed + i)
            ));
        }
        return team;
    }

    private FootballAttributeProfile profileFor(FootballPosition position, int seed) {
        int modifier = seed % 6;
        return switch (position) {
            case GOALKEEPER -> new FootballAttributeProfile(70 + modifier, 81 + modifier, 73, 66, 58);
            case DEFENDER -> new FootballAttributeProfile(58 + modifier, 79, 76, 68, 64);
            case MIDFIELDER -> new FootballAttributeProfile(72 + modifier, 68, 78, 82, 73);
            case FORWARD -> new FootballAttributeProfile(85 + modifier, 50, 75, 70, 80);
        };
    }

    private FootballMatch buildPreparedMatch(Sport sport, FootballTeam homeTeam, FootballTeam awayTeam) {
        FootballMatch match = new FootballMatch(homeTeam, awayTeam);
        match.setHomeSetup(homeTeam.getSelectedFootballLineup(), homeTeam.getSelectedFootballTactic());
        match.setAwaySetup(awayTeam.getSelectedFootballLineup(), awayTeam.getSelectedFootballTactic());
        sport.getRuleset().isValidLineup(match.getHomeLineup());
        sport.getRuleset().isValidLineup(match.getAwayLineup());
        return match;
    }

    private Map<String, String> propertiesByKey(List<SavePropertyValue> properties) {
        Map<String, String> values = new LinkedHashMap<>();
        for (SavePropertyValue property : properties) {
            values.put(property.key(), property.value());
        }
        return values;
    }
}
