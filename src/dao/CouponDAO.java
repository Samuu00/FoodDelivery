package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CouponDAO {

    /** @return percentuale di sconto, oppure -1 se il coupon non esiste o non è dell'utente */
    public double discountPercent(String code, String username) throws SQLException {
        String sql = "SELECT Sconto FROM coupon WHERE Codice = ? AND (Username = ? OR Username IS NULL)";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("Sconto") : -1;
            }
        }
    }
}
