package com.sms;

import com.sms.db.DBConnection;
import com.sms.ui.MainFrame;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Fail fast with a clear message if the database isn't reachable,
        // rather than letting the UI open and error out on first use.
        try {
            DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to the database.\n\n" + e.getMessage() +
                    "\n\nCheck that MySQL is running, the schema has been created, " +
                    "and the credentials in DBConnection.java are correct.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            new MainFrame().setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::close));
    }
}
