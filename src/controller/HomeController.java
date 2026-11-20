package controller;

import ai.AIAgent;
import app.Navigator;
import dao.RestaurantDAO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Cart;
import model.Restaurant;
import ui.Icons;
import ui.Ui;
import util.Images;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController {
    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    /** Etichetta mostrata → categoria nel DB (null = tutte) e illustrazione. */
    private record Category(String label, String dbName, String image) {}
    private static final List<Category> CATEGORIES = List.of(
            new Category("Tutti", null, null),
            new Category("Pizza", "Pizza", "pizza.png"),
            new Category("Sushi", "Sushi", "sushi.png"),
            new Category("Burger", "Panini", "panini.png"),
            new Category("Dolci", "Dolci", "dolci.png"));
    private static final List<String> SUGGESTIONS = List.of("Pizza piccante sotto i 10 €", "Sushi per due", "Un dolce al cioccolato");

    @FXML private Label greeting, listTitle, count, hint;
    @FXML private HBox searchForm;
    @FXML private TextField search;
    @FXML private Button searchButton, clearSearch;
    @FXML private ProgressIndicator spinner;
    @FXML private FlowPane suggestions, grid;
    @FXML private GridPane categories;

    private final AIAgent ai = new AIAgent();
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final ToggleGroup categoryGroup = new ToggleGroup();
    private List<Restaurant> all = List.of();

    @FXML
    private void initialize() {
        greeting.setText("Ciao " + Cart.get().getCurrentUser().getUsername() + ",\ncosa mangiamo oggi?");
        searchForm.getChildren().add(0, Icons.of("search", 22, "icon-muted"));

        Label tryLabel = Ui.label("Prova a chiedere:", "caption");
        tryLabel.setGraphic(Icons.of("sparkle", 16, "icon-cta"));
        suggestions.getChildren().add(tryLabel);
        for (String s : SUGGESTIONS) {
            Button b = new Button(s);
            b.getStyleClass().add("suggestion");
            b.setOnAction(e -> { search.setText(s); onSearch(); });
            suggestions.getChildren().add(b);
        }

        for (int i = 0; i < CATEGORIES.size(); i++) {
            Category c = CATEGORIES.get(i);
            ToggleButton t = new ToggleButton(c.label());
            t.getStyleClass().add("category-tile");
            t.setToggleGroup(categoryGroup);
            t.setUserData(c);
            if (c.image() != null) {
                ImageView iv = new ImageView(Images.load(c.image(), 128));
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                t.setGraphic(iv);
            }
            categories.add(t, i, 0);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / CATEGORIES.size());
            categories.getColumnConstraints().add(cc);
        }
        categoryGroup.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) { old.setSelected(true); return; }
            showCategory((Category) now.getUserData());
        });

        try {
            all = restaurantDAO.all();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ristoranti non caricati", e);
            grid.getChildren().setAll(Ui.emptyState("alert", "Ristoranti non disponibili", "Controlla la connessione e riprova.", null));
        }
        categoryGroup.selectToggle(categoryGroup.getToggles().get(0));
    }

    private void showCategory(Category c) {
        clearSearch.setVisible(false);
        List<Restaurant> list = c.dbName() == null ? all
                : all.stream().filter(r -> r.categories().contains(c.dbName())).toList();
        render(c.dbName() == null ? "Tutti i ristoranti" : "Ristoranti · " + c.label(), list);
    }

    private void render(String title, List<Restaurant> list) {
        listTitle.setText(title);
        count.setText(list.size() == 1 ? "1 ristorante" : list.size() + " ristoranti");
        if (list.isEmpty()) {
            Button reset = Ui.button("Mostra tutti i ristoranti", null, "btn-secondary");
            reset.setOnAction(e -> clearSearch());
            VBox empty = Ui.emptyState("search", "Nessun ristorante trovato", "Prova una categoria o un piatto diverso.", reset);
            empty.setPrefWidth(1120);
            grid.getChildren().setAll(empty);
            return;
        }
        grid.getChildren().setAll(list.stream().map(this::card).toList());
    }

    private Button card(Restaurant r) {
        StackPane media = new StackPane();
        media.getStyleClass().add("card-media");
        ImageView logo = new ImageView(Images.load(r.image(), 240));
        logo.setFitHeight(120);
        logo.setFitWidth(200);
        logo.setPreserveRatio(true);
        if (logo.getImage() != null) media.getChildren().add(logo);

        Label desc = Ui.label(r.description(), "muted");
        desc.setWrapText(true);
        desc.setPrefWidth(229);
        desc.setMinHeight(Region.USE_PREF_SIZE);
        FlowPane chips = new FlowPane(6, 6);
        for (String c : r.categories()) chips.getChildren().add(Ui.label(Ui.categoryLabel(c), "chip"));
        Label address = Ui.label(r.address(), "muted");
        address.setGraphic(Icons.of("pin", 14, "icon-muted"));
        VBox body = new VBox(Ui.label(r.name(), "heading"), desc, chips, address);
        body.getStyleClass().add("card-body");
        VBox.setMargin(chips, new javafx.geometry.Insets(6, 0, 0, 0));

        VBox content = new VBox(media, body);
        content.setPrefWidth(265);
        Button card = new Button();
        card.setGraphic(content);
        card.getStyleClass().setAll("button", "restaurant-card");
        card.setPrefWidth(265);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.TOP_LEFT);
        card.setAccessibleText(r.name());
        card.setOnAction(e -> {
            RestaurantController page = Navigator.go("restaurant");
            page.load(r);
        });
        return card;
    }

    @FXML
    private void onSearch() {
        search(search.getText());
    }

    /** Ricerca AI (se configurata) con ripiego su ricerca testuale; i risultati vanno nella griglia. */
    public void search(String text) {
        String query = text == null ? "" : text.trim();
        search.setText(query);
        if (query.isEmpty()) {
            search.requestFocus();
            return;
        }
        setBusy(true);
        Task<List<Restaurant>> task = new Task<>() {
            @Override
            protected List<Restaurant> call() throws SQLException {
                String sql = ai.generateSQL(query);
                if (sql != null) {
                    try {
                        List<Integer> ids = restaurantDAO.runAiQuery(sql);
                        if (!ids.isEmpty()) {
                            return all.stream().filter(r -> ids.contains(r.id()))
                                    .sorted(Comparator.comparingInt(r -> ids.indexOf(r.id()))).toList();
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Query AI non utilizzabile, uso la ricerca testuale", e);
                    }
                }
                return restaurantDAO.search(query);
            }
        };
        task.setOnSucceeded(e -> {
            setBusy(false);
            categoryGroup.getToggles().forEach(t -> t.setSelected(false));
            render("Risultati per «" + query + "»", task.getValue());
            clearSearch.setVisible(true);
            Navigator.shell().scrollTo(listTitle);
        });
        task.setOnFailed(e -> {
            setBusy(false);
            LOGGER.log(Level.SEVERE, "Ricerca fallita", task.getException());
            hint.setText("La ricerca non è riuscita. Controlla la connessione e riprova.");
            hint.setVisible(true);
        });
        Thread t = new Thread(task, "search");
        t.setDaemon(true);
        t.start();
    }

    private void setBusy(boolean busy) {
        spinner.setVisible(busy);
        search.setDisable(busy);
        searchButton.setDisable(busy);
        hint.setVisible(busy);
        if (busy) hint.setText(ai.isAvailable() ? "Sto chiedendo all'assistente AI…" : "Cerco…");
    }

    @FXML
    private void clearSearch() {
        search.clear();
        categoryGroup.selectToggle(categoryGroup.getToggles().get(0));
        showCategory(CATEGORIES.get(0));
    }
}
