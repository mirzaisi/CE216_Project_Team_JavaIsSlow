package com.playforgemanager.football;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.save.SaveCoachData;
import com.playforgemanager.application.save.SaveFixtureData;
import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameRestorer;
import com.playforgemanager.application.save.SaveLineupData;
import com.playforgemanager.application.save.SavePlayedMatchData;
import com.playforgemanager.application.save.SavePlayerData;
import com.playforgemanager.application.save.SaveRestoreSupport;
import com.playforgemanager.application.save.SaveSeasonData;
import com.playforgemanager.application.save.SaveSessionData;
import com.playforgemanager.application.save.SaveTacticData;
import com.playforgemanager.application.save.SaveTeamData;
import com.playforgemanager.application.save.SaveTrainingPlanData;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Team;
import com.playforgemanager.core.ProgressionState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FootballSaveGameRestorer implements SaveGameRestorer {
    private static final String SPORT = "football";

    @Override
    public GameSession restore(SaveGameDocument document, SportRegistration registration) {
        Objects.requireNonNull(document, "Save document cannot be null.");
        Objects.requireNonNull(registration, "Sport registration cannot be null.");

        SaveSessionData savedSession = document.session();

        requireSport(savedSession.sportId());

        Sport sport = registration.getSportFactory().createSport();

        if (!(sport instanceof FootballSport)) {
            throw new IllegalStateException("Football save requires a FootballSport registration.");
        }

        RestoredLeague restoredLeague = restoreLeague(savedSession.season());
        FootballSeason season = new FootballSeason(restoredLeague.league());

        // Rebuilds standings before restoring the saved season progress.
        season.refreshStandings(sport.getStandingsPolicy());
        season.restoreProgress(savedSession.season().currentWeek(), savedSession.season().completed());

        Team controlledTeam = restoredLeague.teamsById().get(savedSession.controlledTeamId());

        if (controlledTeam == null) {
            throw new IllegalArgumentException("Controlled team was not found in saved league.");
        }

        ProgressionState progressionState = SaveRestoreSupport.requireProgressionState(
                savedSession.progressionState()
        );

        return new GameSession(
                sport,
                season,
                controlledTeam,
                progressionState,
                savedSession.sportId()
        );
    }

    private RestoredLeague restoreLeague(SaveSeasonData savedSeason) {
        FootballLeague league = new FootballLeague(savedSeason.leagueName());
        Map<String, FootballTeam> teamsById = new LinkedHashMap<>();

        // Restores all teams before rebuilding fixtures that reference them.
        for (SaveTeamData savedTeam : savedSeason.teams()) {
            FootballTeam team = restoreTeam(savedTeam);

            if (teamsById.put(team.getId(), team) != null) {
                throw new IllegalArgumentException("Duplicate saved team id: " + team.getId());
            }

            league.addTeam(team);
        }

        for (SaveFixtureData savedFixture : savedSeason.fixtures()) {
            FootballTeam homeTeam = requireTeam(teamsById, savedFixture.homeTeamId());
            FootballTeam awayTeam = requireTeam(teamsById, savedFixture.awayTeamId());

            Fixture fixture = new Fixture(savedFixture.week(), homeTeam, awayTeam);

            if (savedFixture.playedMatch() != null) {
                fixture.attachPlayedMatch(restorePlayedMatch(
                        savedFixture.playedMatch(),
                        homeTeam,
                        awayTeam
                ));
            }

            league.addFixture(fixture);
        }

        return new RestoredLeague(league, Map.copyOf(teamsById));
    }

    private FootballTeam restoreTeam(SaveTeamData savedTeam) {
        FootballTeam team = new FootballTeam(savedTeam.id(), savedTeam.name());
        Map<String, FootballPlayer> playersById = new LinkedHashMap<>();

        // Restores the roster first so lineups can reference saved players.
        for (SavePlayerData savedPlayer : savedTeam.players()) {
            FootballPlayer player = restorePlayer(savedPlayer);

            if (playersById.put(player.getId(), player) != null) {
                throw new IllegalArgumentException("Duplicate saved player id: " + player.getId());
            }

            team.addPlayer(player);
        }

        for (SaveCoachData savedCoach : savedTeam.coaches()) {
            team.addCoach(restoreCoach(savedCoach));
        }

        if (savedTeam.selectedLineup() != null) {
            FootballLineup lineup = restoreLineup(savedTeam.selectedLineup(), playersById);

            team.assignLineup(lineup);
        }

        if (savedTeam.selectedTactic() != null) {
            team.assignTactic(restoreTactic(savedTeam.selectedTactic()));
        }

        if (savedTeam.selectedTrainingPlan() != null) {
            team.assignTrainingPlan(restoreTrainingPlan(savedTeam.selectedTrainingPlan()));
        }

        return team;
    }

    private FootballPlayer restorePlayer(SavePlayerData savedPlayer) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedPlayer.properties());
        String positionValue = SaveRestoreSupport.requireString(properties, "position", SPORT);

        FootballPosition position = SaveRestoreSupport.requireEnum(
                FootballPosition.class,
                positionValue,
                SPORT
        );

        FootballAttributeProfile attributeProfile = new FootballAttributeProfile(
                SaveRestoreSupport.requireInt(properties, "attributeProfile.attack", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.defense", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.stamina", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.passing", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.speed", SPORT)
        );

        FootballPlayer player = new FootballPlayer(
                savedPlayer.id(),
                savedPlayer.name(),
                position,
                attributeProfile
        );

        restoreAvailability(savedPlayer, player);
        restoreWeeklyTrainingEffect(properties, player);

        return player;
    }

    private void restoreAvailability(SavePlayerData savedPlayer, FootballPlayer player) {
        // Injury countdown takes priority over the simple availability flag.
        if (savedPlayer.injuryMatchesRemaining() > 0) {
            player.injureForMatches(savedPlayer.injuryMatchesRemaining());
            return;
        }

        player.setAvailable(savedPlayer.available());
    }

    private void restoreWeeklyTrainingEffect(Map<String, String> properties, FootballPlayer player) {
        FootballTrainingEffect effect = new FootballTrainingEffect(
                SaveRestoreSupport.optionalInt(properties, "weeklyTrainingEffect.attackDelta", 0, SPORT),
                SaveRestoreSupport.optionalInt(properties, "weeklyTrainingEffect.defenseDelta", 0, SPORT),
                SaveRestoreSupport.optionalInt(properties, "weeklyTrainingEffect.staminaDelta", 0, SPORT),
                SaveRestoreSupport.optionalInt(properties, "weeklyTrainingEffect.passingDelta", 0, SPORT),
                SaveRestoreSupport.optionalInt(properties, "weeklyTrainingEffect.speedDelta", 0, SPORT),
                SaveRestoreSupport.optionalBoolean(
                        properties,
                        "weeklyTrainingEffect.acceleratedRecovery",
                        false,
                        SPORT
                )
        );

        player.applyWeeklyTrainingEffect(effect);
    }

    private FootballCoach restoreCoach(SaveCoachData savedCoach) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedCoach.properties());

        return new FootballCoach(
                savedCoach.id(),
                savedCoach.name(),
                savedCoach.role(),
                SaveRestoreSupport.requireString(properties, "specialization", SPORT),
                SaveRestoreSupport.requireInt(properties, "coachingRating", SPORT)
        );
    }

    private FootballMatch restorePlayedMatch(
            SavePlayedMatchData savedMatch,
            FootballTeam homeTeam,
            FootballTeam awayTeam
    ) {
        FootballMatch match = new FootballMatch(homeTeam, awayTeam);

        // Restores both team setups before applying the saved match result.
        match.setHomeSetup(
                restoreLineup(savedMatch.homeLineup(), playersById(homeTeam)),
                restoreTactic(savedMatch.homeTactic())
        );

        match.setAwaySetup(
                restoreLineup(savedMatch.awayLineup(), playersById(awayTeam)),
                restoreTactic(savedMatch.awayTactic())
        );

        match.setResult(savedMatch.homeScore(), savedMatch.awayScore());

        return match;
    }

    private FootballLineup restoreLineup(
            SaveLineupData savedLineup,
            Map<String, FootballPlayer> playersById
    ) {
        List<FootballPlayer> starters = new ArrayList<>();

        for (String playerId : savedLineup.selectedPlayerIds()) {
            starters.add(requirePlayer(playersById, playerId));
        }

        List<FootballPlayer> bench = new ArrayList<>();

        for (String playerId : savedLineup.reservePlayerIds()) {
            bench.add(requirePlayer(playersById, playerId));
        }

        return new FootballLineup(starters, bench);
    }

    private FootballTactic restoreTactic(SaveTacticData savedTactic) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedTactic.properties());

        String formation = SaveRestoreSupport.requireString(properties, "formation", SPORT);
        String mentalityValue = SaveRestoreSupport.requireString(properties, "mentality", SPORT);

        FootballTactic.Mentality mentality = SaveRestoreSupport.requireEnum(
                FootballTactic.Mentality.class,
                mentalityValue,
                SPORT
        );

        int pressingIntensity = SaveRestoreSupport.requireInt(properties, "pressingIntensity", SPORT);
        int attackingWidth = SaveRestoreSupport.requireInt(properties, "attackingWidth", SPORT);

        return new FootballTactic(
                savedTactic.name(),
                formation,
                mentality,
                pressingIntensity,
                attackingWidth
        );
    }

    private FootballTrainingPlan restoreTrainingPlan(SaveTrainingPlanData savedPlan) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedPlan.properties());

        int conditioningLoad = SaveRestoreSupport.requireInt(properties, "conditioningLoad", SPORT);
        int tacticalLoad = SaveRestoreSupport.requireInt(properties, "tacticalLoad", SPORT);
        boolean recoveryIncluded = SaveRestoreSupport.requireBoolean(properties, "recoveryIncluded", SPORT);

        return new FootballTrainingPlan(
                savedPlan.focus(),
                savedPlan.intensity(),
                conditioningLoad,
                tacticalLoad,
                recoveryIncluded
        );
    }

    private Map<String, FootballPlayer> playersById(FootballTeam team) {
        Map<String, FootballPlayer> players = new LinkedHashMap<>();

        // Builds a lookup map for restoring saved lineup player references.
        for (Player player : team.getRoster()) {
            if (!(player instanceof FootballPlayer footballPlayer)) {
                throw new IllegalArgumentException("Football team contains a non-football player.");
            }

            players.put(footballPlayer.getId(), footballPlayer);
        }

        return players;
    }

    private FootballTeam requireTeam(Map<String, FootballTeam> teamsById, String teamId) {
        FootballTeam team = teamsById.get(teamId);

        if (team == null) {
            throw new IllegalArgumentException("Saved fixture references missing team: " + teamId);
        }

        return team;
    }

    private FootballPlayer requirePlayer(Map<String, FootballPlayer> playersById, String playerId) {
        FootballPlayer player = playersById.get(playerId);

        if (player == null) {
            throw new IllegalArgumentException("Saved lineup references missing player: " + playerId);
        }

        return player;
    }

    private void requireSport(String sportId) {
        if (!SPORT.equals(sportId.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Football restorer cannot load sport: " + sportId);
        }
    }

    private record RestoredLeague(
            FootballLeague league,
            Map<String, FootballTeam> teamsById
    ) {
    }
}
