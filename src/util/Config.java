package util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Legge config.properties dal classpath (non versionato, vedi config.example.properties). */
public final class Config {
    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) LOGGER.warning("config.properties non trovato nel classpath");
            else PROPS.load(in);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore lettura config.properties", e);
        }
    }

    private Config() {}

    public static String get(String key) { return PROPS.getProperty(key); }
}
