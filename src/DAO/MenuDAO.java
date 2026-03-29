package DAO;
import Model.*;

import DatabaseConn.DatabaseConnection;
import Model.Restaurant; // Assumendo che venga spostato
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for handling Menu logic.
 */
public class MenuDAO {
    private static final Logger LOGGER = Logger.getLogger(MenuDAO.class.getName());

    /**
     * Recupera le info principali di un ristorante.
     */
    public Restaurant getRestaurantInfo(int ristoranteId) throws SQLException {
        String query = "SELECT RistoranteID, Nome, Descrizione, Indirizzo, ImmagineURL FROM Ristoranti WHERE RistoranteID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ristoranteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Restaurant(
                        rs.getInt("RistoranteID"),
                        rs.getString("Nome"),
                        rs.getString("Descrizione"),
                        rs.getString("ImmagineURL"),
                        rs.getString("Indirizzo")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Ottiene l'ID del menu partendo dal nome del piatto (usato storicamente da Ordini).
     */
    public int getMenuIdByName(String nomePiatto) throws SQLException {
        String sql = "SELECT MenuID FROM Menu WHERE NomePiatto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomePiatto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("MenuID");
            }
        }
        throw new SQLException("Menu non trovato: " + nomePiatto);
    }

    /**
     * Retrieves the menu items for a particular restaurant.
     */
    public java.util.List<Model.MenuItem> getMenuItemsByRestaurant(int ristoranteId) throws SQLException {
        java.util.List<Model.MenuItem> items = new java.util.ArrayList<>();
        String query = "SELECT MenuID, NomePiatto, Prezzo, ImmagineURL, Categoria " +
                       "FROM Menu WHERE RistoranteID =? ORDER BY Categoria";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, ristoranteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new Model.MenuItem(
                        rs.getInt("MenuID"),
                        rs.getString("NomePiatto"),
                        rs.getDouble("Prezzo"),
                        rs.getString("ImmagineURL"),
                        rs.getString("Categoria")
                    ));
                }
            }
        }
        return items;
    }
}
