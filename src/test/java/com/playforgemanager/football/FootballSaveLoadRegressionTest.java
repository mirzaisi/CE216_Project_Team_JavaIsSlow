package com.playforgemanager.football;

import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameDocumentMapper;
import com.playforgemanager.core.GameSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSaveLoadRegressionTest {
    @Test
    void footballSaveLoadPreservesTrainingCoachTacticLineupInjuryAndAttributes() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason season = FootballRegressionTestSupport.playableSeason(2);
        FootballTeam originalTeam = (FootballTeam) season.getLeague().getTeams().get(0);
        FootballPlayer trainedPlayer = (FootballPlayer) originalTeam.getFootballPlayers().get(0);

        List<String> selectedIds = new ArrayList<>();
        for (Object selected : originalTeam.getSelectedFootballLineup().getAllPlayers()) {
            selectedIds.add(((FootballPlayer) selected).getId());
        }
        List<FootballPlayer> unselectedPlayers = new ArrayList<>();
        for (Object candidate : originalTeam.getFootballPlayers()) {
            FootballPlayer player = (FootballPlayer) candidate;
            if (!selectedIds.contains(player.getId())) {
                unselectedPlayers.add(player);
            }
        }
        FootballPlayer injuredPlayer = unselectedPlayers.get(0);
        FootballPlayer unavailablePlayer = unselectedPlayers.get(1);

        trainedPlayer.applyWeeklyTrainingEffect(new FootballTrainingEffect(2, 1, 3, 4, 5, true));
        injuredPlayer.injureForMatches(2);
        unavailablePlayer.setAvailable(false);

        GameSession originalSession = FootballRegressionTestSupport.sessionWithSeason(sport, season);
        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(originalSession);
        GameSession restoredSession = new FootballSaveGameRestorer().restore(
                document,
                FootballRegressionTestSupport.footballRegistration(2)
        );
        FootballTeam restoredTeam = (FootballTeam) restoredSession.getControlledTeam();

        FootballPlayer restoredTrainedPlayer = findPlayer(restoredTeam, trainedPlayer.getId());
        assertEquals(trainedPlayer.getPosition(), restoredTrainedPlayer.getPosition());
        assertEquals(trainedPlayer.getAttributeProfile().getAttack(), restoredTrainedPlayer.getAttributeProfile().getAttack());
        assertEquals(trainedPlayer.getAttributeProfile().getDefense(), restoredTrainedPlayer.getAttributeProfile().getDefense());
        assertEquals(trainedPlayer.getWeeklyTrainingEffect(), restoredTrainedPlayer.getWeeklyTrainingEffect());
        assertEquals(2, findPlayer(restoredTeam, injuredPlayer.getId()).getInjuryMatchesRemaining());
        assertFalse(findPlayer(restoredTeam, injuredPlayer.getId()).isAvailable());
        assertFalse(findPlayer(restoredTeam, unavailablePlayer.getId()).isAvailable());

        assertNotNull(restoredTeam.getSelectedFootballLineup());
        assertNotNull(restoredTeam.getSelectedFootballTactic());
        assertNotNull(restoredTeam.getSelectedFootballTrainingPlan());
        assertEquals(originalTeam.getSelectedFootballTactic().getName(), restoredTeam.getSelectedFootballTactic().getName());
        assertEquals(originalTeam.getSelectedFootballTrainingPlan().getFocus(), restoredTeam.getSelectedFootballTrainingPlan().getFocus());
        assertEquals(((FootballCoach) originalTeam.getCoaches().get(0)).getSpecialization(), ((FootballCoach) restoredTeam.getCoaches().get(0)).getSpecialization());
        assertEquals(((FootballCoach) originalTeam.getCoaches().get(0)).getCoachingRating(), ((FootballCoach) restoredTeam.getCoaches().get(0)).getCoachingRating());
        assertTrue(((FootballSport) restoredSession.getActiveSport()).getFootballRuleset().isValidLineup(restoredTeam.getSelectedFootballLineup()));
    }

    @Test
    void loadedSessionSimulatesSameImmediatePreparedMatchAsUnsavedSession() {
        FootballSport sport = FootballRegressionTestSupport.sport();
        FootballSeason season = FootballRegressionTestSupport.playableSeason(2);
        GameSession originalSession = FootballRegressionTestSupport.sessionWithSeason(sport, season);
        SaveGameDocument document = new SaveGameDocumentMapper().toDocument(originalSession);
        GameSession restoredSession = new FootballSaveGameRestorer().restore(
                document,
                FootballRegressionTestSupport.footballRegistration(2)
        );

        FootballTeam originalHome = (FootballTeam) originalSession.getControlledTeam();
        FootballTeam originalAway = (FootballTeam) originalSession.getCurrentSeason().getLeague().getTeams().get(1);
        FootballTeam restoredHome = (FootballTeam) restoredSession.getControlledTeam();
        FootballTeam restoredAway = (FootballTeam) restoredSession.getCurrentSeason().getLeague().getTeams().get(1);

        FootballMatch originalMatch = FootballRegressionTestSupport.preparedMatch(sport, originalHome, originalAway);
        FootballMatch restoredMatch = FootballRegressionTestSupport.preparedMatch((FootballSport) restoredSession.getActiveSport(), restoredHome, restoredAway);

        originalSession.getActiveSport().getMatchEngine().simulate(originalMatch, originalSession.getActiveSport().getRuleset());
        restoredSession.getActiveSport().getMatchEngine().simulate(restoredMatch, restoredSession.getActiveSport().getRuleset());

        assertEquals(originalMatch.getHomeScore(), restoredMatch.getHomeScore());
        assertEquals(originalMatch.getAwayScore(), restoredMatch.getAwayScore());
    }

    private FootballPlayer findPlayer(FootballTeam team, String playerId) {
        for (Object candidate : team.getFootballPlayers()) {
            FootballPlayer player = (FootballPlayer) candidate;
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        throw new IllegalArgumentException("Player not found: " + playerId);
    }
}
