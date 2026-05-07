package com.playforgemanager.ui.screens;

import com.playforgemanager.application.FixtureListView;
import com.playforgemanager.application.FixtureSummaryView;
import com.playforgemanager.ui.AppContext;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FixturesScreen implements UiScreen {
    private final AppContext context;

    public FixturesScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }

        FixtureListView view = context.getFixtureListQueryService()
                .build(context.getUiSession().getActiveSession());

        Label header = new Label("Fixtures");
        header.getStyleClass().add("section-title");
        Label subtitle = new Label(String.format(
                "%s • Currently in week %d",
                view.teamName(),
                view.currentWeek()
        ));
        subtitle.getStyleClass().add("section-subtitle");

        TableView<FixtureSummaryView> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(view.fixtures()));
        table.getColumns().add(TableColumns.integer("Week", FixtureSummaryView::week));
        table.getColumns().add(TableColumns.string("Home", FixtureSummaryView::homeTeamName));
        table.getColumns().add(TableColumns.string("Away", FixtureSummaryView::awayTeamName));
        table.getColumns().add(TableColumns.string("Score", FixturesScreen::scoreLabel));
        table.getColumns().add(TableColumns.string("Status", f -> f.played() ? "Played" : "Upcoming"));
        table.getColumns().add(TableColumns.string("Your team",
                f -> f.controlledTeamInvolved() ? (f.controlledTeamHome() ? "Home" : "Away") : ""));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(520);

        VBox card = new VBox(12, header, subtitle, table);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private static String scoreLabel(FixtureSummaryView fixture) {
        if (!fixture.played()) {
            return "—";
        }
        return fixture.homeScore() + " : " + fixture.awayScore();
    }
}
