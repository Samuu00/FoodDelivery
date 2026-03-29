package DAO;
import Model.*;

import Controller.Restaurant;
import DatabaseConn.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Restaurant related queries.
 */
public class RestaurantDAO {
    private static final Logger LOGGER = Logger.getLogger(RestaurantDAO.class.getName());

    /**
     * Executes a dynamic custom query to get a list of restaurants.
     * Often used in conjunction with the AI generated queries.
     *
     * @param query the SQL query string
     * @return a list of Restaurant objects
     */
    public List<Restaurant> getRestaurantsByCustomQuery(String query) {
        List<Restaurant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                list.add(extractRestaurant(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nella query dinamica ristoranti: " + query, e);
        }
        return list;
    }

    /**
     * Retrieves restaurants filtered by a specific category.
     *
     * @param categoria the category name (e.g., 'Pizza', 'Sushi')
     * @return a list of Restaurant objects
     */
    public List<Restaurant> getRestaurantsByCategory(String categoria) {
        List<Restaurant> list = new ArrayList<>();
        String sql = "SELECT DISTINCT r.RistoranteID, r.Nome, r.Descrizione, r.Indirizzo, r.ImmagineURL " +
                     "FROM Ristoranti r " +
                     "JOIN Menu m ON r.RistoranteID = m.RistoranteID " +
                     "WHERE m.Categoria = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractRestaurant(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nella ricerca ristoranti per categoria", e);
        }
        return list;
    }

    /**
     * Helper to safely extract a Restaurant object from ResultSet.
     */
    private Restaurant extractRestaurant(ResultSet rs) {
        try {
            int ristoranteId = rs.getInt("RistoranteID");
            String nome = getColumnSafe(rs, "Nome");
            String descrizione = getColumnSafe(rs, "Descrizione");
            String immagineURL = getColumnSafe(rs, "ImmagineURL");
            String indirizzo = getColumnSafe(rs, "Indirizzo");
            return new Restaurant(ristoranteId, nome, descrizione, immagineURL, indirizzo);
        } catch (Exception e) {
            return new Restaurant(0, "Senza Nome", "", "", "");
        }
    }

    private String getColumnSafe(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (Exception e) {
            return "";
        }
    }
}
