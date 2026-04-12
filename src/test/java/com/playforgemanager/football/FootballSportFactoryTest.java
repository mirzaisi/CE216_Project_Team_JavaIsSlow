package com.playforgemanager.football;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSportFactoryTest {

    @Test
    void createLeagueBuildsFootballLeagueWithExpectedNumberOfTeams() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Test League");

        assertInstanceOf(FootballLeague.class, league);
        assertEquals("Test League", league.getName());
        assertEquals(4, league.getTeamCount());
        assertEquals("Red Hawks", league.getTeams().get(0).getName());
    }

    @Test
    void createLeaguePopulatesEachTeamWithPlayers() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Test League");

        for (int i = 0; i < league.getTeams().size(); i++) {
            assertEquals(19, league.getTeams().get(i).getRoster().size());
        }
    }

    @Test
    void createLeagueAssignsLineupsThatSatisfyFootballPolicy() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);
        FootballRuleset ruleset = new FootballRuleset();

        League league = factory.createLeague("Test League");

        for (var team : league.getTeams()) {
            FootballTeam footballTeam = (FootballTeam) team;
            FootballLineup lineup = footballTeam.getSelectedFootballLineup();

            assertTrue(ruleset.isValidLineup(lineup));
            assertEquals(FootballRuleset.REQUIRED_GOALKEEPERS, countPosition(lineup, FootballPosition.GOALKEEPER));
            assertEquals(FootballRuleset.REQUIRED_DEFENDERS, countPosition(lineup, FootballPosition.DEFENDER));
            assertEquals(FootballRuleset.REQUIRED_MIDFIELDERS, countPosition(lineup, FootballPosition.MIDFIELDER));
            assertEquals(FootballRuleset.REQUIRED_FORWARDS, countPosition(lineup, FootballPosition.FORWARD));
        }
    }

    @Test
    void createSeasonWrapsProvidedLeague() {
        FootballSportFactory factory = new FootballSportFactory(new FakeAssetProvider(), 4);
        League league = factory.createLeague("Test League");

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

    private long countPosition(FootballLineup lineup, FootballPosition position) {
        return lineup.getStartingPlayers().stream()
                .filter(player -> player.getPosition() == position)
                .count();
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
