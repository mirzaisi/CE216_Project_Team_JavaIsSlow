package com.playforgemanager.ui.screens;

import com.playforgemanager.application.MatchProcessingResult;
import com.playforgemanager.application.PostMatchSummaryView;
import com.playforgemanager.application.TeamAvailabilityChange;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PostMatchScreen implements UiScreen {
    private final AppContext context;

    public PostMatchScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        MatchProcessingResult lastResult = context.getUiSession().getLastMatchResult();
        if (lastResult == null) {
            return emptyView();
        }

        PostMatchSummaryView summary = context.getPostMatchSummaryQueryService().build(lastResult);

        Label header = new Label(summary.outcomeLabel() + " — Week " + summary.weekNumber());
        header.getStyleClass().addAll("section-title", outcomeStyle(summary.outcomeLabel()));

        Label score = new Label(String.format(
                "%s %d — %d %s",
                summary.controlledTeamHome() ? summary.controlledTeamName() : summary.opponentTeamName(),
                summary.controlledTeamHome() ? summary.controlledTeamScore() : summary.opponentScore(),
                summary.controlledTeamHome() ? summary.opponentScore() : summary.controlledTeamScore(),
                summary.controlledTeamHome() ? summary.opponentTeamName() : summary.controlledTeamName()
        ));
        score.getStyleClass().add("hero-title");

        Label rankLine = new Label("Standing after match: rank " + summary.controlledTeamRankAfterMatch());
        rankLine.getStyleClass().add("section-subtitle");

        VBox availabilityBlock = new VBox(6);
        Label availabilityHeader = new Label("Squad availability changes");
        availabilityHeader.getStyleClass().add("block-header");
        availabilityBlock.getChildren().add(availabilityHeader);
        boolean anyChange = false;
        for (TeamAvailabilityChange change : summary.availabilityChanges()) {
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
                    delta
            ));
            line.getStyleClass().add(delta >= 0 ? "status-ok" : "status-warning");
            availabilityBlock.getChildren().add(line);
        }
        if (!anyChange) {
            Label noChange = new Label("No squad availability changes.");
            noChange.getStyleClass().add("muted");
            availabilityBlock.getChildren().add(noChange);
        }

        Button advanceWeek = new Button("Advance week");
        advanceWeek.getStyleClass().add("button-primary");
        advanceWeek.setOnAction(e -> {
            try {
                var weekResult = context.getWeekProgressionService()
                        .advanceOneStep(context.getUiSession().getActiveSession());
                context.getUiSession().setLastWeekResult(weekResult);
                context.getRouter().navigate(Screen.POST_WEEK);
            } catch (RuntimeException ex) {
                UiAlerts.error("Could not advance week", ex.getMessage());
            }
        });

        Button viewTable = new Button("League table");
        viewTable.getStyleClass().add("button-secondary");
        viewTable.setOnAction(e -> context.getRouter().navigate(Screen.STANDINGS));

        Button viewFixtures = new Button("Fixtures");
        viewFixtures.getStyleClass().add("button-secondary");
        viewFixtures.setOnAction(e -> context.getRouter().navigate(Screen.FIXTURES));

        HBox actions = new HBox(10, advanceWeek, viewTable, viewFixtures);

        VBox card = new VBox(14, header, score, rankLine, availabilityBlock, actions);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private String outcomeStyle(String outcomeLabel) {
        return switch (outcomeLabel) {
            case "WIN" -> "status-ok";
            case "LOSS" -> "status-warning";
            default -> "section-subtitle";
        };
    }

    private Region emptyView() {
        Label info = new Label("No match has been played yet in this session.");
        info.getStyleClass().add("section-subtitle");
        Button match = new Button("Go to next match");
        match.getStyleClass().add("button-primary");
        match.setOnAction(e -> context.getRouter().navigate(Screen.MATCH));
        VBox card = new VBox(12, info, match);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }
}
