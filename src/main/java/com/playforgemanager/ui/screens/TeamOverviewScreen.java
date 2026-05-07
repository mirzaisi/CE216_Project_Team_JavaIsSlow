package com.playforgemanager.ui.screens;

import com.playforgemanager.application.FixtureSummaryView;
import com.playforgemanager.application.TeamOverviewView;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TeamOverviewScreen implements UiScreen {
    private final AppContext context;

    public TeamOverviewScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }

        TeamOverviewView view = context.getTeamOverviewQueryService()
                .build(context.getUiSession().getActiveSession());

        Label name = new Label(view.controlledTeamName());
        name.getStyleClass().add("hero-title");
        Label subtitle = new Label(String.format(
                "%s • %s • Week %d",
                view.sportName(),
                view.leagueName(),
                view.currentWeek()
        ));
        subtitle.getStyleClass().add("section-subtitle");

        GridPane stats = new GridPane();
        stats.setHgap(28);
        stats.setVgap(10);
        addStat(stats, 0, "Rank", view.currentRank() + " / " + view.leagueSize());
        addStat(stats, 1, "Roster", view.rosterSize() + " players");
        addStat(stats, 2, "Available", view.availablePlayers() + " players");
        addStat(stats, 3, "Lineup",
                view.selectedLineupSize() == null ? "Not set" : view.selectedLineupSize() + " starters");
        addStat(stats, 4, "Tactic", view.selectedTacticName() == null ? "—" : view.selectedTacticName());
        addStat(stats, 5, "Training", view.trainingFocus() == null ? "—" : view.trainingFocus());
        addStat(stats, 6, "Status",
                view.seasonCompleted() ? "Season completed" : view.progressionState().name());

        VBox nextFixtureBox = buildNextFixtureBlock(view.nextFixture());

        HBox actions = new HBox(10);
        Button squad = primary("View Squad");
        squad.setOnAction(e -> context.getRouter().navigate(Screen.SQUAD));
        Button tactics = secondary("Tactics & Lineup");
        tactics.setOnAction(e -> context.getRouter().navigate(Screen.TACTICS_LINEUP));
        Button matchPrep = primary(view.seasonCompleted() ? "Season finished" : "Go to Match");
        matchPrep.setDisable(view.seasonCompleted());
        matchPrep.setOnAction(e -> context.getRouter().navigate(Screen.MATCH));
        actions.getChildren().addAll(squad, tactics, matchPrep);

        VBox card = new VBox(16, name, subtitle, stats, nextFixtureBox, actions);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private VBox buildNextFixtureBlock(FixtureSummaryView next) {
        Label header = new Label("Next match");
        header.getStyleClass().add("block-header");
        Label content;
        if (next == null) {
            content = new Label("No upcoming fixture.");
        } else {
            String venue = next.controlledTeamHome() ? "Home" : "Away";
            content = new Label(String.format(
                    "Week %d • %s vs %s (%s)",
                    next.week(),
                    next.homeTeamName(),
                    next.awayTeamName(),
                    venue
            ));
        }
        content.getStyleClass().add("muted");

        VBox box = new VBox(4, header, content);
        VBox.setMargin(box, new Insets(8, 0, 0, 0));
        return box;
    }

    private void addStat(GridPane grid, int rowIndex, String label, String value) {
        int row = rowIndex / 3;
        int col = (rowIndex % 3) * 2;
        Label labelNode = new Label(label.toUpperCase());
        labelNode.getStyleClass().add("stat-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-value");
        VBox cell = new VBox(2, labelNode, valueNode);
        cell.setMinWidth(140);
        HBox.setHgrow(cell, Priority.ALWAYS);
        grid.add(cell, col, row, 2, 1);
    }

    private Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-primary");
        button.setPrefHeight(40);
        return button;
    }

    private Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-secondary");
        button.setPrefHeight(40);
        return button;
    }
}
