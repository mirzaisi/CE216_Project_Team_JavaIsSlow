package com.playforgemanager.ui;

import com.playforgemanager.ui.screens.FixturesScreen;
import com.playforgemanager.ui.screens.HomeScreen;
import com.playforgemanager.ui.screens.MatchScreen;
import com.playforgemanager.ui.screens.PostMatchScreen;
import com.playforgemanager.ui.screens.PostWeekScreen;
import com.playforgemanager.ui.screens.SaveLoadScreen;
import com.playforgemanager.ui.screens.SportSelectionScreen;
import com.playforgemanager.ui.screens.SquadScreen;
import com.playforgemanager.ui.screens.StandingsScreen;
import com.playforgemanager.ui.screens.TacticsLineupScreen;
import com.playforgemanager.ui.screens.TeamOverviewScreen;
import com.playforgemanager.ui.screens.UiScreen;
import javafx.scene.layout.Region;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class ScreenRouter {
    private final AppContext context;
    private final MainShell shell;
    private final Map<Screen, Supplier<UiScreen>> factories = new EnumMap<>(Screen.class);
    private Screen currentScreen;

    public ScreenRouter(AppContext context, MainShell shell) {
        this.context = Objects.requireNonNull(context);
        this.shell = Objects.requireNonNull(shell);
        registerFactories();
    }

    public void navigate(Screen screen) {
        Supplier<UiScreen> factory = factories.get(Objects.requireNonNull(screen, "Screen cannot be null."));
        if (factory == null) {
            throw new IllegalStateException("No screen factory registered for: " + screen);
        }
        UiScreen instance = factory.get();
        Region content = instance.render();
        currentScreen = screen;
        shell.setContent(screen, content);
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    private void registerFactories() {
        factories.put(Screen.HOME, () -> new HomeScreen(context));
        factories.put(Screen.SPORT_SELECTION, () -> new SportSelectionScreen(context));
        factories.put(Screen.SAVE_LOAD, () -> new SaveLoadScreen(context));
        factories.put(Screen.TEAM_OVERVIEW, () -> new TeamOverviewScreen(context));
        factories.put(Screen.SQUAD, () -> new SquadScreen(context));
        factories.put(Screen.TACTICS_LINEUP, () -> new TacticsLineupScreen(context));
        factories.put(Screen.FIXTURES, () -> new FixturesScreen(context));
        factories.put(Screen.STANDINGS, () -> new StandingsScreen(context));
        factories.put(Screen.MATCH, () -> new MatchScreen(context));
        factories.put(Screen.POST_MATCH, () -> new PostMatchScreen(context));
        factories.put(Screen.POST_WEEK, () -> new PostWeekScreen(context));
    }
}
