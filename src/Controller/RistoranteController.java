package Controller;
import Model.*;

import DatabaseConn.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import DAO.MenuDAO;
import Model.Restaurant;
import Model.Cart;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RistoranteController extends HomeController implements SceneManager.SceneController {
    private static final Logger LOGGER = Logger.getLogger(RistoranteController.class.getName());
    private final MenuDAO menuDAO = new MenuDAO();

    @FXML private VBox ContainerMenu;
    @FXML private ImageView ImmagineRistorante;
    @FXML private Label NomeRistorante;
    @FXML private Label DescrizioneRistorante;

    private int ristoranteId;

    public void setRistorante(int id) {
        this.ristoranteId = id;
        caricaInfoRistorante();
        caricaMenu();
    }

    private void caricaInfoRistorante() {
        try {
            Restaurant r = menuDAO.getRestaurantInfo(ristoranteId);
            if (r != null) {
                NomeRistorante.setText(r.getNome());
                DescrizioneRistorante.setText(r.getDescrizione());
                String img = r.getImmagineURL();
                if (img != null && !img.isEmpty()) {
                    ImmagineRistorante.setImage(new Image("file:src/Image/" + img));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore Info Ristorante", e);
        }
    }

    @Override
    public void onSceneShown() {
        for (Node categoriaNode : ContainerMenu.getChildren()) {
            if (!(categoriaNode instanceof VBox categoriaBox)) continue;

            for (int i = 1; i < categoriaBox.getChildren().size(); i++) {
                Node piattoNode = categoriaBox.getChildren().get(i);
                if (!(piattoNode instanceof HBox hbox) || hbox.getChildren().size() < 2) continue;

                int idxInfo = (hbox.getChildren().get(0) instanceof ImageView) ? 1 : 0;
                int idxQty  = idxInfo + 1;

                VBox infoBox = (VBox) hbox.getChildren().get(idxInfo);
                HBox quantityBox = (HBox) hbox.getChildren().get(idxQty);

                Label qtyLabel = (Label) quantityBox.getChildren().get(1);
                Label nomeLbl = (Label) infoBox.getChildren().get(0);
                String nomePiatto = nomeLbl.getText();

                int q = Cart.getInstance().getItems().stream()
                        .filter(item -> item.name.equals(nomePiatto))
                        .mapToInt(item -> item.quantity)
                        .findFirst()
                        .orElse(0);

                qtyLabel.setText(String.valueOf(q));
            }
        }
    }

    private void caricaMenu() {
        ContainerMenu.getChildren().clear();
        try {
            java.util.List<Model.MenuItem> menuList = menuDAO.getMenuItemsByRestaurant(ristoranteId);
            String categoriaAttuale = "";
            VBox categoriaBox = null;

            for (Model.MenuItem mItem : menuList) {
                String categoria = mItem.getCategoria();
                String nomePiatto = mItem.getNomePiatto();
                int menuId = mItem.getMenuId();
                double prezzo = mItem.getPrezzo();
                String imgPiatto = mItem.getImmagineURL();

                if (!categoria.equals(categoriaAttuale)) {
                    categoriaAttuale = categoria;
                    Label catLabel = new Label(categoria);
                    catLabel.getStyleClass().add("label");

                    categoriaBox = new VBox(10);
                    categoriaBox.getStyleClass().add("category-card");
                    categoriaBox.getChildren().add(catLabel);

                    ContainerMenu.getChildren().add(categoriaBox);
                }

                HBox piattoBox = new HBox(10);
                piattoBox.getStyleClass().add("menu-item");

                if (imgPiatto != null && !imgPiatto.isEmpty()) {
                    ImageView img = new ImageView(new Image("file:src/Image/" + imgPiatto, 80, 80, true, true));
                    piattoBox.getChildren().add(img);
                }

                VBox infoBox = new VBox(5);
                Label nomeLbl = new Label(nomePiatto);
                nomeLbl.getStyleClass().add("label");

                Label prezzoLbl = new Label("€ " + prezzo);
                prezzoLbl.getStyleClass().add("price-label");

                infoBox.getChildren().addAll(nomeLbl, prezzoLbl);

                HBox quantityBox = new HBox(5);
                Button minusBtn = new Button("-");
                Button plusBtn = new Button("+");
                int q = Cart.getInstance().getItems().stream()
                        .filter(item -> item.name.equals(nomePiatto))
                        .mapToInt(item -> item.quantity)
                        .findFirst()
                        .orElse(0);
                Label qtyLabel = new Label(String.valueOf(q));


                minusBtn.getStyleClass().add("quantity-btn");
                plusBtn.getStyleClass().add("quantity-btn");

                minusBtn.setOnAction(e -> {
                    Cart.getInstance().remove(nomePiatto);
                    int q1 = Cart.getInstance().getItems().stream()
                            .filter(item -> item.name.equals(nomePiatto))
                            .mapToInt(item -> item.quantity)
                            .findFirst().orElse(0);
                    qtyLabel.setText(String.valueOf(q1));
                    refreshCartCount();
                });

                plusBtn.setOnAction(e -> {
                    Cart.getInstance().add(nomePiatto, prezzo, menuId);
                    int q2 = Cart.getInstance().getItems().stream()
                            .filter(item -> item.name.equals(nomePiatto))
                            .mapToInt(item -> item.quantity)
                            .findFirst().orElse(0);
                    qtyLabel.setText(String.valueOf(q2));
                    refreshCartCount();
                });



                quantityBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

                piattoBox.getChildren().addAll(infoBox, quantityBox);
                categoriaBox.getChildren().add(piattoBox);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore Caricamento Menu", e);
        }
    }

}
