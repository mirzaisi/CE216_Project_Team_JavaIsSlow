package com.playforgemanager.ui.screens;

import com.playforgemanager.application.save.SaveGameFormat;
import com.playforgemanager.application.save.SaveGameResult;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

public class SaveLoadScreen implements UiScreen {
    private final AppContext context;

    public SaveLoadScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        Label heading = new Label("Save & Load");
        heading.getStyleClass().add("section-title");

        Label info = new Label(
                "Save the current session to a JSON file, or load an earlier save to resume play. "
                        + "Save files use the extension " + SaveGameFormat.FILE_EXTENSION + "."
        );
        info.setWrapText(true);
        info.getStyleClass().add("section-subtitle");

        Button saveButton = new Button("Save current game");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setPrefHeight(40);
        saveButton.setOnAction(e -> handleSave());

        Button loadButton = new Button("Load saved game");
        loadButton.getStyleClass().add("button-secondary");
        loadButton.setPrefHeight(40);
        loadButton.setOnAction(e -> handleLoad());

        HBox actions = new HBox(10, saveButton, loadButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        boolean hasSession = context.getUiSession().hasSession();
        saveButton.setDisable(!hasSession);

        Label status;
        if (hasSession) {
            GameSession s = context.getUiSession().getActiveSession();
            status = new Label(String.format(
                    "Active session: %s — %s, week %d.",
                    s.getActiveSport().getName(),
                    s.getControlledTeam().getName(),
                    s.getCurrentSeason().getCurrentWeek()
            ));
        } else {
            status = new Label("No active session. You can still load a save.");
        }
        status.getStyleClass().add("muted");

        VBox card = new VBox(14, heading, info, status, new Label(" "), actions);
        card.setPadding(new Insets(28));
        card.setMaxWidth(720);
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        wrapper.setAlignment(Pos.TOP_LEFT);
        return wrapper;
    }

    private void handleSave() {
        if (!context.getUiSession().hasSession()) {
            UiAlerts.error("No session", "Start or load a session before saving.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save game");
        chooser.setInitialFileName("playforge-save" + SaveGameFormat.FILE_EXTENSION);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PlayForge Save (*" + SaveGameFormat.FILE_EXTENSION + ")",
                        "*" + SaveGameFormat.FILE_EXTENSION)
        );
        File target = chooser.showSaveDialog(context.getPrimaryStage());
        if (target == null) {
            return;
        }
        try {
            SaveGameResult result = context.getSaveGameService()
                    .save(context.getUiSession().getActiveSession(), target.toPath());
            UiAlerts.info("Saved", "Game saved to:\n" + result.savePath());
        } catch (Exception ex) {
            UiAlerts.error("Save failed", buildErrorMessage(ex));
        }
    }

    private void handleLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load game");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PlayForge Save (*" + SaveGameFormat.FILE_EXTENSION + ")",
                        "*" + SaveGameFormat.FILE_EXTENSION)
        );
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File source = chooser.showOpenDialog(context.getPrimaryStage());
        if (source == null) {
            return;
        }

        try {
            Path path = source.toPath();
            GameSession loaded = context.getLoadGameService().load(path);
            context.getUiSession().setActiveSession(loaded);
            UiAlerts.info("Loaded", "Game loaded from:\n" + path.toAbsolutePath().normalize());
            context.getRouter().navigate(Screen.TEAM_OVERVIEW);
        } catch (Exception ex) {
            UiAlerts.error("Load failed", buildErrorMessage(ex));
        }
    }

    private String buildErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }
}
