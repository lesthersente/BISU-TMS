
package bisu.tms;

import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;
import javax.swing.*;

public class AdminSetup {

    public static void main(String[] args) {

        String username = "lesther";

        JPasswordField passwordField =
                new JPasswordField(20);

        int choice = JOptionPane.showConfirmDialog(
                null,
                passwordField,
                "Enter the password for admin: lesther",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        char[] password = passwordField.getPassword();

        try {
            if (password.length < 8) {
                JOptionPane.showMessageDialog(
                    null,
                    "Password must contain at least 8 characters."
                );
                return;
            }

            Database.initializeDatabase();

            try (Connection conn = Database.getConnection()) {

                conn.setAutoCommit(false);

                try {
                    int existingId = -1;
                    int count = 0;

                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT id FROM users")) {

                        while (rs.next()) {
                            count++;
                            existingId = rs.getInt("id");
                        }
                    }

                    if (count > 1) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(
                            null,
                            "More than one account exists. " +
                            "No changes were made."
                        );
                        return;
                    }

                    String salt = PasswordUtil.createSalt();
                    String hash = PasswordUtil.hashPassword(
                            password, salt);

                    String sql;

                    if (count == 0) {
                        sql = "INSERT INTO users " +
                              "(username, password_hash, " +
                              "password_salt, role) " +
                              "VALUES (?, ?, ?, 'ADMIN')";
                    } else {
                        sql = "UPDATE users SET " +
                              "username = ?, password_hash = ?, " +
                              "password_salt = ?, role = 'ADMIN' " +
                              "WHERE id = ?";
                    }

                    try (PreparedStatement ps =
                            conn.prepareStatement(sql)) {

                        ps.setString(1, username);
                        ps.setString(2, hash);
                        ps.setString(3, salt);

                        if (count == 1) {
                            ps.setInt(4, existingId);
                        }

                        ps.executeUpdate();
                    }

                    conn.commit();

                    JOptionPane.showMessageDialog(
                        null,
                        "BISU-TMS ADMIN ACCOUNT SAVED!\n\n" +
                        "Username: " + username + "\n" +
                        "Your chosen password is now active.\n\n" +
                        "You can now log in."
                    );

                } catch (SQLException |
                         GeneralSecurityException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (SQLException |
                 GeneralSecurityException ex) {

            JOptionPane.showMessageDialog(
                null,
                "Admin setup failed: " + ex.getMessage()
            );

        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }
}
