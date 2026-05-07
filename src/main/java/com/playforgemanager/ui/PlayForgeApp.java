package com.playforgemanager.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlayForgeApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppContext context = new AppContext(primaryStage);
        MainShell shell = new MainShell(context);
        ScreenRouter router = new ScreenRouter(context, shell);
        context.setRouter(router);

        Scene scene = new Scene(shell.getRoot(), 1200, 760);
        var cssUrl = PlayForgeApp.class.getResource("/css/playforge.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("PlayForge Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();

        router.navigate(Screen.HOME);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
