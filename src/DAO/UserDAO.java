package DAO;
import Model.*;

import Controller.User;
import DatabaseConn.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User related queries.
 * Handles authentication, registration and other user operations.
 */
public class UserDAO {

    /**
     * Authenticates a user against the database.
     *
     * @param email the user's email
     * @param hashedPassword the user's hashed password
     * @return the User object if authentication is successful, null otherwise
     * @throws SQLException if a database access error occurs
     */
    public User authenticate(String email, String hashedPassword) throws SQLException {
        String query = "SELECT Username, Email FROM Utenti WHERE Email = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("Username"), rs.getString("Email"));
                }
            }
        }
        return null;
    }

    /**
     * Registers a new user in the database.
     *
     * @param username the username
     * @param email the user email
     * @param hashedPassword the hashed password
     * @throws SQLException if a database access error occurs or constraints are violated
     */
    public void registerUser(String username, String email, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO Utenti (Username, Email, Password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.executeUpdate();
        }
    }

    /**
     * Checks if a user exists by username.
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean userExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Utenti WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Updates the password for a given username.
     *
     * @param username the username to update
     * @param newHashedPassword the new hashed password
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean updatePassword(String username, String newHashedPassword) throws SQLException {
        String update = "UPDATE Utenti SET Password = ? WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
             
            ps.setString(1, newHashedPassword);
            ps.setString(2, username);
            
            int rows = ps.executeUpdate();
            
            // For safety, ensure auto-commit behavior or manual commit if needed 
            // The original logic had !conn.getAutoCommit() block, we keep it safe:
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
            return rows > 0;
        }
    }
}
