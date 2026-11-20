package app;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String[] FONTS = {
            "Fredoka-SemiBold.ttf", "Fredoka-Bold.ttf",
            "NunitoSans-Regular.ttf", "NunitoSans-Bold.ttf", "NunitoSans-ExtraBold.ttf"
    };

    @Override
    public void start(Stage stage) {
        for (String f : FONTS) Font.loadFont(Main.class.getResourceAsStream("/fonts/" + f), 14);
        stage.setTitle("Food Delivery");
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/IconaAPP.png")));
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        Navigator.init(stage);
        Navigator.showFullScreen("splash");
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
