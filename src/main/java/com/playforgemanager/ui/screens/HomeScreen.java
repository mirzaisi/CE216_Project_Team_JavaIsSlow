package com.playforgemanager.ui.screens;

import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import com.playforgemanager.ui.UiAssets;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class HomeScreen implements UiScreen {
    private final AppContext context;

    public HomeScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        ImageView logoView = null;
        var logo = UiAssets.loadLogo();
        if (logo != null) {
            logoView = new ImageView(logo);
            logoView.setPreserveRatio(true);
            logoView.setFitWidth(240);
        }

        Label title = new Label("PlayForge Manager");
        title.getStyleClass().add("hero-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Sports manager — pick a sport, build your team, run the season.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(420);
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        Button newGame = primary("Start New Game");
        newGame.setOnAction(e -> context.getRouter().navigate(Screen.SPORT_SELECTION));

        Button loadGame = secondary("Load Saved Game");
        loadGame.setOnAction(e -> context.getRouter().navigate(Screen.SAVE_LOAD));

        VBox card = logoView == null
            ? new VBox(14, title, subtitle, new Label(" "), newGame, loadGame)
            : new VBox(14, logoView, title, subtitle, new Label(" "), newGame, loadGame);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(560);
        card.getStyleClass().add("card");

        VBox container = new VBox(card);
        container.setPadding(new Insets(60));
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    private Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-primary");
        button.setPrefHeight(44);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button-secondary");
        button.setPrefHeight(44);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
}
