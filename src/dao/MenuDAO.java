package dao;

import model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    /** Piatti del ristorante, ordinati per categoria. */
    public List<MenuItem> byRestaurant(int restaurantId) throws SQLException {
        List<MenuItem> out = new ArrayList<>();
        String sql = "SELECT MenuID, NomePiatto, Prezzo, ImmagineURL, Categoria FROM Menu "
                + "WHERE RistoranteID = ? ORDER BY Categoria, NomePiatto";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new MenuItem(rs.getInt("MenuID"), rs.getString("NomePiatto"),
                            rs.getDouble("Prezzo"), rs.getString("ImmagineURL"), rs.getString("Categoria")));
                }
            }
        }
        return out;
    }
}
