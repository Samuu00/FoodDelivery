package DatabaseConn;

import Utils.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the connection to the database.
 */
public class DatabaseConnection {
    // Load credentials through the ConfigManager utility to avoid hardcoding strings.
    private static final String URL = ConfigManager.getProperty("db.url");
    private static final String USER = ConfigManager.getProperty("db.user");
    private static final String PASSWORD = ConfigManager.getProperty("db.password");

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Database configuration is missing or invalid.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
