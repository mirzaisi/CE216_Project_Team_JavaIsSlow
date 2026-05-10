package com.playforgemanager.handball;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HandballSportFactory implements SportFactory {
    private static final HandballPosition[] DEFAULT_POSITION_PATTERN = {
            HandballPosition.GOALKEEPER,
            HandballPosition.LEFT_WING,
            HandballPosition.RIGHT_WING,
            HandballPosition.LEFT_BACK,
            HandballPosition.CENTER_BACK,
            HandballPosition.RIGHT_BACK,
            HandballPosition.PIVOT,
            HandballPosition.GOALKEEPER,
            HandballPosition.LEFT_WING,
            HandballPosition.RIGHT_WING,
            HandballPosition.LEFT_BACK,
            HandballPosition.CENTER_BACK,
            HandballPosition.RIGHT_BACK,
            HandballPosition.PIVOT
    };

    private static final int DEFAULT_SQUAD_SIZE = DEFAULT_POSITION_PATTERN.length;

    private final AssetProvider assetProvider;
    private final int initialTeamCount;

    public HandballSportFactory(AssetProvider assetProvider, int initialTeamCount) {
        this.assetProvider = Objects.requireNonNull(assetProvider, "Asset provider cannot be null.");

        if (initialTeamCount < 2) {
            throw new IllegalArgumentException("Initial team count must be at least 2.");
        }

        this.initialTeamCount = initialTeamCount;
    }

    @Override
    public String getSportName() {
        return "Handball";
    }

    @Override
    public Sport createSport() {
        return new HandballSport();
    }

    @Override
    public League createLeague(String leagueName) {
        HandballLeague league = new HandballLeague(
                Objects.requireNonNull(leagueName, "League name cannot be null.")
        );

        List<String> teamNames = collectNormalizedValues(
                assetProvider.getTeamNames(),
                "Asset provider must supply team names."
        );

        if (teamNames.size() < initialTeamCount) {
            throw new IllegalStateException("Asset provider does not supply enough team names.");
        }

        HandballRuleset ruleset = new HandballRuleset();

        // Creates the initial handball teams and prepares each one for matches.
        for (int i = 0; i < initialTeamCount; i++) {
            HandballTeam team = new HandballTeam("handball-team-" + (i + 1), teamNames.get(i));

            populatePlayers(team, i);
            populateCoach(team, i);
            configureMatchPreparation(team, i, ruleset);

            league.addTeam(team);
        }

        return league;
    }

    @Override
    public Season createSeason(League league) {
        return new HandballSeason(league);
    }

    private void populatePlayers(HandballTeam team, int teamIndex) {
        List<String> names = collectPeopleNames();

        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply player names.");
        }

        // Creates a default squad using the fixed handball position pattern.
        for (int i = 0; i < DEFAULT_SQUAD_SIZE; i++) {
            HandballPosition position = DEFAULT_POSITION_PATTERN[i];
            String baseName = names.get((teamIndex * DEFAULT_SQUAD_SIZE + i) % names.size());

            HandballPlayer player = new HandballPlayer(
                    "handball-player-" + (teamIndex + 1) + "-" + (i + 1),
                    baseName + " " + (i + 1),
                    position,
                    createAttributeProfile(position, teamIndex, i)
            );

            team.addPlayer(player);
        }
    }

    private void populateCoach(HandballTeam team, int teamIndex) {
        List<String> names = collectPeopleNames();

        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply coach names.");
        }

        String coachName = names.get(teamIndex % names.size()) + " Coach";

        HandballCoach coach = new HandballCoach(
                "handball-coach-" + (teamIndex + 1),
                coachName,
                "Head Coach",
                chooseCoachSpecialization(teamIndex),
                70 + (teamIndex % 21)
        );

        team.addCoach(coach);
    }

    private void configureMatchPreparation(HandballTeam team, int teamIndex, HandballRuleset ruleset) {
        // Assigns default tactic, training plan, and valid lineup for the generated team.
        team.assignTactic(createDefaultTactic(teamIndex));
        team.assignTrainingPlan(new HandballTrainingPlan("Transition Balance", 62, 58, 55, true));
        team.assignLineup(ruleset.buildLineup(team.getHandballPlayers()), ruleset);
    }

    private HandballTactic createDefaultTactic(int teamIndex) {
        return switch (teamIndex % 4) {
            case 0 -> new HandballTactic(
                    "Balanced Build-Up",
                    "3-3",
                    HandballTactic.Tempo.BALANCED,
                    58,
                    60
            );
            case 1 -> new HandballTactic(
                    "Fast Break",
                    "3-3",
                    HandballTactic.Tempo.FAST_BREAK,
                    68,
                    82
            );
            case 2 -> new HandballTactic(
                    "Compact Six-Zero",
                    "6-0",
                    HandballTactic.Tempo.CONTROLLED,
                    64,
                    42
            );
            default -> new HandballTactic(
                    "Balanced Pressure",
                    "5-1",
                    HandballTactic.Tempo.BALANCED,
                    72,
                    56
            );
        };
    }

    private HandballAttributeProfile createAttributeProfile(
            HandballPosition position,
            int teamIndex,
            int playerIndex
    ) {
        int variance = (teamIndex * 7 + playerIndex * 3) % 11;

        // Builds position-based handball attributes with small deterministic variance.
        return switch (position) {
            case GOALKEEPER -> new HandballAttributeProfile(
                    clamp(32 + variance),
                    clamp(74 + variance),
                    clamp(58 + variance),
                    clamp(55 + variance),
                    clamp(84 + variance)
            );
            case LEFT_WING, RIGHT_WING -> new HandballAttributeProfile(
                    clamp(78 + variance),
                    clamp(58 + variance),
                    clamp(68 + variance),
                    clamp(86 + variance),
                    clamp(44 + variance)
            );
            case LEFT_BACK, RIGHT_BACK -> new HandballAttributeProfile(
                    clamp(82 + variance),
                    clamp(66 + variance),
                    clamp(70 + variance),
                    clamp(73 + variance),
                    clamp(42 + variance)
            );
            case CENTER_BACK -> new HandballAttributeProfile(
                    clamp(74 + variance),
                    clamp(64 + variance),
                    clamp(84 + variance),
                    clamp(72 + variance),
                    clamp(48 + variance)
            );
            case PIVOT -> new HandballAttributeProfile(
                    clamp(80 + variance),
                    clamp(76 + variance),
                    clamp(61 + variance),
                    clamp(60 + variance),
                    clamp(38 + variance)
            );
        };
    }

    private HandballCoachSpecialization chooseCoachSpecialization(int teamIndex) {
        return switch (teamIndex % 4) {
            case 0 -> HandballCoachSpecialization.ATTACK;
            case 1 -> HandballCoachSpecialization.DEFENSE;
            case 2 -> HandballCoachSpecialization.GOALKEEPING;
            default -> HandballCoachSpecialization.FITNESS;
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

        // Converts asset entries into trimmed non-empty strings.
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

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
