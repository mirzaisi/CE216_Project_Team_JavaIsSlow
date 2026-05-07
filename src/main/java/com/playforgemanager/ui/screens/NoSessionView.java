package com.playforgemanager.ui.screens;

import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

final class NoSessionView {
    private NoSessionView() {}

    static Region build(AppContext context) {
        Label title = new Label("No active session");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Start a new game or load a save to use this screen.");
        subtitle.getStyleClass().add("section-subtitle");

        Button newGame = new Button("Start new game");
        newGame.getStyleClass().add("button-primary");
        newGame.setOnAction(e -> context.getRouter().navigate(Screen.SPORT_SELECTION));

        Button load = new Button("Load saved game");
        load.getStyleClass().add("button-secondary");
        load.setOnAction(e -> context.getRouter().navigate(Screen.SAVE_LOAD));

        HBox actions = new HBox(10, newGame, load);

        VBox card = new VBox(12, title, subtitle, new Label(" "), actions);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(40));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }
}
