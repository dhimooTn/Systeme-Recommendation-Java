package com.sysrec.projet_ds1_java.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/database/my.db";
    private static volatile Connection connection = null;

    // Private constructor to prevent instantiation
    private DBConnection() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Gets a connection to the SQLite database.
     * Creates a new connection if one doesn't exist or if the existing one is closed.
     *
     * @return Connection object
     * @throws SQLException if database access error occurs
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                // Enable foreign key support
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
                System.out.println("✅ Connected to SQLite database.");
            } catch (SQLException e) {
                System.err.println("❌ Failed to connect to SQLite database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if it exists and is open.
     *
     * @throws SQLException if database access error occurs
     */
    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try {
                connection.close();
                System.out.println("🔌 SQLite connection closed.");
            } catch (SQLException e) {
                System.err.println("❌ Error closing SQLite connection: " + e.getMessage());
                throw e;
            } finally {
                connection = null; // Ensure connection is marked as closed
            }
        }
    }

    /**
     * Verifies if the connection is valid by executing a simple query.
     *
     * @return true if connection is valid, false otherwise
     */
    public static synchronized boolean isConnectionValid() {
        if (connection == null) {
            return false;
        }

        try {
            return connection.isValid(1); // 1 second timeout
        } catch (SQLException e) {
            System.err.println("❌ Error validating connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resets the connection by closing the existing one (if open) and creating a new one.
     *
     * @throws SQLException if database access error occurs
     */
    public static synchronized void resetConnection() throws SQLException {
        closeConnection();
        getConnection();
    }

    public static void main(String[] args) {
        try {
            // Test connection
            Connection conn = DBConnection.getConnection();
            Objects.requireNonNull(conn, "Connection should not be null");

            // Test connection validity
            if (isConnectionValid()) {
                System.out.println("Connection test successful");
            } else {
                System.err.println("Connection test failed");
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
        } finally {
            try {
                DBConnection.closeConnection();
            } catch (SQLException e) {
                System.err.println("Error during connection cleanup: " + e.getMessage());
            }
        }
    }
}