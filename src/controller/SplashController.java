package controller;

import app.Navigator;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SplashController {
    @FXML private ImageView icon;

    @FXML
    private void initialize() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), icon);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.setOnFinished(e -> Navigator.showFullScreen("auth"));
        fade.play();
    }
}
