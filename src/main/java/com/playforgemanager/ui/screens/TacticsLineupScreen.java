package com.playforgemanager.ui.screens;

import com.playforgemanager.application.setup.LineupSlotView;
import com.playforgemanager.application.setup.TacticOptionView;
import com.playforgemanager.application.setup.TeamSetupService;
import com.playforgemanager.application.setup.TeamSetupView;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.ui.AppContext;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TacticsLineupScreen implements UiScreen {
    private final AppContext context;

    public TacticsLineupScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }
        return buildView();
    }

    private Region buildView() {
        GameSession session = context.getUiSession().getActiveSession();
        TeamSetupService setupService = context.getTeamSetupService();
        TeamSetupView view = setupService.buildView(session);

        Label header = new Label("Tactics & Lineup");
        header.getStyleClass().add("section-title");
        Label subtitle = new Label(String.format(
                "%s • Starters %d / %d • Bench up to %d",
                view.controlledTeamName(),
                view.startingLineup().size(),
                view.requiredStarters(),
                view.benchLimit()
        ));
        subtitle.getStyleClass().add("section-subtitle");

        Label validationLabel = new Label(view.lineupValid()
                ? "Lineup valid for " + view.sportName() + " rules."
                : "Lineup needs attention: " + view.lineupValidationMessage());
        validationLabel.getStyleClass().add(view.lineupValid() ? "status-ok" : "status-warning");

        Button autoPick = new Button("Auto-pick lineup");
        autoPick.getStyleClass().add("button-primary");
        autoPick.setOnAction(e -> {
            try {
                setupService.autoPickLineup(session);
                rerender();
            } catch (RuntimeException ex) {
                UiAlerts.error("Could not auto-pick lineup", ex.getMessage());
            }
        });

        VBox lineupBlock = new VBox(8,
                blockHeader("Starting lineup"),
                buildLineupTable(view.startingLineup()));

        VBox benchBlock = new VBox(8,
                blockHeader("Bench"),
                buildLineupTable(view.bench()));

        VBox tacticsBlock = buildTacticsBlock(view);

        HBox columns = new HBox(20, lineupBlock, benchBlock, tacticsBlock);
        HBox.setHgrow(lineupBlock, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(benchBlock, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(tacticsBlock, javafx.scene.layout.Priority.ALWAYS);

        VBox card = new VBox(14, header, subtitle, validationLabel, autoPick, columns);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private VBox buildTacticsBlock(TeamSetupView view) {
        VBox box = new VBox(10);
        box.getChildren().add(blockHeader("Tactic"));
        Label currentTactic = new Label(view.selectedTacticName() == null
                ? "No tactic selected"
                : view.selectedTacticName());
        currentTactic.getStyleClass().add("stat-value");
        Label description = new Label(view.selectedTacticDescription() == null
                ? ""
                : view.selectedTacticDescription());
        description.setWrapText(true);
        description.getStyleClass().add("muted");

        VBox optionsBox = new VBox(6);
        for (TacticOptionView option : view.tacticOptions()) {
            Button button = new Button(option.name());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add(option.selected() ? "tactic-button-active" : "tactic-button");
            button.setOnAction(e -> applyTactic(option.id()));
            optionsBox.getChildren().add(button);
        }
        box.getChildren().addAll(currentTactic, description, new Label(" "), optionsBox);
        return box;
    }

    private void applyTactic(String tacticId) {
        try {
            context.getTeamSetupService().applyTactic(context.getUiSession().getActiveSession(), tacticId);
            rerender();
        } catch (RuntimeException ex) {
            UiAlerts.error("Could not apply tactic", ex.getMessage());
        }
    }

    private void rerender() {
        context.getRouter().navigate(com.playforgemanager.ui.Screen.TACTICS_LINEUP);
    }

    private static TableView<LineupSlotView> buildLineupTable(java.util.List<LineupSlotView> slots) {
        TableView<LineupSlotView> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(slots));
        table.getColumns().add(TableColumns.string("Role", LineupSlotView::roleLabel));
        table.getColumns().add(TableColumns.string("Player", LineupSlotView::playerName));
        table.getColumns().add(TableColumns.string("Status", slot -> slot.available() ? "Available" : "Out"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(360);
        return table;
    }

    private static Label blockHeader(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("block-header");
        return label;
    }
}
