package com.playforgemanager.handball;

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
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HandballSaveGameRestorer implements SaveGameRestorer {
    private static final String SPORT = "handball";

    @Override
    public GameSession restore(SaveGameDocument document, SportRegistration registration) {
        Objects.requireNonNull(document, "Save document cannot be null.");
        Objects.requireNonNull(registration, "Sport registration cannot be null.");

        SaveSessionData savedSession = document.session();

        requireSport(savedSession.sportId());

        Sport sport = registration.getSportFactory().createSport();

        if (!(sport instanceof HandballSport)) {
            throw new IllegalStateException("Handball save requires a HandballSport registration.");
        }

        RestoredLeague restoredLeague = restoreLeague(savedSession.season());
        HandballSeason season = new HandballSeason(restoredLeague.league());

        season.refreshStandings(sport.getStandingsPolicy());

        restoreSeasonProgress(
                season,
                savedSession.season().currentWeek(),
                savedSession.season().completed()
        );

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
        HandballLeague league = new HandballLeague(savedSeason.leagueName());
        Map<String, HandballTeam> teamsById = new LinkedHashMap<>();

        // Restores teams first so fixtures can reference them safely.
        for (SaveTeamData savedTeam : savedSeason.teams()) {
            HandballTeam team = restoreTeam(savedTeam);

            if (teamsById.put(team.getId(), team) != null) {
                throw new IllegalArgumentException("Duplicate saved team id: " + team.getId());
            }

            league.addTeam(team);
        }

        // Restores fixtures and reconnects played matches when available.
        for (SaveFixtureData savedFixture : savedSeason.fixtures()) {
            HandballTeam homeTeam = requireTeam(teamsById, savedFixture.homeTeamId());
            HandballTeam awayTeam = requireTeam(teamsById, savedFixture.awayTeamId());

            Fixture fixture = new Fixture(savedFixture.week(), homeTeam, awayTeam);

            if (savedFixture.playedMatch() != null) {
                fixture.attachPlayedMatch(
                        restorePlayedMatch(savedFixture.playedMatch(), homeTeam, awayTeam)
                );
            }

            league.addFixture(fixture);
        }

        return new RestoredLeague(league, Map.copyOf(teamsById));
    }

    private HandballTeam restoreTeam(SaveTeamData savedTeam) {
        HandballTeam team = new HandballTeam(savedTeam.id(), savedTeam.name());
        Map<String, HandballPlayer> playersById = new LinkedHashMap<>();

        // Restores the roster before selected lineup data is rebuilt.
        for (SavePlayerData savedPlayer : savedTeam.players()) {
            HandballPlayer player = restorePlayer(savedPlayer);

            if (playersById.put(player.getId(), player) != null) {
                throw new IllegalArgumentException("Duplicate saved player id: " + player.getId());
            }

            team.addPlayer(player);
        }

        for (SaveCoachData savedCoach : savedTeam.coaches()) {
            team.addCoach(restoreCoach(savedCoach));
        }

        if (savedTeam.selectedLineup() != null) {
            HandballLineup lineup = restoreLineup(savedTeam.selectedLineup(), playersById);

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

    private HandballPlayer restorePlayer(SavePlayerData savedPlayer) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedPlayer.properties());

        String positionValue = SaveRestoreSupport.requireString(properties, "position", SPORT);
        HandballPosition position = SaveRestoreSupport.requireEnum(
                HandballPosition.class,
                positionValue,
                SPORT
        );

        HandballAttributeProfile attributeProfile = new HandballAttributeProfile(
                SaveRestoreSupport.requireInt(properties, "attributeProfile.shooting", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.defense", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.passing", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.speed", SPORT),
                SaveRestoreSupport.requireInt(properties, "attributeProfile.reflexes", SPORT)
        );

        HandballPlayer player = new HandballPlayer(
                savedPlayer.id(),
                savedPlayer.name(),
                position,
                attributeProfile
        );

        // Restores injury duration first, otherwise restores the saved availability flag.
        if (savedPlayer.injuryMatchesRemaining() > 0) {
            player.injureForMatches(savedPlayer.injuryMatchesRemaining());
        } else {
            player.setAvailable(savedPlayer.available());
        }

        return player;
    }

    private HandballCoach restoreCoach(SaveCoachData savedCoach) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedCoach.properties());

        String specializationValue = SaveRestoreSupport.requireString(
                properties,
                "specialization",
                SPORT
        );
        HandballCoachSpecialization specialization = SaveRestoreSupport.requireEnum(
                HandballCoachSpecialization.class,
                specializationValue,
                SPORT
        );
        int coachingRating = SaveRestoreSupport.requireInt(
                properties,
                "coachingRating",
                SPORT
        );

        return new HandballCoach(
                savedCoach.id(),
                savedCoach.name(),
                savedCoach.role(),
                specialization,
                coachingRating
        );
    }

    private HandballMatch restorePlayedMatch(
            SavePlayedMatchData savedMatch,
            HandballTeam homeTeam,
            HandballTeam awayTeam
    ) {
        HandballMatch match = new HandballMatch(homeTeam, awayTeam);

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

    private HandballLineup restoreLineup(
            SaveLineupData savedLineup,
            Map<String, HandballPlayer> playersById
    ) {
        List<HandballPlayer> starters = new ArrayList<>();

        for (String playerId : savedLineup.selectedPlayerIds()) {
            starters.add(requirePlayer(playersById, playerId));
        }

        List<HandballPlayer> bench = new ArrayList<>();

        for (String playerId : savedLineup.reservePlayerIds()) {
            bench.add(requirePlayer(playersById, playerId));
        }

        return new HandballLineup(starters, bench);
    }

    private HandballTactic restoreTactic(SaveTacticData savedTactic) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedTactic.properties());

        return new HandballTactic(
                savedTactic.name(),
                SaveRestoreSupport.requireString(properties, "shape", SPORT),
                SaveRestoreSupport.requireEnum(
                        HandballTactic.Tempo.class,
                        SaveRestoreSupport.requireString(properties, "tempo", SPORT),
                        SPORT
                ),
                SaveRestoreSupport.requireInt(properties, "pressureLevel", SPORT),
                SaveRestoreSupport.requireInt(properties, "transitionSpeed", SPORT)
        );
    }

    private HandballTrainingPlan restoreTrainingPlan(SaveTrainingPlanData savedPlan) {
        Map<String, String> properties = SaveRestoreSupport.propertyMap(savedPlan.properties());

        return new HandballTrainingPlan(
                savedPlan.focus(),
                savedPlan.intensity(),
                SaveRestoreSupport.requireInt(properties, "conditioningLoad", SPORT),
                SaveRestoreSupport.requireInt(properties, "tacticalLoad", SPORT),
                SaveRestoreSupport.requireBoolean(properties, "recoveryIncluded", SPORT)
        );
    }

    private void restoreSeasonProgress(HandballSeason season, int savedWeek, boolean completed) {
        if (savedWeek < 1) {
            throw new IllegalArgumentException("Saved current week must be at least 1.");
        }

        // Advances only through season state, without recomputing matches or training.
        while (season.getCurrentWeek() < savedWeek && !season.isCompleted()) {
            season.advanceWeek();
        }

        if (completed && !season.isCompleted()) {
            while (!season.isCompleted()) {
                season.advanceWeek();
            }
        }
    }

    private Map<String, HandballPlayer> playersById(HandballTeam team) {
        Map<String, HandballPlayer> players = new LinkedHashMap<>();

        for (Player player : team.getRoster()) {
            if (!(player instanceof HandballPlayer handballPlayer)) {
                throw new IllegalArgumentException("Handball team contains a non-handball player.");
            }

            players.put(handballPlayer.getId(), handballPlayer);
        }

        return players;
    }

    private HandballTeam requireTeam(Map<String, HandballTeam> teamsById, String teamId) {
        HandballTeam team = teamsById.get(teamId);

        if (team == null) {
            throw new IllegalArgumentException("Saved fixture references missing team: " + teamId);
        }

        return team;
    }

    private HandballPlayer requirePlayer(Map<String, HandballPlayer> playersById, String playerId) {
        HandballPlayer player = playersById.get(playerId);

        if (player == null) {
            throw new IllegalArgumentException("Saved lineup references missing player: " + playerId);
        }

        return player;
    }

    private void requireSport(String sportId) {
        if (!SPORT.equals(sportId.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Handball restorer cannot load sport: " + sportId);
        }
    }

    private record RestoredLeague(HandballLeague league, Map<String, HandballTeam> teamsById) {
    }
}
