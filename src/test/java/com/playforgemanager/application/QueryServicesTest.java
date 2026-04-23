package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.football.FootballLineup;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.football.FootballTactic;
import com.playforgemanager.football.FootballTeam;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryServicesTest {

    @Test
    void teamOverviewBuildsGenericSummaryFromSession() {
        GameSession session = createFootballSession();

        TeamOverviewView overview = new TeamOverviewQueryService().build(session);

        assertEquals("football", overview.sportId());
        assertEquals("Football", overview.sportName());
        assertEquals("Red Hawks", overview.controlledTeamName());
        assertEquals(4, overview.leagueSize());
        assertEquals(19, overview.rosterSize());
        assertNotNull(overview.selectedTacticName());
        assertNotNull(overview.trainingFocus());
        assertNotNull(overview.nextFixture());
    }

    @Test
    void squadQueryBuildsGenericPlayerSummaries() {
        GameSession session = createFootballSession();

        SquadView squad = new SquadQueryService().build(session);

        assertEquals("Red Hawks", squad.teamName());
        assertEquals(19, squad.totalPlayers());
        assertEquals(19, squad.availablePlayers());
        assertEquals(19, squad.players().size());
        assertTrue(squad.players().stream().allMatch(player -> player.roleLabel() != null && !player.roleLabel().isBlank()));
        assertTrue(squad.players().stream().anyMatch(PlayerSummaryView::selectedForCurrentLineup));
    }

    @Test
    void fixtureAndStandingsQueriesReturnGenericReadModels() {
        GameSession session = createFootballSession();
        new WeekProgressionService(DefaultWeekProgressionRegistry.create()).advanceOneStep(session);

        FixtureListView fixtureList = new FixtureListQueryService().build(session);
        StandingsView standings = new StandingsQueryService().build(session);

        assertEquals(12, fixtureList.fixtures().size());
        assertTrue(fixtureList.fixtures().stream().anyMatch(FixtureSummaryView::played));
        assertEquals("football", standings.sportId());
        assertEquals(4, standings.rows().size());
        assertEquals(1, standings.rows().get(0).rank());
        assertTrue(standings.rows().stream().allMatch(row -> row.points() >= 0));
    }

    @Test
    void postMatchSummaryBuildsUiFriendlyResultWithoutSportSpecificTypes() {
        GameSession session = createFootballSession();
        FootballTeam controlledTeam = (FootballTeam) session.getControlledTeam();
        FootballLineup lineup = controlledTeam.getSelectedFootballLineup();
        FootballTactic tactic = controlledTeam.getSelectedFootballTactic();
        MatchProcessingResult result = new MatchProcessingService(DefaultMatchProcessingRegistry.create())
                .playControlledMatch(session, lineup, tactic);

        PostMatchSummaryView summary = new PostMatchSummaryQueryService().build(result);

        assertEquals("football", summary.sportId());
        assertEquals(controlledTeam.getName(), summary.controlledTeamName());
        assertNotNull(summary.fixture());
        assertTrue(summary.controlledTeamScore() >= 0);
        assertTrue(summary.opponentScore() >= 0);
        assertTrue(summary.outcomeLabel().equals("WIN")
                || summary.outcomeLabel().equals("DRAW")
                || summary.outcomeLabel().equals("LOSS"));
        assertFalse(summary.availabilityChanges().isEmpty());
    }

    private GameSession createFootballSession() {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        GameInitializationService initializationService =
                new GameInitializationService(new FootballSportFactory(assetProvider, 4));
        return initializationService.startNewSession("Integration League");
    }
}
