package Controller;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {
    private final Connection conn;

    public AddressDAO(Connection conn) {
        this.conn = conn;
    }

    public void addAddress(String username, String address) throws SQLException {
        String sql = "INSERT INTO Indirizzi (Username, Indirizzo) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, address);
            stmt.executeUpdate();
        }
    }

    public List<String> getAddresses(String username) throws SQLException {
        List<String> addresses = new ArrayList<>();
        String sql = "SELECT Indirizzo FROM Indirizzi WHERE Username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                addresses.add(rs.getString("Indirizzo"));
            }
        }
        return addresses;
    }
}
