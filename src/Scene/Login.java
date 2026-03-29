package Scene;
import Model.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Login extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/splashScreen.fxml"));
        Parent splashRoot = loader.load();
        Scene splashScene = new Scene(splashRoot);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Image/IconaAPP.png")));

        stage.setScene(splashScene);
        stage.setTitle("Benvenuto su Food Delivery");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
