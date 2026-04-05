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
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.FORWARD,
            FootballPosition.GOALKEEPER,
            FootballPosition.DEFENDER,
            FootballPosition.MIDFIELDER,
            FootballPosition.FORWARD
    };

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
        BootstrapFootballLeague league = new BootstrapFootballLeague(leagueName);
        List<String> teamNames = assetProvider.getTeamNames();
        if (teamNames == null || teamNames.size() < initialTeamCount) {
            throw new IllegalStateException("Asset provider does not supply enough team names.");
        }

        for (int i = 0; i < initialTeamCount; i++) {
            BootstrapFootballTeam team = new BootstrapFootballTeam(
                    "football-team-" + (i + 1),
                    teamNames.get(i)
            );

            populatePlayers(team, i);
            populateCoach(team, i);
            league.addTeam(team);
        }

        return league;
    }

    @Override
    public Season createSeason(League league) {
        return new BootstrapFootballSeason(league);
    }

    private void populatePlayers(BootstrapFootballTeam team, int teamIndex) {
        List<String> names = new ArrayList<>();
        names.addAll(assetProvider.getMaleNames());
        names.addAll(assetProvider.getFemaleNames());

        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply player names.");
        }

        for (int i = 0; i < DEFAULT_POSITION_PATTERN.length; i++) {
            String baseName = names.get((teamIndex * DEFAULT_POSITION_PATTERN.length + i) % names.size());
            String playerName = baseName + " " + (i + 1);
            FootballPosition position = DEFAULT_POSITION_PATTERN[i];
            FootballAttributeProfile profile = createAttributeProfile(position, teamIndex, i);

            team.addPlayer(new FootballPlayer(
                    "football-player-" + (teamIndex + 1) + "-" + (i + 1),
                    playerName,
                    position,
                    profile
            ));
        }
    }

    private void populateCoach(BootstrapFootballTeam team, int teamIndex) {
        List<String> names = new ArrayList<>();
        names.addAll(assetProvider.getMaleNames());
        names.addAll(assetProvider.getFemaleNames());

        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply coach names.");
        }

        String coachName = names.get(teamIndex % names.size()) + " Coach";

        team.addCoach(new FootballCoach(
                "football-coach-" + (teamIndex + 1),
                coachName,
                "Head Coach",
                chooseCoachSpecialization(teamIndex),
                70 + (teamIndex % 21)
        ));
    }

    private FootballAttributeProfile createAttributeProfile(FootballPosition position, int teamIndex, int playerIndex) {
        int variance = (teamIndex * 7 + playerIndex * 3) % 11;

        return switch (position) {
            case GOALKEEPER -> new FootballAttributeProfile(
                    38 + variance,
                    80 + variance,
                    68 + variance,
                    62 + variance,
                    55 + variance
            );
            case DEFENDER -> new FootballAttributeProfile(
                    52 + variance,
                    78 + variance,
                    74 + variance,
                    66 + variance,
                    63 + variance
            );
            case MIDFIELDER -> new FootballAttributeProfile(
                    69 + variance,
                    67 + variance,
                    76 + variance,
                    81 + variance,
                    72 + variance
            );
            case FORWARD -> new FootballAttributeProfile(
                    84 + variance,
                    48 + variance,
                    73 + variance,
                    71 + variance,
                    79 + variance
            );
        };
    }

    private String chooseCoachSpecialization(int teamIndex) {
        return switch (teamIndex % 4) {
            case 0 -> "General Management";
            case 1 -> "Attacking Play";
            case 2 -> "Defensive Organization";
            default -> "Fitness and Conditioning";
        };
    }
}
