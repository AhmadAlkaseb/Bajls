package app.auth;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordSupport {
    private PasswordSupport() {
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, storedPassword);
        } catch (IllegalArgumentException e) {
            return storedPassword.equals(rawPassword);
        }
    }
}
