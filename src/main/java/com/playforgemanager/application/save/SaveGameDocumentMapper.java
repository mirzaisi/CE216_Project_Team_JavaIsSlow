package com.playforgemanager.application.save;

import com.playforgemanager.core.Coach;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;
import com.playforgemanager.core.TrainingPlan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SaveGameDocumentMapper {
    private static final Set<String> TEAM_EXCLUDED_PROPERTIES = Set.of(
            "id",
            "name",
            "roster",
            "selectedLineup",
            "selectedTactic",
            "trainingPlan",
            "coaches",
            "footballPlayers",
            "availablePlayers",
            "selectedFootballLineup",
            "selectedFootballTactic",
            "selectedFootballTrainingPlan"
    );
    private static final Set<String> PLAYER_EXCLUDED_PROPERTIES = Set.of(
            "id",
            "name",
            "available",
            "injuryMatchesRemaining"
    );
    private static final Set<String> COACH_EXCLUDED_PROPERTIES = Set.of("id", "name", "role");
    private static final Set<String> TACTIC_EXCLUDED_PROPERTIES = Set.of("name");
    private static final Set<String> TRAINING_EXCLUDED_PROPERTIES = Set.of("focus", "intensity");
    private static final List<String> BENCH_METHOD_CANDIDATES = List.of("getBenchPlayers", "getReservePlayers", "getSubstitutes");

    public SaveGameDocument toDocument(GameSession session) {
        Objects.requireNonNull(session, "Game session cannot be null.");
        validateSession(session);

        return new SaveGameDocument(
                SaveGameFormat.FORMAT_ID,
                SaveGameFormat.CURRENT_VERSION,
                new SaveSessionData(
                        session.getSelectedSportId(),
                        session.getProgressionState().name(),
                        session.getControlledTeam().getId(),
                        toSeasonData(session.getCurrentSeason()),
                        SaveGameFormat.fieldPolicies()
                )
        );
    }

    private void validateSession(GameSession session) {
        Season season = Objects.requireNonNull(session.getCurrentSeason(), "Current season cannot be null.");
        League league = Objects.requireNonNull(season.getLeague(), "League cannot be null.");
        List<Team> teams = league.getTeams();

        if (teams.isEmpty()) {
            throw new IllegalStateException("Cannot save a session without league teams.");
        }
        if (!teams.contains(session.getControlledTeam())) {
            throw new IllegalStateException("Controlled team must belong to the current league.");
        }

        Set<String> teamIds = new HashSet<>();
        for (Team team : teams) {
            if (!teamIds.add(team.getId())) {
                throw new IllegalStateException("Duplicate team id in league: " + team.getId());
            }
            validateRoster(team);
        }

        for (Fixture fixture : league.getFixtures()) {
            if (!teams.contains(fixture.getHomeTeam()) || !teams.contains(fixture.getAwayTeam())) {
                throw new IllegalStateException("Fixture teams must belong to the current league.");
            }
            Match playedMatch = fixture.getPlayedMatch();
            if (playedMatch != null) {
                validatePlayedMatch(fixture, playedMatch);
            }
        }
    }

    private void validateRoster(Team team) {
        Set<String> playerIds = new HashSet<>();
        for (Player player : team.getRoster()) {
            if (!playerIds.add(player.getId())) {
                throw new IllegalStateException("Duplicate player id in team " + team.getId() + ": " + player.getId());
            }
        }
    }

    private void validatePlayedMatch(Fixture fixture, Match match) {
        if (match.getHomeTeam() != fixture.getHomeTeam() || match.getAwayTeam() != fixture.getAwayTeam()) {
            throw new IllegalStateException("Played match teams must match their fixture.");
        }
        if (!match.isPlayed()) {
            throw new IllegalStateException("Attached match must be marked as played.");
        }
        if (match.getHomeLineup() == null || match.getAwayLineup() == null) {
            throw new IllegalStateException("Played matches must include both lineups before saving.");
        }
        if (match.getHomeTactic() == null || match.getAwayTactic() == null) {
            throw new IllegalStateException("Played matches must include both tactics before saving.");
        }
    }

    private SaveSeasonData toSeasonData(Season season) {
        League league = season.getLeague();
        return new SaveSeasonData(
                league.getName(),
                season.getCurrentWeek(),
                season.isCompleted(),
                league.getTeams().stream().map(this::toTeamData).toList(),
                league.getFixtures().stream().map(this::toFixtureData).toList()
        );
    }

    private SaveTeamData toTeamData(Team team) {
        return new SaveTeamData(
                team.getId(),
                team.getName(),
                extractCoaches(team),
                team.getRoster().stream().map(this::toPlayerData).toList(),
                toLineupDataOrNull(team.getSelectedLineup()),
                toTacticDataOrNull(team.getSelectedTactic()),
                toTrainingPlanDataOrNull(team.getTrainingPlan()),
                extractProperties(team, TEAM_EXCLUDED_PROPERTIES)
        );
    }

    private SaveCoachData toCoachData(Coach coach) {
        return new SaveCoachData(
                coach.getId(),
                coach.getName(),
                coach.getRole(),
                extractProperties(coach, COACH_EXCLUDED_PROPERTIES)
        );
    }

    private SavePlayerData toPlayerData(Player player) {
        return new SavePlayerData(
                player.getId(),
                player.getName(),
                player.isAvailable(),
                player.getInjuryMatchesRemaining(),
                extractProperties(player, PLAYER_EXCLUDED_PROPERTIES)
        );
    }

    private SaveFixtureData toFixtureData(Fixture fixture) {
        return new SaveFixtureData(
                fixture.getWeek(),
                fixture.getHomeTeam().getId(),
                fixture.getAwayTeam().getId(),
                toPlayedMatchDataOrNull(fixture.getPlayedMatch())
        );
    }

    private SavePlayedMatchData toPlayedMatchDataOrNull(Match match) {
        if (match == null) {
            return null;
        }
        return new SavePlayedMatchData(
                match.getHomeScore(),
                match.getAwayScore(),
                toLineupData(match.getHomeLineup()),
                toLineupData(match.getAwayLineup()),
                toTacticData(match.getHomeTactic()),
                toTacticData(match.getAwayTactic())
        );
    }

    private SaveLineupData toLineupDataOrNull(Lineup lineup) {
        return lineup == null ? null : toLineupData(lineup);
    }

    private SaveLineupData toLineupData(Lineup lineup) {
        Objects.requireNonNull(lineup, "Lineup cannot be null.");
        List<String> selectedPlayerIds = lineup.getSelectedPlayers().stream()
                .map(Player::getId)
                .toList();
        List<String> reservePlayerIds = reservePlayers(lineup).stream()
                .map(Player::getId)
                .filter(playerId -> !selectedPlayerIds.contains(playerId))
                .toList();
        return new SaveLineupData(selectedPlayerIds, reservePlayerIds);
    }

    private SaveTacticData toTacticDataOrNull(Tactic tactic) {
        return tactic == null ? null : toTacticData(tactic);
    }

    private SaveTacticData toTacticData(Tactic tactic) {
        Objects.requireNonNull(tactic, "Tactic cannot be null.");
        return new SaveTacticData(tactic.getName(), extractProperties(tactic, TACTIC_EXCLUDED_PROPERTIES));
    }

    private SaveTrainingPlanData toTrainingPlanDataOrNull(TrainingPlan trainingPlan) {
        if (trainingPlan == null) {
            return null;
        }
        return new SaveTrainingPlanData(
                trainingPlan.getFocus(),
                trainingPlan.getIntensity(),
                extractProperties(trainingPlan, TRAINING_EXCLUDED_PROPERTIES)
        );
    }

    private List<SaveCoachData> extractCoaches(Team team) {
        try {
            Method method = team.getClass().getMethod("getCoaches");
            Object value = method.invoke(team);
            if (!(value instanceof List<?> coaches)) {
                return List.of();
            }
            List<SaveCoachData> savedCoaches = new ArrayList<>();
            for (Object coach : coaches) {
                if (coach instanceof Coach typedCoach) {
                    savedCoaches.add(toCoachData(typedCoach));
                }
            }
            return List.copyOf(savedCoaches);
        } catch (NoSuchMethodException exception) {
            return List.of();
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not read coaches for team: " + team.getId(), exception);
        }
    }

    private List<Player> reservePlayers(Lineup lineup) {
        for (String methodName : BENCH_METHOD_CANDIDATES) {
            try {
                Method method = lineup.getClass().getMethod(methodName);
                Object value = method.invoke(lineup);
                if (value instanceof List<?> players) {
                    List<Player> result = new ArrayList<>();
                    for (Object player : players) {
                        if (player instanceof Player typedPlayer) {
                            result.add(typedPlayer);
                        }
                    }
                    return List.copyOf(result);
                }
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalStateException("Could not read reserve players from lineup.", exception);
            }
        }
        return List.of();
    }

    private List<SavePropertyValue> extractProperties(Object source, Set<String> excludedProperties) {
        List<SavePropertyValue> values = new ArrayList<>();
        collectProperties(source, "", excludedProperties, values, newIdentitySet(), 0);
        return values.stream()
                .sorted(Comparator.comparing(SavePropertyValue::key))
                .toList();
    }

    private void collectProperties(
            Object source,
            String prefix,
            Set<String> excludedProperties,
            List<SavePropertyValue> values,
            Set<Object> visited,
            int depth
    ) {
        if (source == null || depth > 2 || !visited.add(source)) {
            return;
        }

        for (Method method : source.getClass().getMethods()) {
            if (!isReadableProperty(method)) {
                continue;
            }

            String propertyName = propertyName(method);
            if (propertyName == null || excludedProperties.contains(propertyName)) {
                continue;
            }

            Object rawValue = invokeProperty(source, method);
            if (rawValue == null || rawValue instanceof List<?> || rawValue instanceof Map<?, ?>) {
                continue;
            }

            String key = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
            SavePropertyValue propertyValue = toPropertyValue(key, rawValue);
            if (propertyValue != null) {
                values.add(propertyValue);
            } else if (!isJdkType(rawValue.getClass())) {
                collectProperties(rawValue, key, Set.of(), values, visited, depth + 1);
            }
        }
    }

    private boolean isReadableProperty(Method method) {
        return Modifier.isPublic(method.getModifiers())
                && method.getParameterCount() == 0
                && method.getDeclaringClass() != Object.class
                && propertyName(method) != null;
    }

    private String propertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3 && method.getReturnType() != Void.TYPE) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    private Object invokeProperty(Object source, Method method) {
        try {
            return method.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not read save property: " + method.getName(), exception);
        }
    }

    private SavePropertyValue toPropertyValue(String key, Object value) {
        if (value instanceof String text) {
            return new SavePropertyValue(key, "string", text);
        }
        if (value instanceof Enum<?> enumValue) {
            return new SavePropertyValue(key, "string", enumValue.name());
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return new SavePropertyValue(key, "integer", String.valueOf(value));
        }
        if (value instanceof Float || value instanceof Double) {
            return new SavePropertyValue(key, "decimal", String.format(Locale.ROOT, "%s", value));
        }
        if (value instanceof Boolean) {
            return new SavePropertyValue(key, "boolean", String.valueOf(value));
        }
        return null;
    }

    private boolean isJdkType(Class<?> type) {
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

    private String decapitalize(String text) {
        if (text.length() == 1) {
            return text.toLowerCase(Locale.ROOT);
        }
        return text.substring(0, 1).toLowerCase(Locale.ROOT) + text.substring(1);
    }

    private Set<Object> newIdentitySet() {
        return java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    }
}
