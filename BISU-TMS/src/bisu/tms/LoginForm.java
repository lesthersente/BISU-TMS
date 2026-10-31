package bisu.tms;

import java.awt.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;

public class LoginForm extends JFrame {

    private static final Color PURPLE = new Color(33, 18, 90);
    private static final Color GOLD = new Color(247, 194, 56);
    private static final Color GREEN = new Color(32, 140, 91);
    private static final Color WHITE = Color.WHITE;

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginForm() {
        setTitle("BISU-TMS | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(530, 650);
        setMinimumSize(new Dimension(450, 600));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PURPLE);
        root.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        setContentPane(root);

        // HEADER
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel logo = makeLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(logo);
        header.add(Box.createVerticalStrut(15));

        JLabel title = new JLabel("BISU TOURNAMENT MANAGEMENT");
        title.setForeground(GOLD);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);

        header.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Tournament Administration Login Portal");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // CENTER FORM PANEL - NOW PURPLE TOO
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 25, 40));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(userLabel);
        centerPanel.add(Box.createVerticalStrut(8));

        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        usernameField.setPreferredSize(new Dimension(100, 40));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createVerticalStrut(22));

        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(WHITE);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(passLabel);
        centerPanel.add(Box.createVerticalStrut(8));

        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField.setPreferredSize(new Dimension(100, 40));
        centerPanel.add(passwordField);
        centerPanel.add(Box.createVerticalStrut(28));

        JButton loginButton = new JButton("LOGIN");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(GREEN);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setPreferredSize(new Dimension(100, 45));
        loginButton.addActionListener(e -> login());

        centerPanel.add(loginButton);

        root.add(centerPanel, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JLabel systemLabel = new JLabel("BISU-TMS  |  Java Swing + SQLite");
        systemLabel.setForeground(GOLD);
        systemLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel devLabel = new JLabel("Developed by: @boysaging");
        devLabel.setForeground(Color.WHITE);
        devLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        devLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(systemLabel);
        footer.add(Box.createVerticalStrut(8));
        footer.add(devLabel);

        root.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
    }

    private JLabel makeLogo() {

        URL url = getClass().getResource("/bisu/tms/assets/bisu_logo.png");

        if (url == null) {
            JLabel fallback = new JLabel("BISU", SwingConstants.CENTER);
            fallback.setForeground(GOLD);
            fallback.setFont(new Font("SansSerif", Font.BOLD, 30));
            return fallback;
        }

        Image image = new ImageIcon(url).getImage()
                .getScaledInstance(110, 110, Image.SCALE_SMOOTH);

        return new JLabel(new ImageIcon(image));
    }

    private void login() {

        String username = usernameField.getText()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        char[] password = passwordField.getPassword();

        try {
            if (username.isEmpty() || password.length == 0) {
                showError("Please enter your username and password.");
                return;
            }

            String sql = "SELECT password_hash, password_salt FROM users WHERE username = ?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next() ||
                        !PasswordUtil.verifyPassword(
                                password,
                                rs.getString("password_salt"),
                                rs.getString("password_hash"))) {

                        showError("Invalid username or password.");
                        return;
                    }
                }
            }

            new MyDashboard(username).setVisible(true);
            dispose();

        } catch (SQLException | GeneralSecurityException ex) {
            showError("Login failed: " + ex.getMessage());

        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "BISU-TMS",
                JOptionPane.ERROR_MESSAGE
        );
    }
}