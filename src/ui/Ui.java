package ui;

import app.Navigator;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Cart;
import model.Restaurant;
import util.Images;

import java.util.Map;
import java.util.Optional;

/** Componenti condivisi tra le pagine (gli equivalenti dei componenti del design system). */
public final class Ui {
    private Ui() {}

    private static final Map<String, String> CATEGORY_LABELS = Map.of("Panini", "Burger");

    /** Nome della categoria come lo vede l'utente (nel DB i burger sono "Panini"). */
    public static String categoryLabel(String dbName) {
        return CATEGORY_LABELS.getOrDefault(dbName, dbName);
    }

    public static Label label(String text, String... styleClasses) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styleClasses);
        return l;
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static Region divider() {
        Region r = new Region();
        r.getStyleClass().add("divider");
        return r;
    }

    public static HBox row(double spacing, Node... children) {
        HBox h = new HBox(spacing, children);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Pulsante con icona e testo; variant = classe CSS (btn-secondary, btn-ghost, btn-outline) o null per il primario. */
    public static Button button(String text, String icon, String variant) {
        Button b = new Button(text);
        if (variant != null) b.getStyleClass().add(variant);
        if (icon != null) b.setGraphic(Icons.of(icon, 16, "icon-inherit"));
        return b;
    }

    /** Immagine con angoli arrotondati; segnaposto se manca. */
    public static Node thumb(String image, double size, double radius) {
        Image img = Images.load(image, size * 2);
        if (img == null) {
            Region r = new Region();
            r.setMinSize(size, size);
            r.getStyleClass().add("thumb-placeholder");
            return r;
        }
        ImageView v = new ImageView(img);
        v.setFitWidth(size);
        v.setFitHeight(size);
        v.setPreserveRatio(true);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(size, size);
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        v.setClip(clip);
        StackPane box = new StackPane(v);
        box.setMinSize(size, size);
        box.setMaxSize(size, size);
        return box;
    }

    /** Logo del ristorante in un cerchio crema. */
    public static Node logoBadge(String image, double size) {
        StackPane circle = new StackPane();
        circle.getStyleClass().add("logo-badge");
        circle.setMinSize(size, size);
        circle.setMaxSize(size, size);
        Image img = Images.load(image, size * 2);
        if (img != null) {
            ImageView v = new ImageView(img);
            v.setFitWidth(size * 0.8);
            v.setFitHeight(size * 0.8);
            v.setPreserveRatio(true);
            circle.getChildren().add(v);
        }
        return circle;
    }

    /**
     * "Aggiungi" quando il piatto non è nel carrello, altrimenti stepper [−] n [+].
     * Larghezza fissa (niente salti di layout) e aggiornamento automatico al cambio del carrello.
     */
    public static Node stepper(Restaurant r, int menuId, String name, double price, boolean compact) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setFillHeight(false);
        if (!compact) box.setMinWidth(120);
        Runnable render = () -> {
            int q = Cart.get().quantityOf(menuId);
            if (q == 0 && !compact) {
                Button add = button("Aggiungi", "plus", "btn-secondary");
                add.getStyleClass().add("btn-sm");
                add.setOnAction(e -> addToCart(r, menuId, name, price));
                box.getChildren().setAll(add);
                return;
            }
            Button minus = new Button();
            minus.getStyleClass().setAll("button", "step", "step-minus");
            minus.setGraphic(Icons.of("minus", 16, "icon-inherit"));
            minus.setAccessibleText("Togli una " + name);
            minus.setOnAction(e -> Cart.get().remove(menuId));
            Button plus = new Button();
            plus.getStyleClass().setAll("button", "step", "step-plus");
            plus.setGraphic(Icons.of("plus", 16, "icon-inherit"));
            plus.setAccessibleText("Aggiungi una " + name);
            plus.setOnAction(e -> addToCart(r, menuId, name, price));
            HBox pill = new HBox(4, minus, label(String.valueOf(q), "qty"), plus);
            pill.setAlignment(Pos.CENTER);
            pill.getStyleClass().add(compact ? "stepper-compact" : "stepper");
            box.getChildren().setAll(pill);
        };
        render.run();
        InvalidationListener l = o -> render.run();
        box.getProperties().put("cartListener", l); // riferimento forte finché il nodo vive
        Cart.get().versionProperty().addListener(new WeakInvalidationListener(l));
        return box;
    }

    /** Aggiunge al carrello chiedendo conferma se il carrello è di un altro ristorante. */
    public static void addToCart(Restaurant r, int menuId, String name, double price) {
        Cart cart = Cart.get();
        if (cart.conflictsWith(r.id()) && !confirm("Iniziare un nuovo ordine?",
                "Il carrello contiene piatti di " + cart.getRestaurantName() + ". Se continui verrà svuotato e potrai ordinare da " + r.name() + ".",
                "Svuota e aggiungi")) {
            return;
        }
        boolean first = cart.quantityOf(menuId) == 0;
        cart.add(r, menuId, name, price);
        if (first && Navigator.shell() != null) Navigator.shell().toast(name + " aggiunta al carrello");
    }

    public static VBox emptyState(String icon, String title, String text, Button action) {
        VBox v = new VBox(8, Icons.of(icon, 36, "icon-cta"), label(title, "heading"), label(text, "muted"));
        v.getStyleClass().add("empty-state");
        v.setAlignment(Pos.CENTER);
        if (action != null) {
            VBox.setMargin(action, new javafx.geometry.Insets(8, 0, 0, 0));
            v.getChildren().add(action);
        }
        return v;
    }

    public static <D extends Dialog<?>> D styled(D d) {
        d.getDialogPane().getStylesheets().setAll(Navigator.stylesheets());
        return d;
    }

    public static void error(String title, String message) {
        Alert a = styled(new Alert(Alert.AlertType.ERROR));
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(message);
        a.showAndWait();
    }

    public static boolean confirm(String title, String message, String okText) {
        ButtonType ok = new ButtonType(okText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = styled(new Alert(Alert.AlertType.NONE, message, cancel, ok));
        a.setTitle(title);
        a.setHeaderText(title);
        a.getDialogPane().lookupButton(cancel).getStyleClass().add("btn-ghost");
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ok;
    }
}
