package Utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to load and manage configuration properties.
 * Provides a centralized way to access environment variables and application settings.
 */
public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.warning("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading configuration", ex);
        }
    }

    /**
     * Gets a configuration property by its key.
     *
     * @param key the property key
     * @return the property value, or null if not found
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
