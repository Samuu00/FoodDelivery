package Controller;
import Model.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PaymentController extends HomeController implements Initializable {

    @FXML private ComboBox<String> paymentMethodBox;
    @FXML private VBox cardBox, paypalBox;
    @FXML private TextField cardNumberField, expiryField, cvvField, cardHolderField;
    @FXML private TextField paypalEmailField;
    @FXML private PasswordField paypalPasswordField;
    @FXML private Button payButton;
    @FXML private Label amountLabel;
    @FXML private AnchorPane paymentRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentMethodBox.getItems().setAll("Carta di credito", "PayPal", "Contrassegno");

        cardBox.setVisible(false);
        paypalBox.setVisible(false);

        cardBox.setMaxWidth(400);
        paypalBox.setMaxWidth(400);

        paymentMethodBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            cardBox.setVisible("Carta di credito".equals(newVal));
            paypalBox.setVisible("PayPal".equals(newVal));
        });

        payButton.setOnAction(e -> processPayment());

    }

    private boolean isValidCardNumber(String cardNumber) {
        cardNumber = cardNumber.replaceAll("\\s+", "");
        if (!cardNumber.matches("\\d{13,19}")) return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = cardNumber.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean isValidExpiry(String expiry) {
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2,4}")) return false;
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        if (year < 100) year += 2000;

        java.time.YearMonth expDate = java.time.YearMonth.of(year, month);
        java.time.YearMonth now = java.time.YearMonth.now();
        return !expDate.isBefore(now);
    }

    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3,4}");
    }

    private boolean isValidCardHolder(String name) {
        return name.matches("[A-Za-zÀ-ÖØ-öø-ÿ ]{3,}");
    }

    public void setAmount(double amount) {
        amountLabel.setText("Totale: € " + String.format("%.2f", amount));
    }

    private void processPayment() {
        String method = paymentMethodBox.getValue();
        if (method == null || method.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona un metodo di pagamento.");
            return;
        }

        if ("Carta di credito".equals(method)) {
            String cardNumber = cardNumberField.getText();
            String expiry = expiryField.getText();
            String cvv = cvvField.getText();
            String cardHolder = cardHolderField.getText();

            if (cardNumber.isBlank() || expiry.isBlank() || cvv.isBlank() || cardHolder.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Compila tutti i campi della carta di credito.");
                return;
            }
            if (!isValidCardNumber(cardNumber)) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Numero carta non valido.");
                return;
            }
            if (!isValidExpiry(expiry)) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Data di scadenza non valida.");
                return;
            }
            if (!isValidCVV(cvv)) {
                showAlert(Alert.AlertType.ERROR, "Errore", "CVV non valido.");
                return;
            }
            if (!isValidCardHolder(cardHolder)) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Nome intestatario non valido.");
                return;
            }
        }

        if ("PayPal".equals(method)) {
            if (paypalEmailField.getText().isBlank() || paypalPasswordField.getText().isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci email e password PayPal.");
                return;
            }
            String email = paypalEmailField.getText().trim();
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Email non valida");
                return;
            }
        }

        User user = Cart.getInstance().getCurrentUser();
        Cart cart = Cart.getInstance();

        try {
            OrderHistoryController orderHistoryController = new OrderHistoryController();
            orderHistoryController.salvaOrdine(user, cart);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile salvare l'ordine.");
            return;
        }

        cart.clear();
        showAlert(Alert.AlertType.INFORMATION, "Grazie!",
                "Pagamento " + amountLabel.getText() + " completato con " + method + ".\n\nGrazie per il tuo acquisto!");

        goBackHome();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/home.fxml"));
            Stage stage = (Stage) paymentRoot.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());
            stage.setScene(scene);
            SceneManager.clearStack();
            SceneManager.pushScene(scene, loader.getController(), "home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
