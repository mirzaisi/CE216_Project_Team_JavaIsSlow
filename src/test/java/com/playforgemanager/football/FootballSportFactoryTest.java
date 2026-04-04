package com.playforgemanager.football;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FootballSportFactoryTest {

    @Test
    void createLeagueBuildsBootstrapLeagueWithExpectedNumberOfTeams() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Bootstrap League");

        assertInstanceOf(FootballLeague.class, league);
        assertEquals("Bootstrap League", league.getName());
        assertEquals(4, league.getTeamCount());
        assertEquals("Red Hawks", league.getTeams().get(0).getName());
    }

    @Test
    void createLeaguePopulatesEachTeamWithPlayers() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Bootstrap League");

        for (int i = 0; i < league.getTeams().size(); i++) {
            assertEquals(18, league.getTeams().get(i).getRoster().size());
        }
    }

    @Test
    void createSeasonWrapsProvidedLeague() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);
        League league = factory.createLeague("Bootstrap League");

        Season season = factory.createSeason(league);

        assertInstanceOf(FootballSeason.class, season);
        assertEquals(league, season.getLeague());
        assertEquals(1, season.getCurrentWeek());
    }

    @Test
    void constructorRejectsTeamCountSmallerThanTwo() {
        assertThrows(IllegalArgumentException.class, () ->
                new FootballSportFactory(new FakeAssetProvider(), 1)
        );
    }

    private static class FakeAssetProvider implements AssetProvider {
        @Override
        public List<String> getMaleNames() {
            return List.of("Ali", "Mert", "Can", "Emir");
        }

        @Override
        public List<String> getFemaleNames() {
            return List.of("Ece", "Zeynep", "Defne", "Elif");
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
