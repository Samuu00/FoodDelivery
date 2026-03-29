package Controller;
import Model.*;

import DatabaseConn.DatabaseConnection;
import DAO.AddressDAO;
import DAO.CouponDAO;
import Model.Cart;
import Model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutController extends HomeController {
    private static final Logger LOGGER = Logger.getLogger(CheckoutController.class.getName());
    private final CouponDAO couponDAO = new CouponDAO();

    @FXML private VBox orderSummaryBox;
    @FXML private ComboBox<String> addressComboBox;
    @FXML private TextField couponField;
    @FXML private Label totalPriceLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        populateOrderSummary(orderSummaryBox);
        double total = Cart.getInstance().getDiscountedTotal();
        totalPriceLabel.setText("€ " + String.format("%.2f", total));
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserAddresses();
    }


    private void loadUserAddresses() {
        if (currentUser == null) return;

        try {
            AddressDAO dao = new AddressDAO(DatabaseConnection.getConnection());
            List<String> addresses = dao.getAddresses(currentUser.getUsername());
            addressComboBox.getItems().setAll(addresses);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento indirizzi", e);
            showAlert("Errore DB", "Impossibile caricare gli indirizzi: " + e.getMessage());
        }
    }

    @FXML
    private void applyCoupon() {
        if (currentUser == null) return;

        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            showAlert("Errore", "Inserisci un codice coupon valido.");
            return;
        }

        try {
            double discountPercent = couponDAO.getDiscountPercentage(code, currentUser.getUsername());
            
            if (discountPercent >= 0) {
                double total = calculateCartTotal();
                double discountedTotal = total - (total * discountPercent / 100);

                Cart.getInstance().setDiscountedTotal(discountedTotal);
                totalPriceLabel.setText("€ " + String.format("%.2f", discountedTotal));
            } else {
                showAlert("Errore", "Coupon non valido o non disponibile per questo utente.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore DB Coupon", e);
            showAlert("Errore DB", e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public double calculateCartTotal() {
        return Cart.getInstance().getItems().stream()
                .mapToDouble(item -> item.price * item.quantity)
                .sum();
    }

    public void setAmount(double total) {
        totalPriceLabel.setText("€ " + String.format("%.2f", Cart.getInstance().getDiscountedTotal()));
    }

    public void populateOrderSummary(VBox orderSummaryBox) {
        orderSummaryBox.getChildren().clear();
        for (Cart.Item item : Cart.getInstance().getItems()) {
            Label lbl = new Label(item.name + " x" + item.quantity + " - €" + String.format("%.2f", item.price)
                    + " tot: €" + String.format("%.2f", item.price * item.quantity));
            orderSummaryBox.getChildren().add(lbl);
        }
    }

    @FXML
    private void handleProceedToPayment() {
        User user = Cart.getInstance().getCurrentUser();
        Cart cart = Cart.getInstance();
        String address = addressComboBox.getValue();

        if (user == null || cart.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Errore");
            alert.setHeaderText("Ordine non valido");
            alert.setContentText("Assicurati di aver effettuato il login e di avere articoli nel carrello.");
            alert.showAndWait();
            return;
        }

        if(address == null || address.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Errore");
            alert.setHeaderText("Indirizzo non selezionato");
            alert.setContentText("Seleziona un indirizzo di consegna prima di procedere al pagamento.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/payment.fxml"));
            Scene scene = new Scene(loader.load());
            PaymentController controller = loader.getController();
            SceneManager.pushScene(scene, controller, "payment");
            OrderHistoryController orderController = new OrderHistoryController();
            controller.setAmount(Cart.getInstance().getDiscountedTotal());

            refreshCartUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleAddNewAddress() {
        if (currentUser == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuovo indirizzo");
        dialog.setHeaderText("Aggiungi un nuovo indirizzo di consegna");
        dialog.setContentText("Inserisci indirizzo:");

        dialog.showAndWait().ifPresent(address -> {
            try {
                AddressDAO dao = new AddressDAO(DatabaseConnection.getConnection());
                dao.addAddress(currentUser.getUsername(), address);
                loadUserAddresses();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Errore inserimento indirizzo", e);
                showAlert("Errore DB", "Impossibile aggiungere indirizzo: " + e.getMessage());
            }
        });
    }


    public void refreshAddresses() {
        if (currentUser == null) return;
        try {
            AddressDAO dao = new AddressDAO(DatabaseConnection.getConnection());
            addressComboBox.getItems().setAll(dao.getAddresses(currentUser.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
