package controller;

import app.Navigator;
import dao.AddressDAO;
import dao.CouponDAO;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Cart;
import model.User;
import service.OrderService;
import ui.Icons;
import ui.Ui;
import util.Fmt;
import util.Images;
import util.Validators;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Checkout e pagamento in un'unica pagina: indirizzo, metodo, coupon, riepilogo con "Paga". */
public class CheckoutController implements Navigator.Page {
    private static final Logger LOGGER = Logger.getLogger(CheckoutController.class.getName());

    private enum Method { CARD, PAYPAL, CASH }

    @FXML private Button backLink, addAddressButton;
    @FXML private GridPane addressGrid, methodGrid, cardForm;
    @FXML private HBox addAddressBox, couponForm, couponApplied, cashBox;
    @FXML private VBox paypalBox, summary;
    @FXML private TextField newAddress, cardNumber, cardHolder, expiry, paypalEmail, coupon;
    @FXML private PasswordField cvv;
    @FXML private Label addressError, couponLabel, couponError;

    private final AddressDAO addressDAO = new AddressDAO();
    private final ToggleGroup addressGroup = new ToggleGroup();
    private final ToggleGroup methodGroup = new ToggleGroup();
    private final InvalidationListener cartListener = o -> renderSummary();
    private final User user = Cart.get().getCurrentUser();
    private final Label payError = Ui.label("", "error-text");

    @FXML
    private void initialize() {
        backLink.setGraphic(Icons.of("chevron-left", 16, "icon-inherit"));
        backLink.setText("Torna al menu di " + Cart.get().getRestaurantName());
        addAddressButton.setGraphic(Icons.of("plus", 16, "icon-inherit"));
        payError.setWrapText(true);
        payError.managedProperty().bind(payError.visibleProperty());
        payError.setVisible(false);

        addressGroup.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null && old != null) { old.setSelected(true); return; }
            if (now != null) Cart.get().setDeliveryAddress((String) now.getUserData());
        });
        buildMethods();
        loadAddresses();
        Cart.get().versionProperty().addListener(new WeakInvalidationListener(cartListener));
        renderCoupon();
        renderSummary();
    }

    @Override
    public void onShow() { renderSummary(); }

    // ---------- 1 Indirizzo ----------

    private void loadAddresses() {
        List<String> list;
        try {
            list = addressDAO.list(user.getUsername());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Indirizzi non caricati", e);
            showAddressError("Impossibile caricare gli indirizzi.");
            return;
        }
        addressGrid.getChildren().clear();
        String current = Cart.get().getDeliveryAddress();
        for (int i = 0; i < list.size(); i++) {
            String a = list.get(i);
            ToggleButton t = new ToggleButton();
            t.getStyleClass().add("choice-card");
            t.setUserData(a);
            t.setToggleGroup(addressGroup);
            t.setMaxWidth(Double.MAX_VALUE);
            Region dot = new Region();
            dot.getStyleClass().add("radio-dot");
            int comma = a.indexOf(',');
            VBox text = new VBox(2, Ui.label(comma > 0 ? a.substring(0, comma) : a, "xb"),
                    Ui.label(comma > 0 ? a.substring(comma + 1).trim() : "", "muted"));
            t.setGraphic(Ui.row(12, dot, text));
            t.setAccessibleText(a);
            addressGrid.add(t, i % 2, i / 2);
            if (a.equals(current)) t.setSelected(true);
        }
        if (addressGroup.getSelectedToggle() == null && !addressGroup.getToggles().isEmpty()) {
            addressGroup.getToggles().get(0).setSelected(true);
        }
        if (list.isEmpty()) showAddAddress();
    }

    @FXML
    private void showAddAddress() {
        addAddressBox.setVisible(true);
        addAddressButton.setVisible(false);
        newAddress.requestFocus();
    }

    @FXML
    private void saveAddress() {
        String a = newAddress.getText().trim();
        if (a.length() < 5 || !a.contains(",")) {
            showAddressError("Scrivi via, numero civico e città, es. «Via Roma 1, Cosenza».");
            return;
        }
        try {
            addressDAO.add(user.getUsername(), a);
            Cart.get().setDeliveryAddress(a);
            newAddress.clear();
            addressError.setVisible(false);
            addAddressBox.setVisible(false);
            addAddressButton.setVisible(true);
            loadAddresses();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Indirizzo non salvato", e);
            showAddressError("Impossibile salvare l'indirizzo.");
        }
    }

    private void showAddressError(String msg) {
        addressError.setText(msg);
        addressError.setVisible(true);
    }

    // ---------- 2 Pagamento ----------

    private void buildMethods() {
        addMethod(Method.CARD, "Carta", logos("visa.png", "mastercard.png"), 0);
        addMethod(Method.PAYPAL, "PayPal", logos("paypal.png"), 1);
        addMethod(Method.CASH, "Alla consegna", Icons.of("cash", 24, "icon-success"), 2);
        methodGroup.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) { old.setSelected(true); return; }
            Method m = (Method) now.getUserData();
            cardForm.setVisible(m == Method.CARD);
            paypalBox.setVisible(m == Method.PAYPAL);
            cashBox.setVisible(m == Method.CASH);
            payError.setVisible(false);
        });
        methodGroup.getToggles().get(0).setSelected(true);
    }

    private Node logos(String... files) {
        HBox h = new HBox(6);
        h.setAlignment(Pos.CENTER);
        for (String f : files) {
            ImageView iv = new ImageView(Images.load(f, 64));
            iv.setFitHeight(20);
            iv.setPreserveRatio(true);
            h.getChildren().add(iv);
        }
        return h;
    }

    private void addMethod(Method m, String label, Node graphic, int col) {
        ToggleButton t = new ToggleButton(label);
        t.getStyleClass().addAll("choice-card", "pay-card", "xb");
        t.setGraphic(graphic);
        t.setUserData(m);
        t.setToggleGroup(methodGroup);
        t.setMaxWidth(Double.MAX_VALUE);
        methodGrid.add(t, col, 0);
    }

    private Method method() { return (Method) methodGroup.getSelectedToggle().getUserData(); }

    // ---------- 3 Coupon ----------

    @FXML
    private void applyCoupon() {
        String code = coupon.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showCouponError("Inserisci un codice.");
            return;
        }
        try {
            double percent = new CouponDAO().discountPercent(code, user.getUsername());
            if (percent < 0) {
                showCouponError("Codice non valido o non disponibile per il tuo account.");
                return;
            }
            Cart.get().applyCoupon(code, percent);
            coupon.clear();
            couponError.setVisible(false);
            renderCoupon();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Coupon non verificato", e);
            showCouponError("Impossibile verificare il codice ora.");
        }
    }

    @FXML
    private void removeCoupon() {
        Cart.get().removeCoupon();
        renderCoupon();
    }

    private void renderCoupon() {
        String code = Cart.get().getCouponCode();
        couponForm.setVisible(code == null);
        couponApplied.setVisible(code != null);
        if (code != null) {
            couponLabel.setText(code + " applicato · −" + Math.round(Cart.get().getDiscountPercent()) + "%");
            couponLabel.setGraphic(Icons.of("check", 18, "icon-success"));
        }
    }

    private void showCouponError(String msg) {
        couponError.setText(msg);
        couponError.setVisible(true);
    }

    // ---------- Riepilogo ----------

    private void renderSummary() {
        Cart cart = Cart.get();
        summary.getChildren().clear();
        int n = cart.getTotalQuantity();
        summary.getChildren().add(Ui.row(12, Ui.logoBadge(cart.getRestaurantImage(), 48),
                new VBox(Ui.label(cart.getRestaurantName() == null ? "Carrello vuoto" : cart.getRestaurantName(), "heading"),
                        Ui.label(n == 1 ? "1 articolo" : n + " articoli", "muted"))));
        VBox lines = new VBox(8);
        for (Cart.Item i : cart.getItems()) {
            Label q = Ui.label(i.quantity() + "×", "xb");
            q.setStyle("-fx-text-fill: -fd-cta;");
            lines.getChildren().add(Ui.row(10, q, Ui.label(i.name()), Ui.spacer(), Ui.label(Fmt.money(i.lineTotal()))));
        }
        summary.getChildren().addAll(lines, Ui.divider(),
                Ui.row(0, Ui.label("Subtotale", "muted"), Ui.spacer(), Ui.label(Fmt.money(cart.getSubtotal()))));
        if (cart.getDiscount() > 0) {
            summary.getChildren().add(Ui.row(0, Ui.label("Sconto " + cart.getCouponCode(), "success-text"), Ui.spacer(),
                    Ui.label("−" + Fmt.money(cart.getDiscount()), "success-text")));
        }
        summary.getChildren().addAll(
                Ui.row(0, Ui.label("Consegna", "muted"), Ui.spacer(), Ui.label("Gratis", "success-text")),
                Ui.divider(),
                Ui.row(0, Ui.label("Totale", "xb"), Ui.spacer(), Ui.label(Fmt.money(cart.getTotal()), "total")));
        String address = cart.getDeliveryAddress();
        Label deliver = Ui.label(address == null ? "Scegli dove consegnare" : "Consegna a " + address, "muted");
        deliver.setWrapText(true);
        Button pay = new Button("Paga " + Fmt.money(cart.getTotal()));
        pay.getStyleClass().add("btn-lg");
        pay.setMaxWidth(Double.MAX_VALUE);
        pay.setDefaultButton(true);
        pay.setDisable(cart.isEmpty() || address == null);
        pay.setOnAction(e -> pay());
        Label secure = Ui.label("Pagamento simulato: nessun addebito reale", "muted", "secure-note");
        secure.setMaxWidth(320);
        secure.setGraphic(Icons.of("lock", 14, "icon-muted"));
        HBox secureRow = new HBox(secure);
        secureRow.setAlignment(Pos.CENTER);
        summary.getChildren().addAll(deliver, payError, pay, secureRow);
    }

    // ---------- Paga ----------

    /** @return il primo errore del metodo scelto, o null se è tutto valido */
    private String validatePayment() {
        return switch (method()) {
            case CARD -> cardNumber.getText().isBlank() || cardHolder.getText().isBlank() || expiry.getText().isBlank() || cvv.getText().isBlank()
                    ? "Compila tutti i dati della carta."
                    : !Validators.cardNumber(cardNumber.getText()) ? "Il numero della carta non è valido."
                    : !Validators.expiry(expiry.getText(), YearMonth.now()) ? "La carta è scaduta o la data non è nel formato MM/AA."
                    : !Validators.cvv(cvv.getText()) ? "Il CVV deve avere 3 o 4 cifre."
                    : !Validators.cardHolder(cardHolder.getText()) ? "Scrivi nome e cognome dell'intestatario."
                    : null;
            case PAYPAL -> Validators.email(paypalEmail.getText()) ? null : "Inserisci l'email del tuo conto PayPal.";
            case CASH -> null;
        };
    }

    private void markInvalid() {
        TextInputControl[] fields = {cardNumber, cardHolder, expiry, cvv, paypalEmail};
        for (TextInputControl f : fields) f.getStyleClass().remove("invalid");
        if (method() == Method.CARD) {
            if (!Validators.cardNumber(cardNumber.getText())) cardNumber.getStyleClass().add("invalid");
            if (!Validators.cardHolder(cardHolder.getText())) cardHolder.getStyleClass().add("invalid");
            if (!Validators.expiry(expiry.getText(), YearMonth.now())) expiry.getStyleClass().add("invalid");
            if (!Validators.cvv(cvv.getText())) cvv.getStyleClass().add("invalid");
        } else if (method() == Method.PAYPAL && !Validators.email(paypalEmail.getText())) {
            paypalEmail.getStyleClass().add("invalid");
        }
    }

    private void pay() {
        String problem = validatePayment();
        markInvalid();
        if (problem != null) {
            payError.setText(problem);
            payError.setVisible(true);
            return;
        }
        Cart cart = Cart.get();
        String paidWith = switch (method()) {
            case CARD -> "Carta ···· " + cardNumber.getText().replaceAll("[^0-9]", "").replaceAll(".*(.{4})$", "$1");
            case PAYPAL -> "PayPal";
            case CASH -> "Contanti alla consegna";
        };
        try {
            int number = new OrderService().placeOrder(user, cart);
            ConfirmationController.Receipt receipt = new ConfirmationController.Receipt(number, cart.getRestaurantName(),
                    cart.getDeliveryAddress(), summaryOf(cart), paidWith, cart.getTotal());
            cart.clear();
            ConfirmationController c = Navigator.go("confirmation");
            c.show(receipt);
            Navigator.forgetHistory();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ordine non salvato", e);
            payError.setText("Non siamo riusciti a registrare l'ordine. Nessun addebito: riprova.");
            payError.setVisible(true);
        }
    }

    private static String summaryOf(Cart cart) {
        // stesso ordine (alfabetico) dello storico ordini
        return String.join(", ", cart.getItems().stream().sorted(java.util.Comparator.comparing(Cart.Item::name))
                .map(i -> i.quantity() + "× " + i.name()).toList());
    }

    @FXML
    private void backToMenu() {
        RestaurantController r = Navigator.go("restaurant");
        r.load(Cart.get().getRestaurantId());
    }
}
