package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /** @return l'utente se le credenziali sono corrette, altrimenti null */
    public User authenticate(String email, String passwordHash) throws SQLException {
        String sql = "SELECT Username, Email FROM Utenti WHERE Email = ? AND Password = ?";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new User(rs.getString("Username"), rs.getString("Email")) : null;
            }
        }
    }

    public void register(String username, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO Utenti (Username, Email, Password) VALUES (?, ?, ?)";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }

    /** @return true se l'utente esisteva ed è stato aggiornato */
    public boolean updatePassword(String username, String passwordHash) throws SQLException {
        String sql = "UPDATE Utenti SET Password = ? WHERE Username = ?";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }
}
