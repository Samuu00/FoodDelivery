package Controller;
import Model.*;

import DAO.UserDAO;
import Utils.SecurityUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;


public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwField;
    @FXML private Label statusLabel;
    @FXML private ResetPass reset = new ResetPass();

    @FXML
    private TextField passwordVisibleField;
    @FXML
    private Button toggleButton;
    @FXML
    private ImageView toggleIcon;

    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwField.textProperty());
        toggleIcon.setImage(new Image(getClass().getResourceAsStream("/Image/eye-open.png")));
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwField.setVisible(false);
            passwField.setManaged(false);
            toggleIcon.setImage(new Image(getClass().getResourceAsStream("/Image/eye-closed.png")));
        } else {
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwField.setVisible(true);
            passwField.setManaged(true);
            toggleIcon.setImage(new Image(getClass().getResourceAsStream("/Image/eye-open.png")));
        }
    }

    public void handleResetPassword(ActionEvent event) {
        reset.resetPasswordWithDialogs();
    }

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin(){
        String email = emailField.getText();
        String password = passwField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Compila tutti i campi!");
            return;
        }

        String hashedPass = SecurityUtils.hashPassword(password);

        try {
            User loggedUser = userDAO.authenticate(email, hashedPass);

            if (loggedUser != null) {
                statusLabel.setText("Login avvenuto con successo");

                Cart.getInstance().setCurrentUser(loggedUser);

                SceneManager.clearStack();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/home.fxml"));
                Scene homeScene = new Scene(loader.load());
                homeScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

                Stage stage = (Stage) emailField.getScene().getWindow();
                SceneManager.setStage(stage);
                SceneManager.pushScene(homeScene, loader.getController(), "home");
            }else{
                statusLabel.setText("Credenziali errate");
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Errore DB: ", e);
            statusLabel.setText("Errore DB: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore di I/O nel caricare la pagina home", e);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void openRegisterPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/register.fxml"));
            Scene registerScene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            registerScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());
            stage.setScene(registerScene);
            stage.centerOnScreen();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.setTitle("Benvenuto su Food Delivery");

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
