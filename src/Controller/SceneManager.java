package Controller;
import Model.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SceneManager {

    private static Stage mainStage;
    private static final Stack<SceneControllerPair> sceneStack = new Stack<>();

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public interface SceneController {
        void onSceneShown();
    }

    private static class SceneControllerPair {
        Scene scene;
        SceneController controller;
        String sceneId;

        SceneControllerPair(Scene scene, SceneController controller, String sceneId) {
            this.scene = scene;
            this.controller = controller;
            this.sceneId = sceneId;
        }
    }

    private static class SceneInfo {
        SceneController controller;
        String sceneId;

        SceneInfo(SceneController controller, String sceneId) {
            this.controller = controller;
            this.sceneId = sceneId;
        }
    }

    public static void pushScene(Scene scene, SceneController controller, String sceneId) {
        SceneInfo currentInfo = (SceneInfo) mainStage.getUserData();

        if (currentInfo != null && sceneId.equals(currentInfo.sceneId)) {
            return;
        }

        Scene currentScene = mainStage.getScene();
        if (currentScene != null) {
            sceneStack.push(new SceneControllerPair(currentScene,
                    currentInfo != null ? currentInfo.controller : null,
                    currentInfo != null ? currentInfo.sceneId : null));
        }

        mainStage.setScene(scene);
        mainStage.setUserData(new SceneInfo(controller, sceneId));
        if (controller != null) controller.onSceneShown();
    }

    public static void goBack() {
        if (!sceneStack.isEmpty()) {
            SceneControllerPair pair = sceneStack.pop();
            mainStage.setScene(pair.scene);
            mainStage.setUserData(new SceneInfo(pair.controller, pair.sceneId));
            if (pair.controller != null) pair.controller.onSceneShown();
        }
    }

    public static void clearStack() {
        sceneStack.clear();
    }

}
