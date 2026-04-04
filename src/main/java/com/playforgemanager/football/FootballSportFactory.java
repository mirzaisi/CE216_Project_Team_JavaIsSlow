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
        FootballLeague league = new FootballLeague(leagueName);

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
        return new FootballSeason(league);
    }

    private void populatePlayers(BootstrapFootballTeam team, int teamIndex) {
        List<String> names = new ArrayList<>();
        names.addAll(assetProvider.getMaleNames());
        names.addAll(assetProvider.getFemaleNames());

        if (names.isEmpty()) {
            throw new IllegalStateException("Asset provider must supply player names.");
        }

        for (int i = 0; i < 18; i++) {
            String baseName = names.get((teamIndex * 18 + i) % names.size());
            String playerName = baseName + " " + (i + 1);

            team.addPlayer(new BootstrapFootballPlayer(
                    "football-player-" + (teamIndex + 1) + "-" + (i + 1),
                    playerName
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

        team.addCoach(new BootstrapFootballCoach(
                "football-coach-" + (teamIndex + 1),
                coachName,
                "Head Coach"
        ));
    }
}
