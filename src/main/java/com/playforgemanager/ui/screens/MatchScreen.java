package com.playforgemanager.ui.screens;

import com.playforgemanager.application.FixtureSummaryView;
import com.playforgemanager.application.MatchProcessingResult;
import com.playforgemanager.application.TeamOverviewView;
import com.playforgemanager.application.WeekProgressionResult;
import com.playforgemanager.application.setup.TeamSetupService;
import com.playforgemanager.application.setup.TeamSetupView;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.ui.AppContext;
import com.playforgemanager.ui.Screen;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MatchScreen implements UiScreen {
    private final AppContext context;

    public MatchScreen(AppContext context) {
        this.context = context;
    }

    @Override
    public Region render() {
        if (!context.getUiSession().hasSession()) {
            return NoSessionView.build(context);
        }

        GameSession session = context.getUiSession().getActiveSession();
        if (session.getCurrentSeason().isCompleted()) {
            return seasonCompletedView();
        }

        TeamOverviewView overview = context.getTeamOverviewQueryService().build(session);
        FixtureSummaryView next = overview.nextFixture();
        TeamSetupService setupService = context.getTeamSetupService();
        TeamSetupView setup = setupService.buildView(session);

        Label header = new Label("Next Match — Week " + session.getCurrentSeason().getCurrentWeek());
        header.getStyleClass().add("section-title");
        Label subtitle = new Label(next == null
                ? "No upcoming match this week."
                : String.format("%s vs %s — %s",
                        next.homeTeamName(), next.awayTeamName(),
                        next.controlledTeamHome() ? "you are home" : "you are away"));
        subtitle.getStyleClass().add("section-subtitle");

        Label tacticInfo = new Label("Tactic: " + (setup.selectedTacticName() == null ? "—" : setup.selectedTacticName()));
        Label lineupInfo = new Label(String.format(
                "Lineup: %d / %d starters%s",
                setup.startingLineup().size(),
                setup.requiredStarters(),
                setup.lineupValid() ? "" : " — " + setup.lineupValidationMessage()));
        lineupInfo.getStyleClass().add(setup.lineupValid() ? "status-ok" : "status-warning");

        HBox prepActions = new HBox(10);
        Button toTactics = new Button("Open Tactics & Lineup");
        toTactics.getStyleClass().add("button-secondary");
        toTactics.setOnAction(e -> context.getRouter().navigate(Screen.TACTICS_LINEUP));
        Button autoPick = new Button("Auto-pick lineup");
        autoPick.getStyleClass().add("button-secondary");
        autoPick.setOnAction(e -> {
            try {
                setupService.autoPickLineup(session);
                context.getRouter().navigate(Screen.MATCH);
            } catch (RuntimeException ex) {
                UiAlerts.error("Could not auto-pick lineup", ex.getMessage());
            }
        });
        prepActions.getChildren().addAll(toTactics, autoPick);

        Button playMatch = new Button("Play match");
        playMatch.getStyleClass().add("button-primary");
        playMatch.setPrefHeight(48);
        playMatch.setMaxWidth(280);
        playMatch.setDisable(!setup.lineupValid() || next == null);
        playMatch.setOnAction(e -> playControlledMatch(session));

        Button advanceWeek = new Button("Advance week (let AI play)");
        advanceWeek.getStyleClass().add("button-secondary");
        advanceWeek.setPrefHeight(40);
        advanceWeek.setMaxWidth(280);
        advanceWeek.setOnAction(e -> advanceWeek(session));

        VBox card = new VBox(14,
                header, subtitle,
                new Label(" "),
                tacticInfo, lineupInfo,
                prepActions,
                new Label(" "),
                playMatch, advanceWeek);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }

    private void playControlledMatch(GameSession session) {
        TeamSetupService setupService = context.getTeamSetupService();
        Lineup lineup = setupService.currentLineup(session);
        Tactic tactic = setupService.currentTactic(session);
        if (lineup == null || tactic == null) {
            UiAlerts.error("Setup incomplete", "Pick a lineup and a tactic before starting the match.");
            return;
        }
        try {
            MatchProcessingResult result = context.getMatchProcessingService()
                    .playControlledMatch(session, lineup, tactic);
            context.getUiSession().setLastMatchResult(result);
            context.getRouter().navigate(Screen.POST_MATCH);
        } catch (RuntimeException ex) {
            UiAlerts.error("Match failed", ex.getMessage());
        }
    }

    private void advanceWeek(GameSession session) {
        try {
            WeekProgressionResult result = context.getWeekProgressionService().advanceOneStep(session);
            context.getUiSession().setLastWeekResult(result);
            context.getRouter().navigate(Screen.POST_WEEK);
        } catch (RuntimeException ex) {
            UiAlerts.error("Could not advance week", ex.getMessage());
        }
    }

    private Region seasonCompletedView() {
        Label header = new Label("Season completed");
        header.getStyleClass().add("section-title");
        Label info = new Label("All fixtures have been played. Visit the league table for final standings.");
        info.getStyleClass().add("section-subtitle");
        Button standings = new Button("View League Table");
        standings.getStyleClass().add("button-primary");
        standings.setOnAction(e -> context.getRouter().navigate(Screen.STANDINGS));

        VBox card = new VBox(12, header, info, standings);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.getStyleClass().add("card");
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(28));
        return wrapper;
    }
}
