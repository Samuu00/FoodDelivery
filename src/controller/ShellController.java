package controller;

import ai.AIAgent;
import app.Navigator;
import dao.AddressDAO;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Cart;
import model.User;
import ui.Icons;
import ui.Ui;
import util.Fmt;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShellController {
    private static final Logger LOGGER = Logger.getLogger(ShellController.class.getName());
    /** Pagine che hanno già una ricerca propria o in cui distrarrebbe (checkout). */
    private static final Set<String> NO_SEARCH = Set.of("home", "checkout", "confirmation");

    @FXML private StackPane root;
    @FXML private HBox searchBox;
    @FXML private TextField searchField;
    @FXML private Label aiBadge;
    @FXML private Button addressButton;
    @FXML private Button cartButton;
    @FXML private Button avatarButton;
    @FXML private ScrollPane scroll;
    @FXML private VBox pageHost;
    @FXML private HBox toast;
    @FXML private Label toastText;

    private final InvalidationListener cartListener = o -> refresh();
    private final SequentialTransition toastAnim = new SequentialTransition();
    private final Label cartCount = Ui.label("", "cart-count");
    private final Label cartTotal = Ui.label("", "cart-total");

    @FXML
    private void initialize() {
        searchBox.getChildren().add(0, Icons.of("search", 18));
        aiBadge.setVisible(new AIAgent().isAvailable());
        aiBadge.managedProperty().bind(aiBadge.visibleProperty());
        // la pagina occupa almeno l'altezza visibile (sfondi e footer arrivano in fondo)
        scroll.viewportBoundsProperty().addListener((o, a, b) -> pageHost.setMinHeight(b.getHeight()));

        StackPane cartIcon = new StackPane(Icons.of("cart", 22), cartCount);
        StackPane.setAlignment(cartCount, javafx.geometry.Pos.TOP_RIGHT);
        cartCount.setTranslateX(10);
        cartCount.setTranslateY(-8);
        cartButton.setGraphic(Ui.row(14, cartIcon, cartTotal));

        addressButton.setGraphic(Icons.of("pin", 18, "icon-inherit"));
        addressButton.setContentDisplay(ContentDisplay.LEFT);

        User u = Cart.get().getCurrentUser();
        avatarButton.setText(u.getUsername().substring(0, 1).toUpperCase());
        if (Cart.get().getDeliveryAddress() == null) {
            List<String> addresses = loadAddresses();
            if (!addresses.isEmpty()) Cart.get().setDeliveryAddress(addresses.get(0));
        }
        Cart.get().versionProperty().addListener(new WeakInvalidationListener(cartListener));
        refresh();
    }

    /** Chiamato dal Navigator a ogni cambio pagina. */
    public void show(Node page, String view) {
        pageHost.getChildren().setAll(page);
        VBox.setVgrow(page, Priority.ALWAYS);
        boolean search = !NO_SEARCH.contains(view);
        searchBox.setVisible(search);
        scroll.setVvalue(0);
        page.applyCss();
    }

    /** Scorre la pagina fino al nodo indicato (es. sezione del menu). */
    public void scrollTo(Node node) {
        Bounds b = pageHost.sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        double max = pageHost.getHeight() - scroll.getViewportBounds().getHeight();
        if (max > 0) scroll.setVvalue(Math.min(1, Math.max(0, (b.getMinY() - 16) / max)));
    }

    private void refresh() {
        Cart cart = Cart.get();
        int n = cart.getTotalQuantity();
        cartCount.setText(String.valueOf(n));
        cartCount.setVisible(n > 0);
        cartTotal.setText(Fmt.money(cart.getSubtotal()));
        cartButton.setAccessibleText("Carrello, " + n + " articoli");
        String a = cart.getDeliveryAddress();
        addressButton.setText(a == null ? "Aggiungi indirizzo" : a);
    }

    // ---------- Azioni app bar ----------

    @FXML private void home() { Navigator.home(); }

    @FXML
    private void search() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) return;
        searchField.clear();
        HomeController home = Navigator.home();
        home.search(q);
    }

    @FXML
    private void openCart() {
        if (Cart.get().isEmpty()) {
            toast("Il carrello è vuoto: scegli un ristorante");
            return;
        }
        Navigator.go("checkout");
    }

    @FXML
    private void openAddressMenu() {
        ContextMenu menu = new ContextMenu();
        for (String a : loadAddresses()) {
            CheckMenuItem item = new CheckMenuItem(a);
            item.setSelected(a.equals(Cart.get().getDeliveryAddress()));
            item.setOnAction(e -> Cart.get().setDeliveryAddress(a));
            menu.getItems().add(item);
        }
        if (!menu.getItems().isEmpty()) menu.getItems().add(new SeparatorMenuItem());
        MenuItem manage = new MenuItem("Gestisci indirizzi");
        manage.setOnAction(e -> openAccount("addresses"));
        menu.getItems().add(manage);
        menu.show(addressButton, Side.BOTTOM, 0, 8);
    }

    @FXML
    private void openAccountMenu() {
        User u = Cart.get().getCurrentUser();
        ContextMenu menu = new ContextMenu();
        VBox header = new VBox(2, Ui.label(u.getUsername(), "xb"), Ui.label(u.getEmail(), "muted"));
        CustomMenuItem headerItem = new CustomMenuItem(header, false);
        headerItem.getStyleClass().add("menu-header");
        MenuItem orders = new MenuItem("I miei ordini");
        orders.setOnAction(e -> openAccount("orders"));
        MenuItem addresses = new MenuItem("Indirizzi di consegna");
        addresses.setOnAction(e -> openAccount("addresses"));
        CheckMenuItem dark = new CheckMenuItem("Tema scuro");
        dark.setSelected(Navigator.isDark());
        dark.setOnAction(e -> Navigator.setDark(dark.isSelected()));
        MenuItem logout = new MenuItem("Esci");
        logout.getStyleClass().add("danger");
        logout.setOnAction(e -> {
            Cart.get().setCurrentUser(null);
            Navigator.showFullScreen("auth");
        });
        menu.getItems().addAll(headerItem, new SeparatorMenuItem(), orders, addresses, dark, new SeparatorMenuItem(), logout);
        menu.show(avatarButton, Side.BOTTOM, -196, 8);
    }

    private void openAccount(String tab) {
        AccountController account = Navigator.go("account");
        account.showTab(tab);
    }

    private List<String> loadAddresses() {
        try {
            return new AddressDAO().list(Cart.get().getCurrentUser().getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Indirizzi non caricati", e);
            return List.of();
        }
    }

    // ---------- Toast ----------

    public void toast(String message) {
        toastText.setText(message);
        if (toast.getChildren().size() == 1) toast.getChildren().add(0, Icons.of("check", 18));
        toast.setVisible(true);
        toastAnim.stop();
        toastAnim.getChildren().setAll(fade(0, 1, 150), new PauseTransition(Duration.seconds(2.2)), fade(1, 0, 300));
        toastAnim.setOnFinished(e -> toast.setVisible(false));
        toastAnim.playFromStart();
    }

    private FadeTransition fade(double from, double to, int ms) {
        FadeTransition f = new FadeTransition(Duration.millis(ms), toast);
        f.setFromValue(from);
        f.setToValue(to);
        return f;
    }
}
