package Controller;
import Model.*;

import AI.AIAgent;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.Serial;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController implements SceneManager.SceneController {
    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    @FXML private TextField searchField;
    @FXML private VBox resultBox;
    @FXML private AnchorPane cartPreviewPane;
    @FXML private VBox cartItemsBox;
    @FXML private Label cartCountLabel;
    @FXML private Button checkoutButton;
    @FXML private AnchorPane cartOverlayPane;
    @FXML private AnchorPane sideMenu;
    @FXML private AnchorPane overlayPane;
    @FXML private VBox pizzeriaBox;
    @FXML private VBox sushiBox;
    @FXML private VBox paniniBox;
    @FXML private VBox dolciBox;
    private boolean darkMode = false;

    private boolean menuOpen = false;
    private boolean cartOpen = false;

    private final AIAgent aiAgent = new AIAgent();
    private int count;

    private User currentUser;

    @FXML
    public void initialize() {
        if (searchField != null) searchField.setOnAction(e -> handleSearch());

        if (checkoutButton != null) checkoutButton.setOnAction(e -> goToCheckOut());

        if (cartOverlayPane != null) cartOverlayPane.setOnMouseClicked(e -> closeCart());

        if (pizzeriaBox != null) pizzeriaBox.setOnMouseClicked(e -> openCategory("Pizza"));

        if (sushiBox != null) sushiBox.setOnMouseClicked(e -> openCategory("Sushi"));

        if (paniniBox != null) paniniBox.setOnMouseClicked(e -> openCategory("Panini"));

        if (dolciBox != null) dolciBox.setOnMouseClicked(e -> openCategory("Dolci"));

        refreshCartUI();

    }

    @Override
    public void onSceneShown() {
        refreshCartUI();
    }

    public VBox getCartItemsBox(){
        return cartItemsBox;
    }
    public Label getCartCountLabel(){
        return cartCountLabel;
    }

    public void addToCart(String nomePiatto, double prezzoUnitario, int MenuID) {
        Cart.getInstance().add(nomePiatto, prezzoUnitario, MenuID);
        refreshCartUI();
    }


    public void refreshCartCount() {
        updateCartCount(Cart.getInstance().getTotalQuantity());
    }

    public void removeFromCart(String nomePiatto) {
        Cart.getInstance().remove(nomePiatto);
        refreshCartUI();
    }

    public void refreshCartUI() {
        if (cartItemsBox == null) return;

        cartItemsBox.getChildren().clear();
        for (Cart.Item item : Cart.getInstance().getItems()) {
            HBox itemBox = new HBox(10);
            Label lbl = new Label(item.name + " x" + item.quantity
                    + " - €" + String.format("%.2f", item.price)
                    + " tot: €" + String.format("%.2f", item.price * item.quantity));

            Button addBtn = new Button("+");
            addBtn.getStyleClass().add("quantity-btn");

            Button removeBtn = new Button("-");
            removeBtn.getStyleClass().add("quantity-btn");

            final String itemName = item.name;
            final double itemPrice = item.price;
            final int itemID = item.menuId;

            addBtn.setOnAction(e -> {
                addToCart(itemName, itemPrice, itemID);
                refreshCartUI();
            });


            removeBtn.setOnAction(e -> {
                removeFromCart(itemName);
                refreshCartUI();
            });

            itemBox.getChildren().addAll(lbl, addBtn, removeBtn);
            cartItemsBox.getChildren().add(itemBox);
        }
        updateCartCount(Cart.getInstance().getTotalQuantity());
    }




    private void openCategory(String categoria){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ResultPage.fxml"));
            Scene scene = new Scene(loader.load());
            SearchResultController controller = loader.getController();

            controller.setCategory(categoria);

            SceneManager.pushScene(scene, controller, "result");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToOrderHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/OrderHistory.fxml"));
            Scene scene = new Scene(loader.load());
            OrderHistoryController controller = loader.getController();

            Stage stage = (Stage) cartCountLabel.getScene().getWindow();
            SceneManager.pushScene(scene, controller, "orderHistory");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCheckOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Checkout.fxml"));
            Scene scene = new Scene(loader.load());
            CheckoutController controller = loader.getController();

            controller.setAmount(Cart.getInstance().calculateCartTotal());
            controller.setCurrentUser(Cart.getInstance().getCurrentUser());

            SceneManager.pushScene(scene, controller, "checkOut");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML private ComboBox<String> addressComboBox;
    @FXML private VBox orderSummaryBox;


    @FXML
    private void handleProceedToPayment() {
        if (addressComboBox.getValue() == null || addressComboBox.getValue().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText("Nessun indirizzo selezionato");
            alert.setContentText("Seleziona un indirizzo di consegna prima di procedere al pagamento.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/payment.fxml"));
            Scene scene = new Scene(loader.load());
            PaymentController controller = loader.getController();
            SceneManager.pushScene(scene, controller, "payment");

            controller.setAmount(Cart.getInstance().calculateCartTotal());


            Stage stage = (Stage) orderSummaryBox.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String userInput = searchField.getText().trim();
        if (userInput.isEmpty()) return;

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return aiAgent.generateSQL(userInput);
            }
        };

        aiTask.setOnSucceeded(e -> {
            String sqlQuery = aiTask.getValue();
            if (sqlQuery == null || !sqlQuery.toLowerCase().startsWith("select")) {
                sqlQuery = "SELECT m.NomePiatto, m.Prezzo, r.Nome FROM Menu m JOIN Ristoranti r ON m.RistoranteID = r.RistoranteID LIMIT 10;";
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ResultPage.fxml"));
                Scene scene = new Scene(loader.load());
                SearchResultController controller = loader.getController();
                SceneManager.pushScene(scene, controller, "result");

                controller.setSQLQuery(sqlQuery);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        aiTask.setOnFailed(e -> LOGGER.log(Level.SEVERE, "Errore durante processamento AI.", aiTask.getException()));
        new Thread(aiTask).start();
    }


    public void updateCartCount(int count) {
        cartCountLabel.setText(String.valueOf(count));
    }

    @FXML
    private void toggleMenu() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), sideMenu);
        if (!menuOpen) {
            overlayPane.setVisible(true);
            tt.setToX(0);
            menuOpen = true;
        } else {
            tt.setToX(250);
            tt.setOnFinished(e -> overlayPane.setVisible(false));
            menuOpen = false;
        }
        tt.play();
    }

    @FXML
    private void closeMenu() {
        if (menuOpen) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), sideMenu);
            tt.setToX(250);
            tt.setOnFinished(e -> overlayPane.setVisible(false));
            tt.play();
            menuOpen = false;
        }
    }

    @FXML
    private void toggleCartPreview() {
        refreshCartUI();
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), cartPreviewPane);
        if (cartOpen) {
            transition.setToX(cartPreviewPane.getWidth());
            transition.setOnFinished(e -> {
                cartPreviewPane.setVisible(false);
                cartOverlayPane.setVisible(false);
            });
            cartOpen = false;
        } else {
            cartOverlayPane.setVisible(true);
            cartPreviewPane.setVisible(true);
            cartPreviewPane.setTranslateX(cartPreviewPane.getWidth());
            transition.setToX(0);
            cartOpen = true;
        }
        transition.play();
    }

    @FXML
    private void closeCart() {
        if (cartOpen) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(300), cartPreviewPane);
            transition.setToX(cartPreviewPane.getWidth());
            transition.setOnFinished(e -> {
                cartPreviewPane.setVisible(false);
                cartOverlayPane.setVisible(false);
            });
            transition.play();
            cartOpen = false;
        }
    }

    @FXML
    private void goBackHome(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/home.fxml"));
            Scene homeScene = new Scene(loader.load());
            homeScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

            HomeController controller = loader.getController();
            refreshCartUI();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(homeScene);
            stage.centerOnScreen();
            SceneManager.clearStack();
            SceneManager.pushScene(homeScene, controller, "home");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Scene previousScene;
    private Stage stage;

    public void setPreviousScene(Stage stage, Scene scene) {
        this.stage = stage;
        this.previousScene = scene;
    }

    @FXML
    private void goBack() {
        SceneManager.goBack();
    }


    @FXML
    private void logOut(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            loginScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

            Stage stage = (Stage)((Label)event.getSource()).getScene().getWindow();

            SceneManager.setStage(stage);
            SceneManager.clearStack();
            stage.setUserData(null);

            stage.setScene(loginScene);
            stage.centerOnScreen();

            Cart.getInstance().setCurrentUser(null);

            if (loader.getController() instanceof SceneManager.SceneController controller) {
                controller.onSceneShown();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @FXML
    private void menuAccount(javafx.scene.input.MouseEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserProfile.fxml"));
        Scene accountScene = new Scene(loader.load());
        accountScene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());

        UserProfileController controller = loader.getController();
        controller.setStage((Stage)((Label)event.getSource()).getScene().getWindow());
        controller.loadUserData();
        refreshCartUI();


        SceneManager.pushScene(accountScene, loader.getController(), "account");
    }

}
