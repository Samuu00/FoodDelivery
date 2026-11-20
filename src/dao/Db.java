package dao;

import util.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Connessione al DB con credenziali da config.properties. */
public final class Db {
    private static final String URL = Config.get("db.url");
    private static final String USER = Config.get("db.user");
    private static final String PASSWORD = Config.get("db.password");

    private Db() {}

    public static Connection connect() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Configurazione DB mancante: crea config.properties (vedi config.example.properties)");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
