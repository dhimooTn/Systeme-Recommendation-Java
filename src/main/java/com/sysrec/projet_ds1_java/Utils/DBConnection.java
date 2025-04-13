package com.sysrec.projet_ds1_java.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/database/my.db";
    private static Connection connection = null;

    // Return a singleton connection to the SQLite database
    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("✅ Connected to SQLite database.");
            } catch (SQLException e) {
                System.err.println("❌ Failed to connect to SQLite database: " + e.getMessage());
            }
        }
        return connection;
    }

    // Optional method to close the connection (not always used)
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 SQLite connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing SQLite connection: " + e.getMessage());
        }
    }
}
