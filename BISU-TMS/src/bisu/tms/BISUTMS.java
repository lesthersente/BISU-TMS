
package bisu.tms;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class BISUTMS {

    public static void main(String[] args) {

        try {
            Database.initializeDatabase();

            SwingUtilities.invokeLater(() -> {
                new LoginForm().setVisible(true);
            });

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                null,
                "Database Error:\n" + ex.getMessage(),
                "BISU-TMS",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
