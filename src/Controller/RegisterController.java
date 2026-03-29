package Controller;
import Model.*;

import DAO.UserDAO;
import Utils.SecurityUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;


public class RegisterController {
    @FXML TextField usernameField;
    @FXML TextField emailField;
    @FXML PasswordField passwField;
    @FXML PasswordField confirmPasswField;
    @FXML Label statusLabel;

    @FXML private TextField passwordVisibleField;
    @FXML private Button toggleButton;
    @FXML private ImageView toggleIcon;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button toggleConfirmButton;
    @FXML private ImageView confirmToggleIcon;

    @FXML
    public void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwField.textProperty());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        toggleIcon.setImage(new Image(getClass().getResourceAsStream("/Image/eye-open.png")));

        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswField.textProperty());
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);
        confirmToggleIcon.setImage(new Image(getClass().getResourceAsStream("/Image/eye-open.png")));
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean showing = passwordVisibleField.isVisible();
        passwordVisibleField.setVisible(!showing);
        passwordVisibleField.setManaged(!showing);
        passwField.setVisible(showing);
        passwField.setManaged(showing);

        String iconPath = showing ? "/Image/eye-open.png" : "/Image/eye-closed.png";
        toggleIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        boolean showing = confirmPasswordVisibleField.isVisible();
        confirmPasswordVisibleField.setVisible(!showing);
        confirmPasswordVisibleField.setManaged(!showing);
        confirmPasswField.setVisible(showing);
        confirmPasswField.setManaged(showing);

        String iconPath = showing ? "/Image/eye-open.png" : "/Image/eye-closed.png";
        confirmToggleIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
    }


    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handlerRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwField.getText().trim();
        String confirmPassw = confirmPasswField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Compila tutti i campi!");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            statusLabel.setText("Email non valida");
            return;
        }

        if(password.length() < 8){
            statusLabel.setText("La password deve essere di minimo 8 caratteri");
            return;
        }
        if(!password.equals(confirmPassw)){
            statusLabel.setText("Le password non corrispondono");
            return;
        }

        String hashedPass = SecurityUtils.hashPassword(password);

        try {
            userDAO.registerUser(username, email, hashedPass);

            statusLabel.setText("Registrazione completata!");
            LOGGER.info("Registrazione completata per l'utente: " + username);

            goToLogin();

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Errore registrazione", e);
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("username") || e.getMessage().toLowerCase().contains("email") || e.getMessage().toLowerCase().contains("duplicate"))) {
                statusLabel.setText("Username o email già registrati!");
            } else {
                statusLabel.setText("Errore DB: " + e.getMessage());
            }
        }
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            loginScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(loginScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
            Parent loginRoot = loader.load();

            Scene loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(loginScene);
            stage.setTitle("Benvenuto su Food Delivery");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
