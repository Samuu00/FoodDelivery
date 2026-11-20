package dao;

import model.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RestaurantDAO {

    private static final String COLS = "r.RistoranteID, r.Nome, r.Descrizione, r.Indirizzo, r.ImmagineURL, "
            + "GROUP_CONCAT(DISTINCT m.Categoria ORDER BY m.Categoria) AS Categorie";
    private static final String FROM = " FROM Ristoranti r LEFT JOIN Menu m ON r.RistoranteID = m.RistoranteID ";
    private static final String GROUP = " GROUP BY r.RistoranteID, r.Nome, r.Descrizione, r.Indirizzo, r.ImmagineURL ORDER BY r.Nome";

    /** Tutti i ristoranti con le loro categorie. */
    public List<Restaurant> all() throws SQLException {
        return query("SELECT " + COLS + FROM + GROUP);
    }

    public Restaurant byId(int id) throws SQLException {
        List<Restaurant> r = query("SELECT " + COLS + FROM + "WHERE r.RistoranteID = ?" + GROUP, String.valueOf(id));
        return r.isEmpty() ? null : r.get(0);
    }

    public List<Restaurant> byCategory(String category) throws SQLException {
        return query("SELECT " + COLS + FROM + "WHERE r.RistoranteID IN (SELECT RistoranteID FROM Menu WHERE Categoria = ?)" + GROUP, category);
    }

    /** Ricerca testuale su nome, descrizione, piatti e categoria. Funziona anche senza AI. */
    public List<Restaurant> search(String text) throws SQLException {
        String like = "%" + text.trim() + "%";
        return query("SELECT " + COLS + FROM + "WHERE r.RistoranteID IN (SELECT r2.RistoranteID FROM Ristoranti r2 "
                + "LEFT JOIN Menu m2 ON r2.RistoranteID = m2.RistoranteID WHERE r2.Nome LIKE ? OR r2.Descrizione LIKE ? "
                + "OR m2.NomePiatto LIKE ? OR m2.Categoria LIKE ?)" + GROUP, like, like, like, like);
    }

    /**
     * Esegue la query generata dall'AI, solo se è una singola SELECT, e ne usa solo gli ID dei ristoranti.
     * ponytail: filtro a parole chiave; la difesa vera è un utente DB con soli permessi SELECT.
     */
    public List<Integer> runAiQuery(String sql) throws SQLException {
        String safe = sanitizeSelect(sql);
        if (safe == null) throw new SQLException("Query AI rifiutata: " + sql);
        List<Integer> ids = new ArrayList<>();
        try (Connection c = Db.connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(safe)) {
            while (rs.next()) ids.add(rs.getInt("RistoranteID"));
        }
        return ids;
    }

    private static final Set<String> FORBIDDEN = Set.of(
            "insert", "update", "delete", "drop", "alter", "create", "truncate", "grant", "revoke",
            "replace", "rename", "call", "load", "outfile", "dumpfile", "sleep", "benchmark");

    /** @return la SELECT ripulita (senza ``` e ; finale), o null se non sicura */
    static String sanitizeSelect(String sql) {
        if (sql == null) return null;
        String s = sql.replace("```sql", "").replace("```", "").trim();
        while (s.endsWith(";")) s = s.substring(0, s.length() - 1).trim();
        String lower = s.toLowerCase();
        if (!lower.startsWith("select") || lower.contains(";") || lower.contains("--") || lower.contains("/*")) return null;
        for (String word : lower.split("[^a-z_]+")) {
            if (FORBIDDEN.contains(word)) return null;
        }
        return s;
    }

    private List<Restaurant> query(String sql, String... params) throws SQLException {
        List<Restaurant> out = new ArrayList<>();
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    /** Colonna come stringa, vuota se nulla o assente. */
    private static Restaurant map(ResultSet rs) throws SQLException {
        String cats = col(rs, "Categorie");
        return new Restaurant(rs.getInt("RistoranteID"), col(rs, "Nome"), col(rs, "Descrizione"),
                col(rs, "ImmagineURL"), col(rs, "Indirizzo"), cats.isEmpty() ? List.of() : Arrays.asList(cats.split(",")));
    }

    private static String col(ResultSet rs, String name) {
        try {
            String v = rs.getString(name);
            return v == null ? "" : v;
        } catch (SQLException missing) {
            return "";
        }
    }

    public static void main(String[] args) {
        assert sanitizeSelect("```sql\nSELECT * FROM Ristoranti;\n```").equals("SELECT * FROM Ristoranti");
        assert sanitizeSelect("select 1; drop table Utenti") == null;
        assert sanitizeSelect("DELETE FROM Utenti") == null;
        assert sanitizeSelect("SELECT * FROM Ristoranti WHERE 1=1 -- x") == null;
        assert sanitizeSelect("SELECT Nome FROM Ristoranti r JOIN Menu m ON r.updated = 1") != null;
        System.out.println("sanitizeSelect OK");
    }
}
