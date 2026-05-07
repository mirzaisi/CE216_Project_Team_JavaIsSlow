package com.playforgemanager.ui;

import com.playforgemanager.application.DefaultMatchProcessingRegistry;
import com.playforgemanager.application.DefaultSportRegistry;
import com.playforgemanager.application.DefaultWeekProgressionRegistry;
import com.playforgemanager.application.FixtureListQueryService;
import com.playforgemanager.application.GameInitializationService;
import com.playforgemanager.application.MatchProcessingService;
import com.playforgemanager.application.PostMatchSummaryQueryService;
import com.playforgemanager.application.SportRegistry;
import com.playforgemanager.application.SquadQueryService;
import com.playforgemanager.application.StandingsQueryService;
import com.playforgemanager.application.TeamOverviewQueryService;
import com.playforgemanager.application.WeekProgressionService;
import com.playforgemanager.application.save.DefaultSaveGameRestorationRegistry;
import com.playforgemanager.application.save.LoadGameService;
import com.playforgemanager.application.save.SaveGameService;
import com.playforgemanager.application.setup.DefaultTeamSetupRegistry;
import com.playforgemanager.application.setup.TeamSetupService;
import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;
import com.playforgemanager.infrastructure.JsonSaveGameReader;
import com.playforgemanager.infrastructure.JsonSaveGameWriter;
import javafx.stage.Stage;

public final class AppContext {
    private final Stage primaryStage;
    private final UiSession uiSession;
    private final SportRegistry sportRegistry;
    private final GameInitializationService initializationService;
    private final WeekProgressionService weekProgressionService;
    private final MatchProcessingService matchProcessingService;
    private final TeamOverviewQueryService teamOverviewQueryService;
    private final SquadQueryService squadQueryService;
    private final FixtureListQueryService fixtureListQueryService;
    private final StandingsQueryService standingsQueryService;
    private final PostMatchSummaryQueryService postMatchSummaryQueryService;
    private final TeamSetupService teamSetupService;
    private final SaveGameService saveGameService;
    private final LoadGameService loadGameService;
    private ScreenRouter router;

    public AppContext(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.uiSession = new UiSession();

        AssetProvider assetProvider = new InMemoryAssetProvider();
        this.sportRegistry = DefaultSportRegistry.create(assetProvider);
        this.initializationService = new GameInitializationService(sportRegistry);
        this.weekProgressionService = new WeekProgressionService(DefaultWeekProgressionRegistry.create());
        this.matchProcessingService = new MatchProcessingService(DefaultMatchProcessingRegistry.create());
        this.teamOverviewQueryService = new TeamOverviewQueryService();
        this.squadQueryService = new SquadQueryService();
        this.fixtureListQueryService = new FixtureListQueryService();
        this.standingsQueryService = new StandingsQueryService();
        this.postMatchSummaryQueryService = new PostMatchSummaryQueryService();
        this.teamSetupService = new TeamSetupService(DefaultTeamSetupRegistry.create());
        this.saveGameService = new SaveGameService(new JsonSaveGameWriter());
        this.loadGameService = new LoadGameService(
                new JsonSaveGameReader(),
                sportRegistry,
                DefaultSaveGameRestorationRegistry.create()
        );
    }

    public Stage getPrimaryStage() { return primaryStage; }
    public UiSession getUiSession() { return uiSession; }
    public SportRegistry getSportRegistry() { return sportRegistry; }
    public GameInitializationService getInitializationService() { return initializationService; }
    public WeekProgressionService getWeekProgressionService() { return weekProgressionService; }
    public MatchProcessingService getMatchProcessingService() { return matchProcessingService; }
    public TeamOverviewQueryService getTeamOverviewQueryService() { return teamOverviewQueryService; }
    public SquadQueryService getSquadQueryService() { return squadQueryService; }
    public FixtureListQueryService getFixtureListQueryService() { return fixtureListQueryService; }
    public StandingsQueryService getStandingsQueryService() { return standingsQueryService; }
    public PostMatchSummaryQueryService getPostMatchSummaryQueryService() { return postMatchSummaryQueryService; }
    public TeamSetupService getTeamSetupService() { return teamSetupService; }
    public SaveGameService getSaveGameService() { return saveGameService; }
    public LoadGameService getLoadGameService() { return loadGameService; }

    public ScreenRouter getRouter() {
        if (router == null) {
            throw new IllegalStateException("Screen router has not been wired yet.");
        }
        return router;
    }

    public void setRouter(ScreenRouter router) {
        this.router = router;
    }
}
