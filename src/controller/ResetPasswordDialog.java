package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ui.PasswordInput;
import ui.Ui;
import util.Passwords;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reset password in un unico dialog (prima: due TextInputDialog con la password in chiaro).
 * ponytail: basta lo username per cambiare password, come nel progetto originale; in produzione serve un link via email.
 */
public class ResetPasswordDialog extends Dialog<Void> {
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordDialog.class.getName());

    public ResetPasswordDialog() {
        Ui.styled(this);
        setTitle("Recupera password");
        setHeaderText("Imposta una nuova password");

        TextField username = new TextField();
        username.setPromptText("Il tuo username");
        PasswordInput pass = new PasswordInput();
        pass.setPromptText("Almeno 8 caratteri");
        PasswordInput confirm = new PasswordInput();
        confirm.setPromptText("Ripeti la password");
        Label error = Ui.label("", "error-text");
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);

        VBox form = new VBox(8,
                Ui.label("Username", "field-label"), username,
                Ui.label("Nuova password", "field-label"), pass,
                Ui.label("Conferma password", "field-label"), confirm, error);
        form.setPrefWidth(360);
        getDialogPane().setContent(form);

        ButtonType save = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, save);
        getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("button-ghost");

        // valida senza chiudere il dialog
        getDialogPane().lookupButton(save).addEventFilter(ActionEvent.ACTION, e -> {
            String msg = username.getText().isBlank() ? "Inserisci lo username."
                    : pass.getText().length() < 8 ? "La password deve avere almeno 8 caratteri."
                    : !pass.getText().equals(confirm.getText()) ? "Le due password non coincidono."
                    : update(username.getText().trim(), pass.getText());
            if (msg != null) {
                error.setText(msg);
                error.setVisible(true);
                e.consume();
            }
        });
        setOnShown(e -> username.requestFocus());
    }

    /** @return messaggio d'errore, o null se ok */
    private String update(String username, String password) {
        try {
            if (!new UserDAO().updatePassword(username, Passwords.hash(password))) return "Utente non trovato.";
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Reset password fallito", ex);
            return "Impossibile contattare il server.";
        }
        Alert ok = Ui.styled(new Alert(Alert.AlertType.INFORMATION, "Ora puoi accedere con la nuova password."));
        ok.setHeaderText("Password aggiornata");
        ok.show();
        return null;
    }
}
