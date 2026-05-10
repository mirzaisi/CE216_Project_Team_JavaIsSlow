package com.playforgemanager.ui;

import javafx.scene.image.Image;

public final class UiAssets {
    public static final String LOGO_PATH = "/assets/images/playforge-logo.png";

    private UiAssets() {
    }

    public static Image loadLogo() {
        var url = UiAssets.class.getResource(LOGO_PATH);
        return url == null ? null : new Image(url.toExternalForm());
    }
}
