package DAO;
import Model.*;

import DatabaseConn.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Coupons.
 */
public class CouponDAO {
    private static final Logger LOGGER = Logger.getLogger(CouponDAO.class.getName());

    /**
     * Recupera la percentuale di sconto associata a un coupon valido.
     *
     * @param code     il codice inserito
     * @param username l'username, per coupon specifici per utente
     * @return percentuale di sconto, oppure -1 se non valido
     * @throws SQLException in caso di errore DB
     */
    public double getDiscountPercentage(String code, String username) throws SQLException {
        String sql = "SELECT Sconto FROM coupon WHERE Codice = ? AND (Username = ? OR Username IS NULL)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, code);
            ps.setString(2, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Sconto");
                }
            }
        }
        return -1; // -1 indica coupon non valido
    }
}
