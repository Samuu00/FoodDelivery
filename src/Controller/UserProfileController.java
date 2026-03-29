package Controller;
import Model.*;

import Model.Cart;
import Model.User;
import DAO.AddressDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserProfileController extends HomeController {
    private static final Logger LOGGER = Logger.getLogger(UserProfileController.class.getName());

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private VBox addressBox;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loadUserData() {
        User currentUser = Cart.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            emailLabel.setText(currentUser.getEmail());

            try {
                AddressDAO dao = new AddressDAO(DatabaseConn.DatabaseConnection.getConnection());
                List<String> addresses = dao.getAddresses(currentUser.getUsername());
                addressBox.getChildren().clear();
                for (String addr : addresses) {
                    addressBox.getChildren().add(new Label(addr));
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Errore durante il caricamento degli indirizzi", e);
            }
        }
    }

    @FXML
    private void addAddress() {
        TextField newAddressField = new TextField();
        newAddressField.setPromptText("Inserisci nuovo indirizzo");
        addressBox.getChildren().add(newAddressField);

        newAddressField.setOnAction(e -> {
            String address = newAddressField.getText().trim();
            if (!address.isEmpty() && Cart.getInstance().getCurrentUser() != null) {
                try {
                    AddressDAO dao = new AddressDAO(DatabaseConn.DatabaseConnection.getConnection());
                    dao.addAddress(Cart.getInstance().getCurrentUser().getUsername(), address);
                    loadUserData();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Errore salvataggio nuovo indirizzo", ex);
                }
            }
        });
    }

}
