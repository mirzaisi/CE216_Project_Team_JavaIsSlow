package com.playforgemanager.infrastructure;

import com.playforgemanager.application.save.PersistenceMode;
import com.playforgemanager.application.save.SaveCoachData;
import com.playforgemanager.application.save.SaveFieldPolicy;
import com.playforgemanager.application.save.SaveFixtureData;
import com.playforgemanager.application.save.SaveGameDocument;
import com.playforgemanager.application.save.SaveGameReader;
import com.playforgemanager.application.save.SaveLineupData;
import com.playforgemanager.application.save.SavePlayedMatchData;
import com.playforgemanager.application.save.SavePlayerData;
import com.playforgemanager.application.save.SavePropertyValue;
import com.playforgemanager.application.save.SaveSeasonData;
import com.playforgemanager.application.save.SaveSessionData;
import com.playforgemanager.application.save.SaveTacticData;
import com.playforgemanager.application.save.SaveTeamData;
import com.playforgemanager.application.save.SaveTrainingPlanData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonSaveGameReader implements SaveGameReader {
    @Override
    public SaveGameDocument read(Path savePath) throws IOException {
        Objects.requireNonNull(savePath, "Save path cannot be null.");
        Object parsed = new Parser(Files.readString(savePath)).parse();
        return toDocument(asObject(parsed, "save root"));
    }

    private SaveGameDocument toDocument(Map<String, Object> root) {
        return new SaveGameDocument(
                requiredString(root, "formatId"),
                requiredInt(root, "formatVersion"),
                toSessionData(requiredObject(root, "session"))
        );
    }

    private SaveSessionData toSessionData(Map<String, Object> session) {
        return new SaveSessionData(
                requiredString(session, "sportId"),
                requiredString(session, "progressionState"),
                requiredString(session, "controlledTeamId"),
                toSeasonData(requiredObject(session, "season")),
                requiredObjects(session, "fieldPolicies").stream()
                        .map(this::toFieldPolicy)
                        .toList()
        );
    }

    private SaveSeasonData toSeasonData(Map<String, Object> season) {
        return new SaveSeasonData(
                requiredString(season, "leagueName"),
                requiredInt(season, "currentWeek"),
                requiredBoolean(season, "completed"),
                requiredObjects(season, "teams").stream()
                        .map(this::toTeamData)
                        .toList(),
                requiredObjects(season, "fixtures").stream()
                        .map(this::toFixtureData)
                        .toList()
        );
    }

    private SaveTeamData toTeamData(Map<String, Object> team) {
        return new SaveTeamData(
                requiredString(team, "id"),
                requiredString(team, "name"),
                requiredObjects(team, "coaches").stream()
                        .map(this::toCoachData)
                        .toList(),
                requiredObjects(team, "players").stream()
                        .map(this::toPlayerData)
                        .toList(),
                optionalLineup(team, "selectedLineup"),
                optionalTactic(team, "selectedTactic"),
                optionalTrainingPlan(team, "selectedTrainingPlan"),
                requiredObjects(team, "properties").stream()
                        .map(this::toPropertyValue)
                        .toList()
        );
    }

    private SaveCoachData toCoachData(Map<String, Object> coach) {
        return new SaveCoachData(
                requiredString(coach, "id"),
                requiredString(coach, "name"),
                requiredString(coach, "role"),
                requiredObjects(coach, "properties").stream()
                        .map(this::toPropertyValue)
                        .toList()
        );
    }

    private SavePlayerData toPlayerData(Map<String, Object> player) {
        return new SavePlayerData(
                requiredString(player, "id"),
                requiredString(player, "name"),
                requiredBoolean(player, "available"),
                requiredInt(player, "injuryMatchesRemaining"),
                requiredObjects(player, "properties").stream()
                        .map(this::toPropertyValue)
                        .toList()
        );
    }

    private SaveFixtureData toFixtureData(Map<String, Object> fixture) {
        return new SaveFixtureData(
                requiredInt(fixture, "week"),
                requiredString(fixture, "homeTeamId"),
                requiredString(fixture, "awayTeamId"),
                optionalPlayedMatch(fixture, "playedMatch")
        );
    }

    private SavePlayedMatchData optionalPlayedMatch(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        Map<String, Object> match = asObject(value, key);
        return new SavePlayedMatchData(
                requiredInt(match, "homeScore"),
                requiredInt(match, "awayScore"),
                toLineupData(requiredObject(match, "homeLineup")),
                toLineupData(requiredObject(match, "awayLineup")),
                toTacticData(requiredObject(match, "homeTactic")),
                toTacticData(requiredObject(match, "awayTactic"))
        );
    }

    private SaveLineupData optionalLineup(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : toLineupData(asObject(value, key));
    }

    private SaveLineupData toLineupData(Map<String, Object> lineup) {
        return new SaveLineupData(
                requiredStrings(lineup, "selectedPlayerIds"),
                requiredStrings(lineup, "reservePlayerIds")
        );
    }

    private SaveTacticData optionalTactic(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : toTacticData(asObject(value, key));
    }

    private SaveTacticData toTacticData(Map<String, Object> tactic) {
        return new SaveTacticData(
                requiredString(tactic, "name"),
                requiredObjects(tactic, "properties").stream()
                        .map(this::toPropertyValue)
                        .toList()
        );
    }

    private SaveTrainingPlanData optionalTrainingPlan(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        Map<String, Object> plan = asObject(value, key);
        return new SaveTrainingPlanData(
                requiredString(plan, "focus"),
                requiredInt(plan, "intensity"),
                requiredObjects(plan, "properties").stream()
                        .map(this::toPropertyValue)
                        .toList()
        );
    }

    private SavePropertyValue toPropertyValue(Map<String, Object> property) {
        return new SavePropertyValue(
                requiredString(property, "key"),
                requiredString(property, "valueType"),
                requiredString(property, "value")
        );
    }

    private SaveFieldPolicy toFieldPolicy(Map<String, Object> policy) {
        return new SaveFieldPolicy(
                requiredString(policy, "fieldPath"),
                PersistenceMode.valueOf(requiredString(policy, "mode")),
                requiredString(policy, "reason")
        );
    }

    private Map<String, Object> requiredObject(Map<String, Object> source, String key) {
        return asObject(requiredValue(source, key), key);
    }

    private List<Map<String, Object>> requiredObjects(Map<String, Object> source, String key) {
        List<?> values = asList(requiredValue(source, key), key);
        List<Map<String, Object>> objects = new ArrayList<>(values.size());
        for (Object value : values) {
            objects.add(asObject(value, key));
        }
        return List.copyOf(objects);
    }

    private List<String> requiredStrings(Map<String, Object> source, String key) {
        List<?> values = asList(requiredValue(source, key), key);
        List<String> strings = new ArrayList<>(values.size());
        for (Object value : values) {
            if (!(value instanceof String text)) {
                throw new IllegalArgumentException("Save field must contain only text values: " + key);
            }
            strings.add(text);
        }
        return List.copyOf(strings);
    }

    private String requiredString(Map<String, Object> source, String key) {
        Object value = requiredValue(source, key);
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Save field must be text: " + key);
        }
        return text;
    }

    private int requiredInt(Map<String, Object> source, String key) {
        Object value = requiredValue(source, key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Save field must be a number: " + key);
        }
        double doubleValue = number.doubleValue();
        int intValue = number.intValue();
        if (Double.compare(doubleValue, intValue) != 0) {
            throw new IllegalArgumentException("Save field must be a whole number: " + key);
        }
        return intValue;
    }

    private boolean requiredBoolean(Map<String, Object> source, String key) {
        Object value = requiredValue(source, key);
        if (!(value instanceof Boolean booleanValue)) {
            throw new IllegalArgumentException("Save field must be true or false: " + key);
        }
        return booleanValue;
    }

    private Object requiredValue(Map<String, Object> source, String key) {
        if (!source.containsKey(key) || source.get(key) == null) {
            throw new IllegalArgumentException("Missing save field: " + key);
        }
        return source.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asObject(Object value, String label) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Save field must be an object: " + label);
        }
        return (Map<String, Object>) map;
    }

    private List<?> asList(Object value, String label) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Save field must be a list: " + label);
        }
        return list;
    }

    private static final class Parser {
        private final String json;
        private int index;

        private Parser(String json) {
            this.json = Objects.requireNonNull(json, "JSON text cannot be null.");
        }

        private Object parse() {
            Object value = parseValue();
            skipWhitespace();
            if (!isAtEnd()) {
                throw error("Unexpected text after JSON document.");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (isAtEnd()) {
                throw error("Unexpected end of JSON.");
            }

            char current = peek();
            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> readKeyword("true", Boolean.TRUE);
                case 'f' -> readKeyword("false", Boolean.FALSE);
                case 'n' -> readKeyword("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (match('}')) {
                return object;
            }

            do {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
            } while (match(','));

            expect('}');
            return object;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();
            if (match(']')) {
                return values;
            }

            do {
                values.add(parseValue());
                skipWhitespace();
            } while (match(','));

            expect(']');
            return List.copyOf(values);
        }

        private String parseString() {
            expect('"');
            StringBuilder text = new StringBuilder();

            while (!isAtEnd()) {
                char current = advance();
                if (current == '"') {
                    return text.toString();
                }
                if (current != '\\') {
                    text.append(current);
                    continue;
                }

                if (isAtEnd()) {
                    throw error("Unfinished string escape.");
                }
                char escaped = advance();
                switch (escaped) {
                    case '"' -> text.append('"');
                    case '\\' -> text.append('\\');
                    case '/' -> text.append('/');
                    case 'b' -> text.append('\b');
                    case 'f' -> text.append('\f');
                    case 'n' -> text.append('\n');
                    case 'r' -> text.append('\r');
                    case 't' -> text.append('\t');
                    case 'u' -> text.append(readUnicodeEscape());
                    default -> throw error("Unsupported string escape.");
                }
            }

            throw error("Unfinished string.");
        }

        private char readUnicodeEscape() {
            if (index + 4 > json.length()) {
                throw error("Unfinished unicode escape.");
            }
            String hex = json.substring(index, index + 4);
            index += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException exception) {
                throw error("Invalid unicode escape.");
            }
        }

        private Object parseNumber() {
            int start = index;
            if (match('-')) {
                if (isAtEnd() || !Character.isDigit(peek())) {
                    throw error("Invalid number.");
                }
            }

            readDigits();
            boolean decimal = false;
            if (match('.')) {
                decimal = true;
                readDigits();
            }
            if (match('e') || match('E')) {
                decimal = true;
                if (match('+') || match('-')) {
                }
                readDigits();
            }

            String numberText = json.substring(start, index);
            try {
                return decimal ? Double.parseDouble(numberText) : Long.parseLong(numberText);
            } catch (NumberFormatException exception) {
                throw error("Invalid number.");
            }
        }

        private void readDigits() {
            int start = index;
            while (!isAtEnd() && Character.isDigit(peek())) {
                index++;
            }
            if (start == index) {
                throw error("Expected digit.");
            }
        }

        private Object readKeyword(String keyword, Object value) {
            if (!json.startsWith(keyword, index)) {
                throw error("Unexpected JSON token.");
            }
            index += keyword.length();
            return value;
        }

        private void expect(char expected) {
            skipWhitespace();
            if (isAtEnd() || advance() != expected) {
                throw error("Expected '" + expected + "'.");
            }
        }

        private boolean match(char expected) {
            if (isAtEnd() || peek() != expected) {
                return false;
            }
            index++;
            return true;
        }

        private char advance() {
            return json.charAt(index++);
        }

        private char peek() {
            return json.charAt(index);
        }

        private void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(peek())) {
                index++;
            }
        }

        private boolean isAtEnd() {
            return index >= json.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " At character " + index + ".");
        }
    }
}
