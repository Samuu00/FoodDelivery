package controller;

import app.Navigator;
import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Cart;
import model.User;
import ui.Icons;
import ui.PasswordInput;
import util.Passwords;
import util.Validators;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Accesso e registrazione nella stessa schermata. Dopo la registrazione l'utente entra direttamente. */
public class AuthController {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @FXML private Label heading, subheading, error, strengthText;
    @FXML private ToggleGroup modeGroup;
    @FXML private ToggleButton loginTab, registerTab;
    @FXML private VBox usernameBox, strengthBox;
    @FXML private HBox strengthBars, errorBox;
    @FXML private TextField username, email;
    @FXML private PasswordInput password;
    @FXML private Hyperlink forgotLink;
    @FXML private Button submit;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        errorBox.getChildren().add(0, Icons.of("alert", 16, "icon-danger"));
        for (int i = 0; i < 4; i++) {
            Region bar = new Region();
            bar.getStyleClass().add("strength-bar");
            HBox.setHgrow(bar, Priority.ALWAYS);
            strengthBars.getChildren().add(bar);
        }
        modeGroup.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) old.setSelected(true); // uno dei due resta sempre attivo
            else render();
        });
        password.textProperty().addListener(o -> { hideError(); updateStrength(); });
        email.textProperty().addListener(o -> hideError());
        username.textProperty().addListener(o -> hideError());
        render();
    }

    private boolean isRegister() { return registerTab.isSelected(); }

    private void render() {
        boolean reg = isRegister();
        heading.setText(reg ? "Crea il tuo account" : "Bentornato!");
        subheading.setText(reg ? "Un minuto e sei pronto a ordinare." : "Accedi per ordinare dai tuoi ristoranti preferiti.");
        usernameBox.setVisible(reg);
        forgotLink.setVisible(!reg);
        strengthBox.setVisible(reg);
        password.setPromptText(reg ? "Almeno 8 caratteri" : "La tua password");
        submit.setText(reg ? "Crea account" : "Accedi");
        hideError();
        updateStrength();
    }

    /** 0-4: lunghezza 8+, lunghezza 12+, contiene una cifra, maiuscole o simboli. */
    static int strength(String p) {
        int s = 0;
        if (p.length() >= 8) s++;
        if (p.length() >= 12) s++;
        if (p.chars().anyMatch(Character::isDigit)) s++;
        if (p.chars().anyMatch(c -> Character.isUpperCase(c) || !Character.isLetterOrDigit(c))) s++;
        return p.length() < 8 ? Math.min(s, 1) : s;
    }

    private void updateStrength() {
        String p = password.getText();
        int s = strength(p);
        for (int i = 0; i < 4; i++) {
            var cls = strengthBars.getChildren().get(i).getStyleClass();
            cls.removeAll("on", "weak");
            if (i < s) cls.add(p.length() < 8 ? "weak" : "on");
        }
        strengthText.setText(p.isEmpty() ? "Almeno 8 caratteri, meglio con un numero."
                : p.length() < 8 ? "Troppo corta: servono almeno 8 caratteri."
                : s >= 3 ? "Ottima." : "Buona. Aggiungi un numero o una maiuscola per renderla più sicura.");
    }

    @FXML
    private void submit() {
        String mail = email.getText().trim();
        String pass = password.getText();
        if (isRegister()) register(username.getText().trim(), mail, pass);
        else login(mail, pass);
    }

    private void login(String mail, String pass) {
        if (mail.isEmpty() || pass.isEmpty()) {
            showError("Inserisci email e password.");
            return;
        }
        try {
            User user = userDAO.authenticate(mail, Passwords.hash(pass));
            if (user == null) {
                showError("Email o password non corretti.");
                password.setInvalid(true);
                return;
            }
            enter(user);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Login fallito", e);
            showError("Impossibile contattare il server. Riprova tra poco.");
        }
    }

    private void register(String user, String mail, String pass) {
        String problem = user.isEmpty() || mail.isEmpty() || pass.isEmpty() ? "Compila tutti i campi."
                : !Validators.email(mail) ? "L'email non sembra valida."
                : pass.length() < 8 ? "La password deve avere almeno 8 caratteri."
                : null;
        if (problem != null) {
            showError(problem);
            return;
        }
        try {
            userDAO.register(user, mail, Passwords.hash(pass));
            enter(new User(user, mail));
        } catch (SQLIntegrityConstraintViolationException dup) {
            showError("Username o email già registrati. Prova ad accedere.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Registrazione fallita", e);
            showError("Impossibile contattare il server. Riprova tra poco.");
        }
    }

    private void enter(User user) {
        Cart.get().setCurrentUser(user);
        Navigator.startApp();
    }

    private void showError(String msg) {
        error.setText(msg);
        errorBox.setVisible(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        password.setInvalid(false);
    }

    @FXML
    private void forgotPassword() {
        new ResetPasswordDialog().showAndWait();
    }

    public static void main(String[] args) {
        assert strength("") == 0;
        assert strength("abc") == 0;
        assert strength("abcdefgh") == 1;
        assert strength("abcdefg1") == 2;
        assert strength("Abcdefghijk1") == 4;
        System.out.println("strength OK");
    }
}
