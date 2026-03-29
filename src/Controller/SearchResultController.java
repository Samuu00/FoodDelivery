package Controller;
import Model.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.List;
import DAO.RestaurantDAO;

public class SearchResultController extends HomeController implements SceneManager.SceneController {

    @FXML private VBox resultBox;
    @FXML private AnchorPane resultRoot;
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private String sqlQuery;
    private String category;

    public void setSQLQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
        this.category = null;
        loadResults();
    }

    public void setCategory(String category) {
        this.category = category;
        this.sqlQuery = null;
        loadResults();
    }

    private void loadResults() {
        resultBox.getChildren().clear();

        List<Restaurant> restaurants;
        if (category != null) {
            restaurants = restaurantDAO.getRestaurantsByCategory(category);
        } else {
            restaurants = restaurantDAO.getRestaurantsByCustomQuery(sqlQuery);
        }

        if (restaurants.isEmpty()) {
            resultBox.getChildren().add(new Label("Nessun ristorante trovato."));
            return;
        }

        for (Restaurant r : restaurants) {
            int ristoranteId = r.getId();
            String nomeRistorante = r.getNome();
            String descrizione = r.getDescrizione();
            String immagineURL = r.getImmagineURL();

            HBox ristoranteBox = new HBox(10);
                ristoranteBox.setStyle(
                        "-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.9);" +
                        "-fx-background-radius: 10; -fx-border-color: black;"
                );

                if (immagineURL != null && !immagineURL.isEmpty()) {
                    try {
                        ImageView imgView = new ImageView(
                                new Image(new FileInputStream("src/Image/" + immagineURL), 100, 100, true, true)
                        );
                        ristoranteBox.getChildren().add(imgView);
                    } catch (Exception e) {
                        System.out.println("Immagine non trovata: " + immagineURL);
                    }
                }

                VBox infoBox = new VBox(5);
                Label nomeLabel = new Label(nomeRistorante);
                nomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                Label descrizioneLabel = new Label(descrizione != null ? descrizione : "");
                infoBox.getChildren().addAll(nomeLabel, descrizioneLabel);

                ristoranteBox.getChildren().add(infoBox);

                int finalRistoranteId = ristoranteId;
                ristoranteBox.setOnMouseClicked(e -> apriPaginaRistorante(finalRistoranteId));

                resultBox.getChildren().add(ristoranteBox);
        }
    }

    private void apriPaginaRistorante(int ristoranteId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Ristorante.fxml"));
            Scene scene = new Scene(loader.load());
            RistoranteController controller = loader.getController();

            controller.setRistorante(ristoranteId);
            controller.refreshCartUI();


            SceneManager.pushScene(scene, controller, "ristorante");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/home.fxml"));
            Stage stage = (Stage) resultRoot.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/CSS/SceneCSS.css").toExternalForm());
            stage.setScene(scene);
            SceneManager.clearStack();
            SceneManager.pushScene(scene, loader.getController(), "home");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSceneShown() {
        refreshCartUI();
    }
}
