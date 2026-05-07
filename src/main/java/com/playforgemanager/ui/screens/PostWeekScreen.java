package com.playforgemanager.ui.screens;

import com.playforgemanager.application.TeamAvailabilityChange;
import com.playforgemanager.application.WeekProgressionResult;
import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Match;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PostWeekScreen implements UiScreen {
    private final AppContext context;

    public PostWeekScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        WeekProgressionResult result = context.getUiSession().getLastWeekResult();
        if (result == null) {
            return emptyView();
        }

        Label header = new Label("Week " + result.getPlayedWeek() + " summary");
        header.getStyleClass().add("section-title");

        Label statusLine = new Label(result.isSeasonCompleted()
                ? "Season completed."
                : "Now in week " + result.getCurrentWeekAfterProgression() + ".");
        statusLine.getStyleClass().add("section-subtitle");

        VBox fixtureBlock = new VBox(4);
        Label fixturesHeader = new Label("Played fixtures");
        fixturesHeader.getStyleClass().add("block-header");
        fixtureBlock.getChildren().add(fixturesHeader);
        if (result.getPlayedFixtures().isEmpty()) {
            Label none = new Label("No fixtures played this week.");
            none.getStyleClass().add("muted");
            fixtureBlock.getChildren().add(none);
        } else {
            for (Fixture fixture : result.getPlayedFixtures()) {
                Match match = fixture.getPlayedMatch();
                String line;
                if (match == null) {
                    line = String.format("Week %d: %s vs %s — pending",
                            fixture.getWeek(), fixture.getHomeTeam().getName(), fixture.getAwayTeam().getName());
                } else {
                    line = String.format("%s %d — %d %s",
                            fixture.getHomeTeam().getName(), match.getHomeScore(),
                            match.getAwayScore(), fixture.getAwayTeam().getName());
                }
                fixtureBlock.getChildren().add(new Label(line));
            }
        }

        VBox availabilityBlock = new VBox(4);
        Label availabilityHeader = new Label("Squad availability changes");
        availabilityHeader.getStyleClass().add("block-header");
        availabilityBlock.getChildren().add(availabilityHeader);
        boolean anyChange = false;
        for (TeamAvailabilityChange change : result.getAvailabilityChanges()) {
            int before = change.getAvailablePlayersBefore();
            int after = change.getAvailablePlayersAfter();
            if (before == after) {
                continue;
            }
            anyChange = true;
            int delta = after - before;
            Label line = new Label(String.format(
                    "%s: %d → %d (%s%d)",
                    change.getTeam().getName(),
                    before,
                    after,
                    delta >= 0 ? "+" : "",
                    delta));
            line.getStyleClass().add(delta >= 0 ? "status-ok" : "status-warning");
            availabilityBlock.getChildren().add(line);
        }
        if (!anyChange) {
            Label none = new Label("No availability changes.");
            none.getStyleClass().add("muted");
            availabilityBlock.getChildren().add(none);
        }

        Button table = new Button("League table");
        table.getStyleClass().add("button-secondary");
        table.setOnAction(e -> context.getRouter().navigate(Screen.STANDINGS));

        Button next = new Button(result.isSeasonCompleted() ? "Back to home" : "Continue");
        next.getStyleClass().add("button-primary");
        next.setOnAction(e -> context.getRouter().navigate(
                result.isSeasonCompleted() ? Screen.STANDINGS : Screen.TEAM_OVERVIEW));

        HBox actions = new HBox(10, next, table);

        VBox card = new VBox(14, header, statusLine, fixtureBlock, availabilityBlock, actions);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private Region emptyView() {
        Label info = new Label("No week has been advanced yet in this session.");
        info.getStyleClass().add("section-subtitle");
        Button toMatch = new Button("Go to next match");
        toMatch.getStyleClass().add("button-primary");
        toMatch.setOnAction(e -> context.getRouter().navigate(Screen.MATCH));
        VBox card = new VBox(12, info, toMatch);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }
}
