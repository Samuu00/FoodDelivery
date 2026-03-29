package Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for security operations like password hashing.
 */
public class SecurityUtils {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());

    /**
     * Hashes a password using SHA-256 algorithm.
     * Note: In a production environment with sensitive data, 
     * algorithms like BCrypt or Argon2 should be preferred over SHA-256.
     *
     * @param password the plain text password
     * @return the hashed password in hexadecimal format, or null if an error occurs
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Algorithm not found for hashing", e);
            return null;
        }
    }

    /**
     * Helper method to convert byte array to hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
