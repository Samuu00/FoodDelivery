package ai;

import dao.Db;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Traduce una richiesta in linguaggio naturale in una SELECT sui ristoranti (OpenAI). */
public class AIAgent {
    private static final Logger LOGGER = Logger.getLogger(AIAgent.class.getName());
    private static final String SCHEMA =
            "Ristoranti(RistoranteID INT, Nome VARCHAR, Descrizione TEXT, Indirizzo VARCHAR, ImmagineURL VARCHAR) "
            + "Menu(MenuID INT, RistoranteID INT, NomePiatto VARCHAR, Prezzo DECIMAL(6,2), ImmagineURL VARCHAR, Categoria VARCHAR)";

    private final String apiKey;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public AIAgent() {
        String env = System.getenv("OPENAI_API_KEY");
        apiKey = env != null && !env.isBlank() ? env : Config.get("api.key");
    }

    public boolean isAvailable() { return apiKey != null && !apiKey.isBlank(); }

    /** @return la query generata, o null se l'AI non è configurata o la chiamata fallisce */
    public String generateSQL(String userRequest) {
        if (!isAvailable()) return null;
        JSONArray messages = new JSONArray()
                .put(new JSONObject().put("role", "system").put("content",
                        "Sei un assistente SQL esperto. Schema: " + SCHEMA + " Dati di esempio: " + sampleData()))
                .put(new JSONObject().put("role", "user").put("content",
                        "Genera solo una query SELECT DISTINCT MySQL valida che restituisca sempre RistoranteID, Nome, "
                                + "Descrizione, Indirizzo e ImmagineURL dei ristoranti per: " + userRequest
                                + ". Nessun commento o testo aggiuntivo."));
        JSONObject body = new JSONObject().put("model", "gpt-4").put("messages", messages).put("max_tokens", 200);

        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                LOGGER.warning("OpenAI ha risposto " + res.statusCode());
                return null;
            }
            return new JSONObject(res.body()).getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content").trim();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Chiamata OpenAI fallita", e);
            return null;
        }
    }

    private String sampleData() {
        StringBuilder sb = new StringBuilder();
        try (Connection c = Db.connect(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT RistoranteID, Nome, Descrizione FROM Ristoranti")) {
                while (rs.next()) sb.append(rs.getInt(1)).append(", ").append(rs.getString(2)).append(", ").append(rs.getString(3)).append("; ");
            }
            try (ResultSet rs = st.executeQuery("SELECT RistoranteID, NomePiatto, Prezzo, Categoria FROM Menu")) {
                while (rs.next()) sb.append(rs.getInt(1)).append(", ").append(rs.getString(2)).append(", ")
                        .append(rs.getDouble(3)).append(", ").append(rs.getString(4)).append("; ");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossibile leggere i dati di esempio per l'AI", e);
        }
        return sb.toString();
    }
}
