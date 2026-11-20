package controller;

import app.Navigator;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.AddressDAO;
import dao.OrderDAO;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Cart;
import model.Order;
import model.User;
import ui.Icons;
import ui.Ui;
import util.Fmt;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Area account: storico ordini (riordina, PDF) e indirizzi di consegna. */
public class AccountController implements Navigator.Page {
    private static final Logger LOGGER = Logger.getLogger(AccountController.class.getName());

    @FXML private Label initial, username, email;
    @FXML private ToggleGroup tabs;
    @FXML private ToggleButton ordersTab, addressesTab;
    @FXML private VBox content;

    private final User user = Cart.get().getCurrentUser();
    private final OrderDAO orderDAO = new OrderDAO();
    private final AddressDAO addressDAO = new AddressDAO();
    private List<Order> orders = List.of();

    @FXML
    private void initialize() {
        initial.setText(user.getUsername().substring(0, 1).toUpperCase());
        username.setText(user.getUsername());
        email.setText(user.getEmail());
        tabs.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) { old.setSelected(true); return; }
            onShow();
        });
        ordersTab.setSelected(true);
    }

    public void showTab(String tab) {
        (tab.equals("addresses") ? addressesTab : ordersTab).setSelected(true);
        onShow();
    }

    @Override
    public void onShow() {
        if (addressesTab.isSelected()) renderAddresses();
        else renderOrders();
    }

    // ---------- Ordini ----------

    private void renderOrders() {
        try {
            orders = orderDAO.history(user.getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Storico non caricato", e);
            content.getChildren().setAll(Ui.emptyState("alert", "Storico non disponibile", "Riprova tra poco.", null));
            return;
        }
        Button export = Ui.button("Esporta PDF", "download", "btn-outline");
        export.setDisable(orders.isEmpty());
        export.setOnAction(e -> exportPdf());
        content.getChildren().setAll(Ui.row(16,
                new VBox(Ui.label("I miei ordini", "display"), Ui.label(orders.size() == 1 ? "1 ordine" : orders.size() + " ordini", "muted")),
                Ui.spacer(), export));
        if (orders.isEmpty()) {
            Button explore = Ui.button("Scopri i ristoranti", null, "btn-secondary");
            explore.setOnAction(e -> Navigator.home());
            content.getChildren().add(Ui.emptyState("bag", "Nessun ordine ancora", "Quando ordinerai qualcosa lo ritroverai qui.", explore));
            return;
        }
        for (Order o : orders) content.getChildren().add(orderCard(o));
    }

    private Node orderCard(Order o) {
        Label items = Ui.label(o.items(), "muted");
        items.setMaxWidth(Double.MAX_VALUE);
        VBox info = new VBox(4, Ui.row(10, Ui.label(o.restaurantName(), "heading"),
                Ui.label("n. " + o.number() + " · " + Fmt.shortDate(o.date()), "muted")), items);
        info.setMinWidth(0);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label status = Ui.label(o.status(), "Consegnato".equalsIgnoreCase(o.status()) ? "chip-success" : "chip-cream");
        Label total = Ui.label(Fmt.money(o.total()), "heading");
        total.setMinWidth(90);
        total.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button reorder = Ui.button("Riordina", null, "btn-secondary");
        reorder.getStyleClass().add("btn-sm");
        reorder.setOnAction(e -> reorder(o));
        HBox card = new HBox(Ui.logoBadge(o.restaurantImage(), 56), info, status, total, reorder);
        card.getStyleClass().addAll("card", "order-card");
        return card;
    }

    private void reorder(Order o) {
        Cart cart = Cart.get();
        if (!cart.isEmpty() && !Ui.confirm("Sostituire il carrello?",
                "Il carrello contiene già dei piatti. Riordinando verranno sostituiti con quelli dell'ordine n. " + o.number() + ".",
                "Sostituisci")) {
            return;
        }
        try {
            orderDAO.reorder(o, cart);
            Navigator.shell().toast("Ordine n. " + o.number() + " nel carrello");
            RestaurantController r = Navigator.go("restaurant");
            r.load(o.restaurantId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Riordino fallito", e);
            Ui.error("Errore", "Impossibile riordinare. Riprova tra poco.");
        }
    }

    private void exportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salva lo storico ordini");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        chooser.setInitialFileName("Ordini-FoodDelivery.pdf");
        File file = chooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            writePdf(out, user.getUsername(), orders);
            Navigator.shell().toast("PDF salvato: " + file.getName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Export PDF fallito", e);
            Ui.error("Errore", "Impossibile creare il PDF.");
        }
    }

    static void writePdf(java.io.OutputStream out, String username, List<Order> orders) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("Food Delivery - Storico ordini di " + username, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
        doc.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(new float[]{1, 2, 4, 2, 2});
        table.setWidthPercentage(100);
        for (String h : new String[]{"N.", "Data", "Ristorante e piatti", "Stato", "Totale"}) table.addCell(h);
        for (Order o : orders) {
            table.addCell(String.valueOf(o.number()));
            table.addCell(Fmt.date(o.date()));
            table.addCell(o.restaurantName() + ": " + o.items().replace("×", "x"));
            table.addCell(o.status());
            table.addCell(Fmt.money(o.total()).replace("€", "EUR"));
        }
        doc.add(table);
        doc.close();
    }

    // ---------- Indirizzi ----------

    private void renderAddresses() {
        content.getChildren().setAll(new VBox(Ui.label("Indirizzi di consegna", "display"),
                Ui.label("Quello in uso è mostrato nella barra in alto e proposto al checkout.", "muted")));
        List<String> list;
        try {
            list = addressDAO.list(user.getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Indirizzi non caricati", e);
            content.getChildren().add(Ui.emptyState("alert", "Indirizzi non disponibili", "Riprova tra poco.", null));
            return;
        }
        if (!list.isEmpty()) {
            VBox rows = new VBox();
            rows.getStyleClass().add("card");
            rows.setStyle("-fx-padding: 4px 24px;");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) rows.getChildren().add(Ui.divider());
                rows.getChildren().add(addressRow(list.get(i)));
            }
            content.getChildren().add(rows);
        }
        content.getChildren().add(addressForm(list.isEmpty()));
    }

    private Node addressRow(String a) {
        int comma = a.indexOf(',');
        VBox text = new VBox(2, Ui.label(comma > 0 ? a.substring(0, comma) : a, "xb"),
                Ui.label(comma > 0 ? a.substring(comma + 1).trim() : "", "muted"));
        HBox row = Ui.row(14, Icons.of("pin", 20, "icon-cta"), text, Ui.spacer());
        row.getStyleClass().add("list-row");
        if (a.equals(Cart.get().getDeliveryAddress())) {
            row.getChildren().add(Ui.label("In uso", "chip-cream"));
        } else {
            Button use = Ui.button("Usa per la consegna", null, "btn-ghost");
            use.setOnAction(e -> { Cart.get().setDeliveryAddress(a); renderAddresses(); });
            row.getChildren().add(use);
        }
        return row;
    }

    private Node addressForm(boolean first) {
        TextField field = new TextField();
        field.setPromptText("Via, numero civico, città");
        HBox.setHgrow(field, Priority.ALWAYS);
        Label error = Ui.label("", "error-text");
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);
        Button save = new Button("Salva indirizzo");
        Runnable submit = () -> {
            String a = field.getText().trim();
            if (a.length() < 5 || !a.contains(",")) {
                error.setText("Scrivi via, numero civico e città, es. «Via Roma 1, Cosenza».");
                error.setVisible(true);
                field.getStyleClass().add("invalid");
                return;
            }
            try {
                addressDAO.add(user.getUsername(), a);
                if (Cart.get().getDeliveryAddress() == null) Cart.get().setDeliveryAddress(a);
                Navigator.shell().toast("Indirizzo salvato");
                renderAddresses();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Indirizzo non salvato", ex);
                error.setText("Impossibile salvare l'indirizzo.");
                error.setVisible(true);
            }
        };
        save.setOnAction(e -> submit.run());
        field.setOnAction(e -> submit.run());
        VBox form = new VBox(12, Ui.label(first ? "Aggiungi il tuo primo indirizzo" : "Nuovo indirizzo", "heading"),
                Ui.label("Indirizzo", "field-label"), Ui.row(10, field, save), error);
        form.getStyleClass().add("card");
        form.setStyle("-fx-padding: 24px;");
        return form;
    }
}
