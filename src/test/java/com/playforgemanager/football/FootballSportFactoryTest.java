package com.playforgemanager.football;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FootballSportFactoryTest {

    @Test
    void createLeagueBuildsExpectedBootstrapLeagueAndTeams() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Bootstrap League");

        assertInstanceOf(BootstrapFootballLeague.class, league);
        assertEquals("Bootstrap League", league.getName());
        assertEquals(4, league.getTeamCount());
        assertEquals("Red Hawks", league.getTeams().get(0).getName());
    }

    @Test
    void createSeasonWrapsProvidedLeague() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);
        League league = factory.createLeague("Bootstrap League");

        Season season = factory.createSeason(league);

        assertInstanceOf(BootstrapFootballSeason.class, season);
        assertEquals(league, season.getLeague());
        assertEquals(1, season.getCurrentWeek());
    }

    private static class FakeAssetProvider implements AssetProvider {
        @Override
        public List<String> getMaleNames() {
            return List.of("Ali", "Mert");
        }

        @Override
        public List<String> getFemaleNames() {
            return List.of("Ece", "Zeynep");
        }

        @Override
        public List<String> getTeamNames() {
            return List.of("Red Hawks", "Blue Wolves", "Golden Stars", "Iron Lions");
        }

        @Override
        public List<String> getLogoReferences() {
            return List.of("logo1", "logo2", "logo3", "logo4");
        }
    }
}