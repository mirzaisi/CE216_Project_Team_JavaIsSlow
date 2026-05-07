package com.playforgemanager.handball;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.League;
import com.playforgemanager.core.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandballSportFactoryTest {

    @Test
    void createLeagueBuildsHandballLeagueWithExpectedNumberOfTeams() {
        HandballSportFactory factory = new HandballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Test League");

        assertInstanceOf(HandballLeague.class, league);
        assertEquals("Test League", league.getName());
        assertEquals(4, league.getTeamCount());
        assertEquals("Red Hawks", league.getTeams().get(0).getName());
    }

    @Test
    void createLeaguePopulatesEachTeamWithPlayers() {
        HandballSportFactory factory = new HandballSportFactory(new FakeAssetProvider(), 4);

        League league = factory.createLeague("Test League");

        for (int i = 0; i < league.getTeams().size(); i++) {
            assertEquals(14, league.getTeams().get(i).getRoster().size());
        }
    }

    @Test
    void createLeagueAssignsLineupsThatSatisfyHandballPolicy() {
        HandballSportFactory factory = new HandballSportFactory(new FakeAssetProvider(), 4);
        HandballRuleset ruleset = new HandballRuleset();

        League league = factory.createLeague("Test League");

        for (var team : league.getTeams()) {
            HandballTeam handballTeam = (HandballTeam) team;
            HandballLineup lineup = handballTeam.getSelectedHandballLineup();

            assertTrue(ruleset.isValidLineup(lineup));
            assertEquals(1, countPosition(lineup, HandballPosition.GOALKEEPER));
            assertEquals(1, countPosition(lineup, HandballPosition.LEFT_WING));
            assertEquals(1, countPosition(lineup, HandballPosition.RIGHT_WING));
            assertEquals(1, countPosition(lineup, HandballPosition.LEFT_BACK));
            assertEquals(1, countPosition(lineup, HandballPosition.CENTER_BACK));
            assertEquals(1, countPosition(lineup, HandballPosition.RIGHT_BACK));
            assertEquals(1, countPosition(lineup, HandballPosition.PIVOT));
        }
    }

    @Test
    void createSeasonWrapsProvidedLeague() {
        HandballSportFactory factory = new HandballSportFactory(new FakeAssetProvider(), 4);
        League league = factory.createLeague("Test League");

        Season season = factory.createSeason(league);

        assertInstanceOf(HandballSeason.class, season);
        assertEquals(league, season.getLeague());
        assertEquals(1, season.getCurrentWeek());
    }

    @Test
    void constructorRejectsTeamCountSmallerThanTwo() {
        assertThrows(IllegalArgumentException.class, () ->
                new HandballSportFactory(new FakeAssetProvider(), 1)
        );
    }

    private long countPosition(HandballLineup lineup, HandballPosition position) {
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
