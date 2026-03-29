package Controller;
import Model.*;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import DAO.UserDAO;
import Utils.SecurityUtils;

public class ResetPass {
    private static final Logger LOGGER = Logger.getLogger(ResetPass.class.getName());
    private final UserDAO userDAO = new UserDAO();

    public boolean userExists(String username) {
        try {
            return userDAO.userExists(username);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante userExists", e);
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        try {
            String hashedPass = SecurityUtils.hashPassword(newPassword);
            if (hashedPass == null) {
                LOGGER.warning("Hash della password fallito");
                return false;
            }
            return userDAO.updatePassword(username, hashedPass);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante updatePassword", e);
        }
        return false;
    }

    public void resetPasswordWithDialogs() {
        TextInputDialog askUser = new TextInputDialog();
        askUser.setTitle("Reset Password");
        askUser.setHeaderText("Inserisci il tuo username");
        askUser.setContentText("Username:");

        askUser.showAndWait().ifPresent(username -> {
            if (userExists(username)) {
                TextInputDialog askNewPass = new TextInputDialog();
                askNewPass.setTitle("Reset Password");
                askNewPass.setHeaderText("Inserisci la nuova password");
                askNewPass.setContentText("Nuova Password:");

                askNewPass.showAndWait().ifPresent(newPass -> {
                    if (newPass.trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Errore");
                        alert.setContentText("La password non può essere vuota!");
                        alert.showAndWait();
                        return;
                    }
                    boolean success = updatePassword(username, newPass);
                    Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    alert.setTitle("Reset Password");
                    alert.setContentText(success ? "Password aggiornata con successo!" : "Errore durante l'aggiornamento.");
                    alert.showAndWait();
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setContentText("Utente non trovato!");
                alert.showAndWait();
            }
        });
    }
}
