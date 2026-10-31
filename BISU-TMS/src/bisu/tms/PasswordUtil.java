
package bisu.tms;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 150000;
    private static final int KEY_LENGTH = 256;

    public static String createSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(char[] password, String salt)
            throws GeneralSecurityException {

        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password, saltBytes, ITERATIONS, KEY_LENGTH);

        try {
            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();

            try {
                return Base64.getEncoder().encodeToString(hash);
            } finally {
                Arrays.fill(hash, (byte) 0);
            }
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(
            char[] password, String salt, String expectedHash)
            throws GeneralSecurityException {

        byte[] expected = Base64.getDecoder().decode(expectedHash);
        byte[] actual = Base64.getDecoder().decode(
                hashPassword(password, salt));

        try {
            return MessageDigest.isEqual(actual, expected);
        } finally {
            Arrays.fill(actual, (byte) 0);
            Arrays.fill(expected, (byte) 0);
        }
    }
}
