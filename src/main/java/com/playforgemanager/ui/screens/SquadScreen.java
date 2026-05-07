package com.playforgemanager.ui.screens;

import com.playforgemanager.application.PlayerSummaryView;
import com.playforgemanager.application.SquadView;
import com.playforgemanager.ui.AppContext;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SquadScreen implements UiScreen {
    private final AppContext context;

    public SquadScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }

        SquadView view = context.getSquadQueryService().build(context.getUiSession().getActiveSession());

        Label header = new Label("Squad — " + view.teamName());
        header.getStyleClass().add("section-title");
        Label summary = new Label(String.format(
                "%d players • %d available",
                view.totalPlayers(),
                view.availablePlayers()
        ));
        summary.getStyleClass().add("section-subtitle");

        TableView<PlayerSummaryView> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(view.players()));
        table.getColumns().add(TableColumns.string("Name", PlayerSummaryView::name));
        table.getColumns().add(TableColumns.string("Role", PlayerSummaryView::roleLabel));
        table.getColumns().add(TableColumns.string("Status", SquadScreen::statusLabel));
        table.getColumns().add(TableColumns.string("In Lineup", p -> p.selectedForCurrentLineup() ? "Yes" : "—"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(440);

        VBox card = new VBox(12, header, summary, table);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private static String statusLabel(PlayerSummaryView player) {
        if (!player.available() && player.injuryMatchesRemaining() > 0) {
            return "Injured (" + player.injuryMatchesRemaining() + ")";
        }
        return player.available() ? "Available" : "Out";
    }
}
