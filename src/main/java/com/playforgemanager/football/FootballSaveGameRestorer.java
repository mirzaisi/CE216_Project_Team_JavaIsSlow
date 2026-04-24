package com.playforgemanager.football;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.application.save.SaveCoachData;
import com.playforgemanager.application.save.SaveFixtureData;
import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameRestorer;
import com.playforgemanager.application.save.SaveLineupData;
import com.playforgemanager.application.save.SavePlayedMatchData;
import com.playforgemanager.application.save.SavePlayerData;
import com.playforgemanager.application.save.SavePropertyValue;
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

public class FootballSaveGameRestorer implements SaveGameRestorer {
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
        season.refreshStandings(sport.getStandingsPolicy());
        season.restoreProgress(savedSession.season().currentWeek(), savedSession.season().completed());

        Team controlledTeam = restoredLeague.teamsById().get(savedSession.controlledTeamId());
        if (controlledTeam == null) {
            throw new IllegalArgumentException("Controlled team was not found in saved league.");
        }

        return new GameSession(
                sport,
                season,
                controlledTeam,
                progressionState(savedSession.progressionState()),
                savedSession.sportId()
        );
    }

    private RestoredLeague restoreLeague(SaveSeasonData savedSeason) {
        FootballLeague league = new FootballLeague(savedSeason.leagueName());
        Map<String, FootballTeam> teamsById = new LinkedHashMap<>();

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
                fixture.attachPlayedMatch(restorePlayedMatch(savedFixture.playedMatch(), homeTeam, awayTeam));
            }
            league.addFixture(fixture);
        }

        return new RestoredLeague(league, Map.copyOf(teamsById));
    }

    private FootballTeam restoreTeam(SaveTeamData savedTeam) {
        FootballTeam team = new FootballTeam(savedTeam.id(), savedTeam.name());
        Map<String, FootballPlayer> playersById = new LinkedHashMap<>();

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
            team.assignLineup(restoreLineup(savedTeam.selectedLineup(), playersById));
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
        Map<String, String> properties = properties(savedPlayer.properties());
        FootballPlayer player = new FootballPlayer(
                savedPlayer.id(),
                savedPlayer.name(),
                enumValue(FootballPosition.class, required(properties, "position")),
                new FootballAttributeProfile(
                        intValue(properties, "attributeProfile.attack"),
                        intValue(properties, "attributeProfile.defense"),
                        intValue(properties, "attributeProfile.stamina"),
                        intValue(properties, "attributeProfile.passing"),
                        intValue(properties, "attributeProfile.speed")
                )
        );

        if (savedPlayer.injuryMatchesRemaining() > 0) {
            player.injureForMatches(savedPlayer.injuryMatchesRemaining());
        } else {
            player.setAvailable(savedPlayer.available());
        }

        return player;
    }

    private FootballCoach restoreCoach(SaveCoachData savedCoach) {
        Map<String, String> properties = properties(savedCoach.properties());
        return new FootballCoach(
                savedCoach.id(),
                savedCoach.name(),
                savedCoach.role(),
                required(properties, "specialization"),
                intValue(properties, "coachingRating")
        );
    }

    private FootballMatch restorePlayedMatch(SavePlayedMatchData savedMatch, FootballTeam homeTeam, FootballTeam awayTeam) {
        FootballMatch match = new FootballMatch(homeTeam, awayTeam);
        match.setHomeSetup(restoreLineup(savedMatch.homeLineup(), playersById(homeTeam)), restoreTactic(savedMatch.homeTactic()));
        match.setAwaySetup(restoreLineup(savedMatch.awayLineup(), playersById(awayTeam)), restoreTactic(savedMatch.awayTactic()));
        match.setResult(savedMatch.homeScore(), savedMatch.awayScore());
        return match;
    }

    private FootballLineup restoreLineup(SaveLineupData savedLineup, Map<String, FootballPlayer> playersById) {
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
        Map<String, String> properties = properties(savedTactic.properties());
        return new FootballTactic(
                savedTactic.name(),
                required(properties, "formation"),
                enumValue(FootballTactic.Mentality.class, required(properties, "mentality")),
                intValue(properties, "pressingIntensity"),
                intValue(properties, "attackingWidth")
        );
    }

    private FootballTrainingPlan restoreTrainingPlan(SaveTrainingPlanData savedPlan) {
        Map<String, String> properties = properties(savedPlan.properties());
        return new FootballTrainingPlan(
                savedPlan.focus(),
                savedPlan.intensity(),
                intValue(properties, "conditioningLoad"),
                intValue(properties, "tacticalLoad"),
                booleanValue(properties, "recoveryIncluded")
        );
    }

    private Map<String, FootballPlayer> playersById(FootballTeam team) {
        Map<String, FootballPlayer> players = new LinkedHashMap<>();
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

    private ProgressionState progressionState(String value) {
        try {
            return ProgressionState.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported saved progression state: " + value, exception);
        }
    }

    private void requireSport(String sportId) {
        if (!"football".equals(sportId.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Football restorer cannot load sport: " + sportId);
        }
    }

    private Map<String, String> properties(List<SavePropertyValue> properties) {
        Map<String, String> values = new LinkedHashMap<>();
        for (SavePropertyValue property : properties) {
            values.put(property.key(), property.value());
        }
        return values;
    }

    private String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing saved football property: " + key);
        }
        return value;
    }

    private int intValue(Map<String, String> values, String key) {
        try {
            return Integer.parseInt(required(values, key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Saved football property must be an integer: " + key, exception);
        }
    }

    private boolean booleanValue(Map<String, String> values, String key) {
        String value = required(values, key);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("Saved football property must be true or false: " + key);
        }
        return Boolean.parseBoolean(value);
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumType, String value) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Saved football property has unsupported value: " + value,
                    exception
            );
        }
    }

    private record RestoredLeague(FootballLeague league, Map<String, FootballTeam> teamsById) {
    }
}
