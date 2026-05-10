package com.playforgemanager.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MainShell {
    private static final List<Screen> IN_GAME_SCREENS = List.of(
            Screen.TEAM_OVERVIEW,
            Screen.SQUAD,
            Screen.TACTICS_LINEUP,
            Screen.FIXTURES,
            Screen.STANDINGS,
            Screen.MATCH,
            Screen.SAVE_LOAD
    );

    private final AppContext context;
    private final BorderPane root;
    private final VBox sidebar;
    private final Label titleLabel;
    private final Label sessionLabel;
    private final Label weekLabel;
    private final Map<Screen, Button> sidebarButtons = new EnumMap<>(Screen.class);

    public MainShell(AppContext context) {
        this.context = context;
        this.root = new BorderPane();
        this.root.getStyleClass().add("app-root");

        this.titleLabel = new Label();
        this.titleLabel.getStyleClass().add("screen-title");

        this.sessionLabel = new Label();
        this.sessionLabel.getStyleClass().add("session-label");

        this.weekLabel = new Label();
        this.weekLabel.getStyleClass().add("week-label");

        this.sidebar = buildSidebar();
        this.root.setLeft(sidebar);
        this.root.setTop(buildHeader());
    }

    public Region getRoot() {
        return root;
    }

    public void setContent(Screen screen, Region content) {
        titleLabel.setText(screen.getTitle());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");
        BorderPane.setMargin(scroll, new Insets(0));
        root.setCenter(scroll);
        refreshSidebarSelection(screen);
        refreshSessionLabels();
    }

    public void refreshSessionLabels() {
        UiSession session = context.getUiSession();
        if (!session.hasSession()) {
            sessionLabel.setText("No active session");
            weekLabel.setText("");
            setInGameButtonsEnabled(false);
            return;
        }
        var active = session.getActiveSession();
        sessionLabel.setText(String.format(
                "%s — %s",
                active.getActiveSport().getName(),
                active.getControlledTeam().getName()
        ));
        weekLabel.setText(active.getCurrentSeason().isCompleted()
                ? "Season completed"
                : "Week " + active.getCurrentSeason().getCurrentWeek());
        setInGameButtonsEnabled(true);
    }

    private VBox buildSidebar() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(20, 16, 20, 16));
        box.setPrefWidth(220);
        box.getStyleClass().add("sidebar");

        Text logoText = new Text("PlayForge");
        logoText.getStyleClass().add("brand");
        Text taglineText = new Text("Manager");
        taglineText.getStyleClass().add("brand-tag");

        VBox brand = new VBox(8);
        var logo = UiAssets.loadLogo();
        if (logo != null) {
            ImageView logoView = new ImageView(logo);
            logoView.setPreserveRatio(true);
            logoView.setFitWidth(140);
            brand.getChildren().add(logoView);
        }
        brand.getChildren().addAll(logoText, taglineText);
        brand.setPadding(new Insets(0, 0, 16, 0));

        box.getChildren().add(brand);
        box.getChildren().add(buildNavButton(Screen.HOME));
        box.getChildren().add(buildNavButton(Screen.SPORT_SELECTION));

        Label divider = new Label("Game");
        divider.getStyleClass().add("nav-divider");
        VBox.setMargin(divider, new Insets(14, 0, 4, 4));
        box.getChildren().add(divider);

        for (Screen screen : IN_GAME_SCREENS) {
            box.getChildren().add(buildNavButton(screen));
        }
        return box;
    }

    private Button buildNavButton(Screen screen) {
        Button button = new Button(screen.getTitle());
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> context.getRouter().navigate(screen));
        sidebarButtons.put(screen, button);
        return button;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14, 24, 14, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        VBox titleBox = new VBox(2, titleLabel, sessionLabel);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().addAll(titleBox, spacer, weekLabel);
        return header;
    }

    private void refreshSidebarSelection(Screen current) {
        for (Map.Entry<Screen, Button> entry : sidebarButtons.entrySet()) {
            Button button = entry.getValue();
            if (entry.getKey() == current) {
                if (!button.getStyleClass().contains("nav-button-active")) {
                    button.getStyleClass().add("nav-button-active");
                }
            } else {
                button.getStyleClass().remove("nav-button-active");
            }
        }
    }

    private void setInGameButtonsEnabled(boolean enabled) {
        for (Screen screen : IN_GAME_SCREENS) {
            Button button = sidebarButtons.get(screen);
            if (button != null) {
                button.setDisable(!enabled);
            }
        }
    }
}
