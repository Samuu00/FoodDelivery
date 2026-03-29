package Controller;
import Model.*;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class SplashController {

    @FXML
    private ImageView icona;

    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), icona);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);

        fade.setOnFinished(e -> {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
                Parent loginRoot = loader.load();
                Scene loginScene = new Scene(loginRoot);

                loginScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

                Stage stage = (Stage) icona.getScene().getWindow();

                stage.setScene(loginScene);

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), loginRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        fade.play();
    }
}
