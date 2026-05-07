package com.playforgemanager.ui.screens;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class SportSelectionScreen implements UiScreen {
    private static final String DEFAULT_LEAGUE = "PlayForge Demo League";

    private final AppContext context;

    public SportSelectionScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        Label heading = new Label("Choose your sport");
        heading.getStyleClass().add("section-title");

        Label subheading = new Label("Both sports run through the same flow. You can save and switch later.");
        subheading.getStyleClass().add("section-subtitle");

        ToggleGroup group = new ToggleGroup();
        HBox sportToggles = new HBox(12);
        sportToggles.setAlignment(Pos.CENTER_LEFT);

        List<SportRegistration> registrations = context.getSportRegistry().getRegisteredSports();
        for (SportRegistration registration : registrations) {
            ToggleButton button = new ToggleButton(registration.getDisplayName());
            button.setUserData(registration.getSportId());
            button.setToggleGroup(group);
            button.getStyleClass().add("sport-toggle");
            button.setPrefHeight(56);
            button.setPrefWidth(180);
            sportToggles.getChildren().add(button);
        }
        if (!group.getToggles().isEmpty()) {
            group.selectToggle(group.getToggles().get(0));
        }

        Label leagueLabel = new Label("League name");
        leagueLabel.getStyleClass().add("field-label");
        TextField leagueName = new TextField(DEFAULT_LEAGUE);
        leagueName.setPrefWidth(360);

        Button startButton = new Button("Start season");
        startButton.getStyleClass().add("button-primary");
        startButton.setPrefHeight(44);
        startButton.setOnAction(e -> {
            if (group.getSelectedToggle() == null) {
                UiAlerts.error("Pick a sport", "Select a sport before starting the season.");
                return;
            }
            String sportId = (String) group.getSelectedToggle().getUserData();
            String name = leagueName.getText() == null ? "" : leagueName.getText().trim();
            if (name.isEmpty()) {
                UiAlerts.error("League name required", "Enter a league name to continue.");
                return;
            }
            try {
                GameSession session = context.getInitializationService().startNewSession(sportId, name);
                context.getUiSession().setActiveSession(session);
                context.getRouter().navigate(Screen.TEAM_OVERVIEW);
            } catch (RuntimeException ex) {
                UiAlerts.error("Could not start session", ex.getMessage());
            }
        });

        Button cancelButton = new Button("Back");
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> context.getRouter().navigate(Screen.HOME));

        HBox actions = new HBox(10, startButton, cancelButton);

        VBox card = new VBox(14,
                heading,
                subheading,
                new Label(" "),
                new Label("Sport"),
                sportToggles,
                new Label(" "),
                leagueLabel,
                leagueName,
                new Label(" "),
                actions);
        card.setPadding(new Insets(32));
        card.setMaxWidth(640);
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(40));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }
}
