package service;

import dao.Db;
import model.Cart;
import model.User;

import java.sql.*;
import java.time.LocalDateTime;

/** Salvataggio ordine in un'unica transazione: o ordine + dettagli, o niente. */
public class OrderService {

    /** @return il numero progressivo dell'ordine per l'utente */
    public int placeOrder(User user, Cart cart) throws SQLException {
        if (user == null || cart.isEmpty()) throw new IllegalArgumentException("Utente o carrello non validi");

        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try {
                int number = 1;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(MAX(NumeroOrdine), 0) + 1 FROM Ordini WHERE Username = ?")) {
                    ps.setString(1, user.getUsername());
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) number = rs.getInt(1); }
                }

                int orderId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO Ordini (Username, DataOrdine, Stato, PrezzoTotale, NumeroOrdine, PrezzoScontato) "
                                + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(3, "In elaborazione");
                    ps.setDouble(4, cart.getSubtotal());
                    ps.setInt(5, number);
                    ps.setDouble(6, cart.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("ID ordine non generato");
                        orderId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO DettagliOrdine (OrdineID, MenuID, Quantita, PrezzoUnitario) VALUES (?, ?, ?, ?)")) {
                    for (Cart.Item item : cart.getItems()) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, item.menuId());
                        ps.setInt(3, item.quantity());
                        ps.setDouble(4, item.price());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return number;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
