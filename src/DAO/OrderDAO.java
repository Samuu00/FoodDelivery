package DAO;
import Model.*;

import DatabaseConn.DatabaseConnection;
import Model.Cart;
import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Order history queries.
 */
public class OrderDAO {
    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    public static class OrderItem {
        public int ordineId;
        public int numeroOrdine;
        public String stato;
        public double prezzoScontato;
        public Timestamp dataOrdine;

        public OrderItem(int ordineId, int numeroOrdine, String stato, double prezzoScontato, Timestamp dataOrdine) {
            this.ordineId = ordineId;
            this.numeroOrdine = numeroOrdine;
            this.stato = stato;
            this.prezzoScontato = prezzoScontato;
            this.dataOrdine = dataOrdine;
        }
    }

    /**
     * Recupera lo storico ordini per un determinato utente.
     */
    public List<OrderItem> getOrderHistory(String username) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM Ordini WHERE Username = ? ORDER BY DataOrdine DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderItem(
                        rs.getInt("OrdineID"),
                        rs.getInt("NumeroOrdine"),
                        rs.getString("Stato"),
                        rs.getDouble("PrezzoScontato"),
                        rs.getTimestamp("DataOrdine")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento storico ordini per utente: " + username, e);
        }
        return list;
    }

    /**
     * Riacquista un intero ordine passato reinserendolo nel carrello attuale.
     */
    public void reorderPastOrder(int ordineId, Cart cart) throws SQLException {
        String sql = "SELECT d.MenuID, d.Quantita, d.PrezzoUnitario, m.NomePiatto " +
                "FROM DettagliOrdine d " +
                "JOIN Menu m ON d.MenuID = m.MenuID " +
                "WHERE d.OrdineID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ordineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int menuId = rs.getInt("MenuID");
                    String nomePiatto = rs.getString("NomePiatto");
                    double prezzoUnitario = rs.getDouble("PrezzoUnitario");
                    int quantita = rs.getInt("Quantita");

                    for (int i = 0; i < quantita; i++) {
                        cart.add(nomePiatto, prezzoUnitario, menuId);
                    }
                }
            }
        }
    }
}
