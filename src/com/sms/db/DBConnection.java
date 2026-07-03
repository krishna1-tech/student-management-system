package com.sms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection manager.
 * Update URL / USER / PASSWORD to match your local MySQL setup.
 */
public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/management_system";
    private static final String USER = "root";
    private static final String PASSWORD = "2006";

    private static Connection connection;

    private DBConnection() {
        // utility class, no instances
    }

    /**
     * Returns a live shared connection, opening one if needed.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "MySQL JDBC driver not found on the classpath. " +
                        "Download mysql-connector-j and add it to /lib, then re-run with it on the classpath.", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /** Closes the shared connection, if open. Call when the application exits. */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
