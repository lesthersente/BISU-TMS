
package bisu.tms;

import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.*;

public class RegisterForm extends JFrame {

    private final LoginForm loginForm;

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);

    private static final Color PURPLE = new Color(33, 18, 90);
    private static final Color GOLD = new Color(247, 194, 56);
    private static final Color GREEN = new Color(32, 140, 91);

    public RegisterForm(LoginForm loginForm) {

        this.loginForm = loginForm;

        setTitle("BISU-TMS | Administrator Setup");
        setSize(500, 540);
        setMinimumSize(new Dimension(460, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(10, 15));
        root.setBackground(PURPLE);
        root.setBorder(
            BorderFactory.createEmptyBorder(25, 40, 25, 40)
        );

        setContentPane(root);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(
            new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("ADMINISTRATOR SETUP");
        title.setForeground(GOLD);
        title.setFont(
            new Font("SansSerif", Font.BOLD, 24)
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
            "Create your one-time administrator account"
        );
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(7, 0, 7, 0);

        JLabel userLabel = createLabel("Admin Username");
        JLabel passLabel = createLabel("Admin Password");
        JLabel confirmLabel = createLabel("Confirm Password");

        c.gridy = 0;
        form.add(userLabel, c);

        c.gridy++;
        usernameField.setPreferredSize(new Dimension(200, 38));
        form.add(usernameField, c);

        c.gridy++;
        form.add(passLabel, c);

        c.gridy++;
        passwordField.setPreferredSize(new Dimension(200, 38));
        form.add(passwordField, c);

        c.gridy++;
        form.add(confirmLabel, c);

        c.gridy++;
        confirmField.setPreferredSize(new Dimension(200, 38));
        form.add(confirmField, c);

        c.gridy++;
        c.insets = new Insets(20, 0, 5, 0);

        JButton createButton = new JButton(
            "CREATE ADMIN ACCOUNT"
        );

        createButton.setBackground(GREEN);
        createButton.setForeground(Color.WHITE);
        createButton.setFont(
            new Font("SansSerif", Font.BOLD, 15)
        );
        createButton.setPreferredSize(new Dimension(200, 45));

        createButton.addActionListener(e -> createAdmin());

        form.add(createButton, c);

        root.add(form, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "Developed by: BOYSAGING",
            SwingConstants.CENTER
        );

        footer.setForeground(GOLD);
        footer.setFont(
            new Font("SansSerif", Font.BOLD, 14)
        );

        root.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(createButton);
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(
            new Font("SansSerif", Font.BOLD, 14)
        );

        return label;
    }

    private void createAdmin() {

        String username = usernameField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();

        try {

            if (!username.matches("[a-z0-9._-]{3,30}")) {

                showError(
                    "Username must contain 3 to 30 valid characters."
                );
                return;
            }

            if (password.length < 8) {

                showError(
                    "Password must contain at least 8 characters."
                );
                return;
            }

            if (!Arrays.equals(password, confirm)) {

                showError("Passwords do not match.");
                return;
            }

            try (Connection conn = Database.getConnection()) {

                conn.setAutoCommit(false);

                try {

                    // Prevent creating another admin account
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT COUNT(*) FROM users"
                         )) {

                        if (rs.next() && rs.getInt(1) > 0) {

                            conn.rollback();

                            showError(
                                "An administrator account already exists."
                            );

                            return;
                        }
                    }

                    String salt = PasswordUtil.createSalt();

                    String hash = PasswordUtil.hashPassword(
                        password, salt
                    );

                    String sql =
                        "INSERT INTO users " +
                        "(username, password_hash, password_salt, role) " +
                        "VALUES (?, ?, ?, 'ADMIN')";

                    try (PreparedStatement ps =
                         conn.prepareStatement(sql)) {

                        ps.setString(1, username);
                        ps.setString(2, hash);
                        ps.setString(3, salt);

                        ps.executeUpdate();
                    }

                    conn.commit();

                } catch (SQLException |
                         GeneralSecurityException ex) {

                    conn.rollback();
                    throw ex;

                } finally {
                    conn.setAutoCommit(true);
                }
            }

            JOptionPane.showMessageDialog(
                this,
                "Administrator account created successfully!\n" +
                "You can now log in to BISU-TMS.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE
            );

            loginForm.setVisible(true);
            dispose();

        } catch (SQLException | GeneralSecurityException ex) {

            showError(
                "Unable to create administrator:\n" +
                ex.getMessage()
            );

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');

            passwordField.setText("");
            confirmField.setText("");
        }
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
            this,
            message,
            "BISU-TMS Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
