package AI;

import DatabaseConn.DatabaseConnection;
import Utils.ConfigManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AIAgent {

    private static final Logger LOGGER = Logger.getLogger(AIAgent.class.getName());
    private final String apiKey;

    public AIAgent() {
        String envKey = System.getenv("OPENAI_API_KEY");
        // Use environment variable if present, otherwise fallback to config.properties
        this.apiKey = (envKey != null && !envKey.isEmpty()) ? envKey : ConfigManager.getProperty("api.key");
        
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            LOGGER.warning("API Key NON trovata");
        }
    }

    private String getDBExample() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM Ristoranti");
            sb.append("Ristoranti:\n");
            while(rs.next()) {
                sb.append(rs.getInt("RistoranteID")).append(", ")
                        .append(rs.getString("Nome")).append(", ")
                        .append(rs.getString("Descrizione")).append(", ")
                        .append(rs.getString("ImmagineURL")).append("\n");
            }
            rs.close();

            ResultSet rs1 = stmt.executeQuery("SELECT * FROM Menu");
            sb.append("Menu:\n");
            while(rs1.next()) {
                sb.append(rs1.getInt("MenuID")).append(", ")
                        .append(rs1.getInt("RistoranteID")).append(", ")
                        .append(rs1.getString("NomePiatto")).append(", ")
                        .append(rs1.getDouble("Prezzo")).append(", ")
                        .append(rs1.getString("ImmagineURL")).append(", ")
                        .append(rs1.getString("Categoria")).append("\n");
            }
            rs1.close();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB example for AI Context", e);
        }
        return sb.toString();
    }


    public String generateSQL(String userRequest) {
        if (apiKey == null) return null;

        String schema = "Ristoranti(RistoranteID INT, Nome VARCHAR, Descrizione text, Indirizzo VARCHAR, ImmagineURL VARCHAR)" +
                        "Menu(MenuID INT, RistoranteID INT, NomePiatto VARCHAR, Prezzo Decimal(6, 2), ImmagineURL VARCHAR, Categoria VARCHAR)";

        String dbExample = getDBExample();

        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("model", "gpt-4");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "Sei un assistente SQL esperto. Schema:\n" + schema + "\nEsempi dati:\n" + dbExample));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", "Genera **solo** una query SELECT DISTINCT valida MySQL assicurati che la query **restituisca sempre RistoranteID, Nome, Descrizione e ImmagineURL** per " + userRequest + ". Nessun commento o testo aggiuntivo."));

            jsonInput.put("messages", messages);
            jsonInput.put("max_tokens", 200);

            try(OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.toString().getBytes("utf-8"));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante la chiamata API a OpenAI", e);
            // Fallback content on failure
            return "SELECT RistoranteID, Nome, Descrizione, ImmagineURL FROM Ristoranti LIMIT 10;";


        }
    }
}
