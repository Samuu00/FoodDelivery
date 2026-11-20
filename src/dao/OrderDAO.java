package dao;

import model.Cart;
import model.Order;
import model.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    /** Ordini dell'utente, con ristorante e riepilogo piatti, dal più recente. */
    public List<Order> history(String username) throws SQLException {
        String sql = "SELECT o.OrdineID, o.NumeroOrdine, o.Stato, o.PrezzoScontato, o.DataOrdine, "
                + "MIN(r.RistoranteID) AS RId, MIN(r.Nome) AS RNome, MIN(r.ImmagineURL) AS RImg, "
                + "GROUP_CONCAT(CONCAT(d.Quantita, '× ', m.NomePiatto) ORDER BY m.NomePiatto SEPARATOR ', ') AS Piatti "
                + "FROM Ordini o JOIN DettagliOrdine d ON d.OrdineID = o.OrdineID "
                + "JOIN Menu m ON m.MenuID = d.MenuID JOIN Ristoranti r ON r.RistoranteID = m.RistoranteID "
                + "WHERE o.Username = ? GROUP BY o.OrdineID, o.NumeroOrdine, o.Stato, o.PrezzoScontato, o.DataOrdine "
                + "ORDER BY o.DataOrdine DESC";
        List<Order> out = new ArrayList<>();
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Order(rs.getInt("OrdineID"), rs.getInt("NumeroOrdine"), rs.getString("Stato"),
                            rs.getDouble("PrezzoScontato"), rs.getTimestamp("DataOrdine").toLocalDateTime(),
                            rs.getInt("RId"), rs.getString("RNome"), rs.getString("RImg"), rs.getString("Piatti")));
                }
            }
        }
        return out;
    }

    /** Sostituisce il carrello con i piatti di un ordine passato (stesso ristorante, prezzi attuali). */
    public void reorder(Order order, Cart cart) throws SQLException {
        String sql = "SELECT m.MenuID, m.NomePiatto, m.Prezzo, d.Quantita FROM DettagliOrdine d "
                + "JOIN Menu m ON d.MenuID = m.MenuID WHERE d.OrdineID = ?";
        Restaurant r = new Restaurant(order.restaurantId(), order.restaurantName(), "", order.restaurantImage(), "", List.of());
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, order.id());
            try (ResultSet rs = ps.executeQuery()) {
                cart.clear();
                while (rs.next()) {
                    for (int i = 0; i < rs.getInt("Quantita"); i++) {
                        cart.add(r, rs.getInt("MenuID"), rs.getString("NomePiatto"), rs.getDouble("Prezzo"));
                    }
                }
            }
        }
    }
}
