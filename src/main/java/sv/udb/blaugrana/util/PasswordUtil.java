package sv.udb.blaugrana.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad de hashing de contrasenas con SHA-256.
 * Uso academico: en un sistema productivo se recomienda una funcion
 * con costo ajustable como BCrypt o Argon2.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String textoPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesHash = digest.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytesHash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible", e);
        }
    }

    public static boolean coincide(String textoPlano, String hashAlmacenado) {
        return hash(textoPlano).equalsIgnoreCase(hashAlmacenado);
    }
}
