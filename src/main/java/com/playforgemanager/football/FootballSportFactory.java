package com.playforgemanager.football;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FootballSportFactory implements SportFactory {
    private static final FootballPosition[] DEFAULT_POSITION_PATTERN = {
            FootballPosition.GOALKEEPER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.DEFENDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.GOALKEEPER,
            FootballPosition.GOALKEEPER
    };

    private static final int DEFAULT_SQUAD_SIZE = DEFAULT_POSITION_PATTERN.length;

    private final AssetProvider assetProvider;
    private final int initialTeamCount;

    public FootballSportFactory(AssetProvider assetProvider) {
        this(assetProvider, 4);
    }

    public FootballSportFactory(AssetProvider assetProvider, int initialTeamCount) {
        this.assetProvider = Objects.requireNonNull(assetProvider, "Asset provider cannot be null.");
        if (initialTeamCount < 2) {
            throw new IllegalArgumentException("Initial team count must be at least 2.");
        }
        this.initialTeamCount = initialTeamCount;
    }

    @Override
    public String getSportName() {
        return "Football";
    }

    @Override
    public Sport createSport() {
        return new FootballSport();
    }

    @Override
    public League createLeague(String leagueName) {
        FootballLeague league = new FootballLeague(Objects.requireNonNull(leagueName, "League name cannot be null."));
        List<String> teamNames = collectNormalizedValues(assetProvider.getTeamNames(), "Asset provider must supply team names.");
        if (teamNames.size() < initialTeamCount) {
            throw new IllegalStateException("Asset provider does not supply enough team names.");
        }

        FootballRuleset ruleset = new FootballRuleset();

        for (int i = 0; i < initialTeamCount; i++) {
            FootballTeam team = new FootballTeam("football-team-" + (i + 1), teamNames.get(i));
            populatePlayers(team, i);
            populateCoach(team, i);
            configureMatchPreparation(team, i, ruleset);
            league.addTeam(team);
        }

        return league;
    }

    @Override
    public Season createSeason(League league) {
        return new FootballSeason(league);
    }

    private void populatePlayers(FootballTeam team, int teamIndex) {
        List<String> names = collectPeopleNames();
        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply player names.");
        }

        for (int i = 0; i < DEFAULT_SQUAD_SIZE; i++) {
            FootballPosition position = DEFAULT_POSITION_PATTERN[i];
            String baseName = names.get((teamIndex * DEFAULT_SQUAD_SIZE + i) % names.size());
            FootballPlayer player = new FootballPlayer(
                    "football-player-" + (teamIndex + 1) + "-" + (i + 1),
                    baseName + " " + (i + 1),
                    position,
                    createAttributeProfile(position, teamIndex, i)
            );
            team.addPlayer(player);
        }
    }

    private void populateCoach(FootballTeam team, int teamIndex) {
        List<String> names = collectPeopleNames();
        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply coach names.");
        }

        String coachName = names.get(teamIndex % names.size()) + " Coach";
        FootballCoach coach = new FootballCoach(
                "football-coach-" + (teamIndex + 1),
                coachName,
                "Head Coach",
                chooseCoachSpecialization(teamIndex),
                70 + (teamIndex % 21)
        );
        team.addCoach(coach);
    }

    private void configureMatchPreparation(FootballTeam team, int teamIndex, FootballRuleset ruleset) {
        team.assignTactic(createDefaultTactic(teamIndex));
        team.assignTrainingPlan(new FootballTrainingPlan("Balanced Development", 60, 58, 52, true));
        team.assignLineup(ruleset.buildLineup(team.getFootballPlayers()), ruleset);
    }

    private FootballTactic createDefaultTactic(int teamIndex) {
        return switch (teamIndex % 4) {
            case 0 -> new FootballTactic(
                    "Balanced Control",
                    "4-2-3-1",
                    FootballTactic.Mentality.BALANCED,
                    58,
                    60
            );
            case 1 -> new FootballTactic(
                    "High Press",
                    "4-3-3",
                    FootballTactic.Mentality.ATTACKING,
                    74,
                    68
            );
            case 2 -> new FootballTactic(
                    "Compact Counter",
                    "4-4-2",
                    FootballTactic.Mentality.DEFENSIVE,
                    50,
                    45
            );
            default -> new FootballTactic(
                    "Possession Shape",
                    "4-3-3",
                    FootballTactic.Mentality.BALANCED,
                    54,
                    72
            );
        };
    }

    private FootballAttributeProfile createAttributeProfile(FootballPosition position, int teamIndex, int playerIndex) {
        int variance = (teamIndex * 7 + playerIndex * 3) % 11;

        return switch (position) {
            case GOALKEEPER -> new FootballAttributeProfile(
                    clamp(38 + variance),
                    clamp(80 + variance),
                    clamp(68 + variance),
                    clamp(62 + variance),
                    clamp(55 + variance)
            );
            case DEFENDER -> new FootballAttributeProfile(
                    clamp(52 + variance),
                    clamp(78 + variance),
                    clamp(74 + variance),
                    clamp(66 + variance),
                    clamp(63 + variance)
            );
            case MIDFIELDER -> new FootballAttributeProfile(
                    clamp(69 + variance),
                    clamp(67 + variance),
                    clamp(76 + variance),
                    clamp(81 + variance),
                    clamp(72 + variance)
            );
            case FORWARD -> new FootballAttributeProfile(
                    clamp(84 + variance),
                    clamp(48 + variance),
                    clamp(73 + variance),
                    clamp(71 + variance),
                    clamp(79 + variance)
            );
        };
    }

    private List<String> collectPeopleNames() {
        List<String> names = new ArrayList<>();
        names.addAll(collectNormalizedValues(assetProvider.getMaleNames(), null));
        names.addAll(collectNormalizedValues(assetProvider.getFemaleNames(), null));
        return names;
    }

    private List<String> collectNormalizedValues(List<?> source, String emptyMessage) {
        List<String> values = new ArrayList<>();
        if (source != null) {
            for (Object entry : source) {
                if (entry != null) {
                    String value = entry.toString().trim();
                    if (!value.isEmpty()) {
                        values.add(value);
                    }
                }
            }
        }

        if (values.isEmpty() && emptyMessage != null) {
            throw new IllegalStateException(emptyMessage);
        }

        return values;
    }

    private String chooseCoachSpecialization(int teamIndex) {
        return switch (teamIndex % 4) {
            case 0 -> "General Management";
            case 1 -> "Attacking Play";
            case 2 -> "Defensive Organization";
            default -> "Fitness and Conditioning";
        };
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
