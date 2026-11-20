package controller;

import app.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.Icons;
import ui.Ui;
import util.Fmt;

public class ConfirmationController {

    /** Dati dell'ordine appena pagato (il carrello a questo punto è già svuotato). */
    public record Receipt(int number, String restaurant, String address, String items, String paidWith, double total) {}

    @FXML private StackPane checkCircle;
    @FXML private Label number, message;
    @FXML private VBox recap;

    @FXML
    private void initialize() {
        checkCircle.getChildren().add(Icons.of("check", 48, "icon-success"));
    }

    public void show(Receipt r) {
        number.setText("ORDINE N. " + r.number());
        message.setText(r.restaurant() + " ha ricevuto il tuo ordine e lo sta preparando.");
        Label items = Ui.label(r.items(), "strong");
        items.setWrapText(true);
        recap.getChildren().setAll(
                Ui.row(16, Ui.label("Consegna a", "muted"), Ui.spacer(), Ui.label(r.address(), "xb")),
                Ui.row(16, Ui.label("Articoli", "muted"), Ui.spacer(), items),
                Ui.row(16, Ui.label("Pagato con", "muted"), Ui.spacer(), Ui.label(r.paidWith(), "strong")),
                Ui.divider(),
                Ui.row(16, Ui.label("Totale", "xb"), Ui.spacer(), Ui.label(Fmt.money(r.total()), "title")));
    }

    @FXML private void home() { Navigator.home(); }

    @FXML
    private void orders() {
        Navigator.home();
        AccountController a = Navigator.go("account");
        a.showTab("orders");
    }
}
