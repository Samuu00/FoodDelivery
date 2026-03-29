package Service;

import DAO.MenuDAO;
import DatabaseConn.DatabaseConnection;
import Model.Cart;
import Model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Layer for Order Operations.
 * Manages atomic transactions over database tables to ensure data integrity.
 */
public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());
    private final MenuDAO menuDAO = new MenuDAO();

    /**
     * Salva un ordine nel database in modo transazionale (ACID).
     *
     * @param user L'utente che effettua l'ordine
     * @param cart Il carrello contenente gli articoli
     * @throws SQLException Se l'operazione fallisce, causa un rollback
     */
    public void placeOrder(User user, Cart cart) throws SQLException {
        if (user == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Utente o carrello non validi");
        }

        String insertOrdine = "INSERT INTO Ordini (Username, DataOrdine, Stato, PrezzoTotale, NumeroOrdine, PrezzoScontato) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDettaglio = "INSERT INTO DettagliOrdine (OrdineID, MenuID, Quantita, PrezzoUnitario) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Inizio Transazione

            // Genera numero ordine (progressivo)
            String queryMax = "SELECT MAX(NumeroOrdine) AS MaxOrdine FROM Ordini WHERE Username = ?";
            int numeroOrdine = 1;
            try (PreparedStatement psMax = conn.prepareStatement(queryMax)) {
                psMax.setString(1, user.getUsername());
                try (ResultSet rsMax = psMax.executeQuery()) {
                    if (rsMax.next()) {
                        numeroOrdine = rsMax.getInt("MaxOrdine") + 1;
                    }
                }
            }

            // Inserisci l'ordine principale
            int ordineId = -1;
            try (PreparedStatement psOrdine = conn.prepareStatement(insertOrdine, Statement.RETURN_GENERATED_KEYS)) {
                psOrdine.setString(1, user.getUsername());
                psOrdine.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                psOrdine.setString(3, "In elaborazione");

                double prezzoTotale = cart.getItems().stream().mapToDouble(i -> i.price * i.quantity).sum();
                psOrdine.setDouble(4, prezzoTotale);
                psOrdine.setInt(5, numeroOrdine);
                psOrdine.setDouble(6, cart.getDiscountedTotal());

                psOrdine.executeUpdate();

                try (ResultSet rs = psOrdine.getGeneratedKeys()) {
                    if (rs.next()) {
                        ordineId = rs.getInt(1);
                    } else {
                        throw new SQLException("Impossibile recuperare l'ID dell'ordine generato.");
                    }
                }
            }

            // Inserisci i dettagli dell'ordine
            try (PreparedStatement psDettaglio = conn.prepareStatement(insertDettaglio)) {
                for (Cart.Item item : cart.getItems()) {
                    int menuId;
                    if (item.menuId <= 0) {
                        menuId = menuDAO.getMenuIdByName(item.name);
                    } else {
                        menuId = item.menuId;
                    }

                    psDettaglio.setInt(1, ordineId);
                    psDettaglio.setInt(2, menuId);
                    psDettaglio.setInt(3, item.quantity);
                    psDettaglio.setDouble(4, item.price);
                    psDettaglio.addBatch();
                }
                psDettaglio.executeBatch();
            }

            conn.commit(); // Conferma la transazione se tutto ha successo
            LOGGER.info("Ordine transazionale piazzato con ID: " + ordineId);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    LOGGER.warning("Rollback della transazione ordine a causa di un errore");
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Errore durante il rollback", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Errore chiusura connessione", e);
                }
            }
        }
    }
}
