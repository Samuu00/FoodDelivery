package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash SHA-256 delle password.
 * ponytail: SHA-256 senza salt, compatibile col DB esistente; in produzione usare BCrypt/Argon2.
 */
public final class Passwords {
    private Passwords() {}

    public static String hash(String password) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
