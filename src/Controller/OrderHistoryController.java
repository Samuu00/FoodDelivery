package Controller;
import Model.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;

import DAO.OrderDAO;
import Service.OrderService;
import Model.Cart;
import Model.User;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderHistoryController extends HomeController {
    private static final Logger LOGGER = Logger.getLogger(OrderHistoryController.class.getName());
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderService orderService = new OrderService();

    @FXML private VBox ordersBox;

    @FXML
    public void initialize() {
        loadOrders();
    }

    public void salvaOrdine(User user, Cart cart) throws SQLException {
        orderService.placeOrder(user, cart);
    }



    @FXML
    private void exportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salva la Cronologia degli Ordini in PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("File PDF (*.pdf)", "*.pdf")
            );
            fileChooser.setInitialFileName("Cronologia-Ordini.pdf");

            Stage stage = (Stage) ordersBox.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file == null) return;

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            doc.add(new Paragraph("Cronologia Ordini:", titleFont));
            doc.add(new Paragraph(" "));


            for (javafx.scene.Node node : ordersBox.getChildren()) {
                if (node instanceof HBox hbox) {
                    for (javafx.scene.Node innerNode : hbox.getChildren()) {
                        if (innerNode instanceof Label label) {
                            doc.add(new Paragraph(label.getText()));
                        }
                    }
                }
            }

            doc.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante esportazione in PDF", e);
        }
    }

    public void loadOrders() {
        User user = Cart.getInstance().getCurrentUser();
        if (user == null || ordersBox == null) return;

        ordersBox.getChildren().clear();
        List<OrderDAO.OrderItem> orders = orderDAO.getOrderHistory(user.getUsername());
        
        for (OrderDAO.OrderItem order : orders) {
            HBox orderRow = new HBox(20);
            orderRow.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");
            orderRow.setMinWidth(800);
            orderRow.setPrefHeight(50);
            orderRow.setAlignment(Pos.CENTER_LEFT);

            Label orderLabel = new Label(
                    "Ordine #" + order.numeroOrdine +
                    " | Data: " + order.dataOrdine.toLocalDateTime() +
                    " | Stato: " + order.stato +
                    " | Totale: €" + String.format("%.2f", order.prezzoScontato)
            );
            orderLabel.setMinWidth(600);

            Button reOrderBtn = new Button("Riacquista");
            reOrderBtn.setOnAction(e -> {
                try {
                    orderDAO.reorderPastOrder(order.ordineId, Cart.getInstance());
                    refreshCartUI();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Errore al riacquisto ordine", ex);
                }
            });

            orderRow.getChildren().addAll(orderLabel, reOrderBtn);
            ordersBox.getChildren().add(orderRow);
        }
    }


}
