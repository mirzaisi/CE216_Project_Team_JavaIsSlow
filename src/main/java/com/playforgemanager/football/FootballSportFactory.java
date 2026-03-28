package com.playforgemanager.football;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.SportFactory;

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
        BootstrapFootballLeague league = new BootstrapFootballLeague(leagueName);

        List<String> teamNames = assetProvider.getTeamNames();
        if (teamNames == null || teamNames.size() < initialTeamCount) {
            throw new IllegalStateException("Asset provider does not supply enough team names.");
        }

        for (int i = 0; i < initialTeamCount; i++) {
            String teamName = teamNames.get(i);

            if (teamName == null || teamName.isBlank()) {
                throw new IllegalStateException("Team name cannot be blank.");
            }

            league.addTeam(new BootstrapFootballTeam(
                    "football-team-" + (i + 1),
                    teamName
            ));
        }

        return league;
    }

    @Override
    public Season createSeason(League league) {
        return new BootstrapFootballSeason(league);
    }
}