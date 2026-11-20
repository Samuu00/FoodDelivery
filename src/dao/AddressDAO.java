package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    public void add(String username, String address) throws SQLException {
        String sql = "INSERT INTO Indirizzi (Username, Indirizzo) VALUES (?, ?)";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, address);
            ps.executeUpdate();
        }
    }

    public List<String> list(String username) throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT Indirizzo FROM Indirizzi WHERE Username = ?";
        try (Connection c = Db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("Indirizzo"));
            }
        }
        return out;
    }
}
