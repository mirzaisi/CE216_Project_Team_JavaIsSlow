package com.playforgemanager.ui.screens;

import com.playforgemanager.application.StandingsRowView;
import com.playforgemanager.application.StandingsView;
import com.playforgemanager.ui.AppContext;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class StandingsScreen implements UiScreen {
    private final AppContext context;

    public StandingsScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }

        StandingsView view = context.getStandingsQueryService()
                .build(context.getUiSession().getActiveSession());

        Label header = new Label("League Table");
        header.getStyleClass().add("section-title");
        Label subtitle = new Label(view.leagueName() + " • Week " + view.currentWeek());
        subtitle.getStyleClass().add("section-subtitle");

        TableView<StandingsRowView> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(view.rows()));
        table.getColumns().add(TableColumns.integer("#", StandingsRowView::rank));
        table.getColumns().add(TableColumns.string("Team", StandingsRowView::teamName));
        table.getColumns().add(TableColumns.integer("P", StandingsRowView::played));
        table.getColumns().add(TableColumns.integer("W", StandingsRowView::wins));
        table.getColumns().add(TableColumns.integer("D", StandingsRowView::draws));
        table.getColumns().add(TableColumns.integer("L", StandingsRowView::losses));
        table.getColumns().add(TableColumns.integer("F", StandingsRowView::scoresFor));
        table.getColumns().add(TableColumns.integer("A", StandingsRowView::scoresAgainst));
        table.getColumns().add(TableColumns.integer("Diff", StandingsRowView::scoreDifference));
        table.getColumns().add(TableColumns.integer("Pts", StandingsRowView::points));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(520);

        VBox card = new VBox(12, header, subtitle, table);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }
}
