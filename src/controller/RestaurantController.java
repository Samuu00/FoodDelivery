package controller;

import app.Navigator;
import dao.MenuDAO;
import dao.RestaurantDAO;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import model.Cart;
import model.MenuItem;
import model.Restaurant;
import ui.Icons;
import ui.Ui;
import util.Fmt;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestaurantController implements Navigator.Page {
    private static final Logger LOGGER = Logger.getLogger(RestaurantController.class.getName());

    @FXML private Button backLink;
    @FXML private StackPane logo;
    @FXML private Label name, description;
    @FXML private HBox meta, tabs;
    @FXML private VBox menu, cartPanel;

    private final InvalidationListener cartListener = o -> renderCart();
    private Restaurant restaurant;

    @FXML
    private void initialize() {
        backLink.setGraphic(Icons.of("chevron-left", 16, "icon-inherit"));
        backLink.setText("Indietro");
        Cart.get().versionProperty().addListener(new WeakInvalidationListener(cartListener));
    }

    @Override
    public void onShow() { renderCart(); }

    public void load(int restaurantId) {
        try {
            Restaurant r = new RestaurantDAO().byId(restaurantId);
            if (r != null) load(r);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ristorante non caricato", e);
            Ui.error("Errore", "Impossibile aprire il ristorante. Riprova tra poco.");
        }
    }

    public void load(Restaurant r) {
        restaurant = r;
        name.setText(r.name());
        description.setText(r.description());
        logo.getChildren().setAll(Ui.logoBadge(r.image(), 128));
        Label address = Ui.label(r.address(), "muted");
        address.setGraphic(Icons.of("pin", 16, "icon-muted"));
        Label free = Ui.label("Consegna gratuita", "success-text");
        free.setGraphic(Icons.of("check", 16, "icon-success"));
        meta.getChildren().setAll(address, free);
        try {
            renderMenu(new MenuDAO().byRestaurant(r.id()));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Menu non caricato", e);
            menu.getChildren().setAll(Ui.emptyState("alert", "Menu non disponibile", "Riprova tra poco.", null));
        }
        renderCart();
    }

    private void renderMenu(List<MenuItem> items) {
        if (items.isEmpty()) {
            menu.getChildren().setAll(Ui.emptyState("bag", "Menu in arrivo", "Questo ristorante non ha ancora piatti.", null));
            return;
        }
        Map<String, List<MenuItem>> byCategory = new LinkedHashMap<>();
        for (MenuItem m : items) byCategory.computeIfAbsent(m.category(), k -> new java.util.ArrayList<>()).add(m);

        ToggleGroup group = new ToggleGroup();
        menu.getChildren().clear();
        tabs.getChildren().clear();
        byCategory.forEach((category, dishes) -> {
            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(16);
            List<Node> cards = dishes.stream().map(this::dishCard).toList();
            layoutGrid(grid, cards, menu.getWidth());
            // 2 colonne se c'è spazio, altrimenti 1 (finestra stretta)
            menu.widthProperty().addListener((o, a, w) -> layoutGrid(grid, cards, w.doubleValue()));
            VBox section = new VBox(14, Ui.label(Ui.categoryLabel(category), "title"), grid);
            menu.getChildren().add(section);

            ToggleButton tab = new ToggleButton(Ui.categoryLabel(category) + " · " + dishes.size());
            tab.getStyleClass().add("tab-pill");
            tab.setToggleGroup(group);
            tab.setOnAction(e -> { tab.setSelected(true); Navigator.shell().scrollTo(section); });
            tabs.getChildren().add(tab);
        });
        ((ToggleButton) tabs.getChildren().get(0)).setSelected(true);
    }

    private static void layoutGrid(GridPane grid, List<Node> cards, double width) {
        int cols = width == 0 || width >= 700 ? 2 : 1;
        if (grid.getColumnConstraints().size() == cols) return;
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            grid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < cards.size(); i++) grid.add(cards.get(i), i % cols, i / cols);
    }

    private Node dishCard(MenuItem m) {
        Label dishName = Ui.label(m.name(), "xb");
        dishName.setStyle("-fx-font-size: 17px;");
        dishName.setWrapText(true);
        VBox info = new VBox(4, dishName, Ui.label(Fmt.money(m.price()), "price"));
        HBox.setHgrow(info, Priority.ALWAYS);
        info.setMinWidth(0);
        HBox card = new HBox(16, Ui.thumb(m.image(), 96, 14), info, Ui.stepper(restaurant, m.menuId(), m.name(), m.price(), false));
        card.getStyleClass().add("dish-card");
        return card;
    }

    private void renderCart() {
        if (restaurant == null) return;
        Cart cart = Cart.get();
        cartPanel.getChildren().clear();
        String from = cart.isEmpty() ? restaurant.name() : cart.getRestaurantName();
        cartPanel.getChildren().add(new VBox(2, Ui.label("Il tuo ordine", "heading"), Ui.label("da " + from, "muted")));

        if (cart.isEmpty()) {
            cartPanel.getChildren().add(Ui.emptyState("cart", "Il carrello è vuoto", "Aggiungi un piatto per iniziare.", null));
        } else {
            VBox lines = new VBox(12);
            Restaurant owner = new Restaurant(cart.getRestaurantId(), cart.getRestaurantName(), "", cart.getRestaurantImage(), "", List.of());
            for (Cart.Item i : cart.getItems()) {
                Label n = Ui.label(i.name(), "strong");
                n.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(n, Priority.ALWAYS);
                lines.getChildren().add(Ui.row(10, Ui.stepper(owner, i.menuId(), i.name(), i.price(), true), n,
                        Ui.label(Fmt.money(i.lineTotal()), "strong")));
            }
            cartPanel.getChildren().addAll(lines, Ui.divider(),
                    Ui.row(0, Ui.label("Subtotale", "muted"), Ui.spacer(), Ui.label(Fmt.money(cart.getSubtotal()), "strong")),
                    Ui.row(0, Ui.label("Consegna", "muted"), Ui.spacer(), Ui.label("Gratis", "success-text")));
        }
        cartPanel.getChildren().add(Ui.row(0, Ui.label("Totale", "xb"), Ui.spacer(), Ui.label(Fmt.money(cart.getSubtotal()), "total")));
        Button checkout = new Button("Vai al checkout");
        checkout.getStyleClass().add("btn-lg");
        checkout.setMaxWidth(Double.MAX_VALUE);
        checkout.setDisable(cart.isEmpty());
        checkout.setOnAction(e -> Navigator.go("checkout"));
        cartPanel.getChildren().add(checkout);
    }

    @FXML
    private void back() {
        if (Navigator.canGoBack()) Navigator.back();
        else Navigator.home();
    }
}
