package com.sysrec.projet_ds1_java.Utils;

import java.sql.*;

public class Dbm {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/database/my.db";

    public static void main(String[] args) {
        initializeDatabase();
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Enable foreign key constraints
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }

            // Create tables and insert data in a transaction
            conn.setAutoCommit(false);
            try {
                createTables(conn);
                insertSampleData(conn);
                conn.commit();
                System.out.println("✅ Database initialized successfully!");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Transaction rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Drop tables if they exist (in reverse order of foreign key dependencies)
            stmt.execute("DROP TABLE IF EXISTS RessourceInteraction;");
            stmt.execute("DROP TABLE IF EXISTS Resources;");
            stmt.execute("DROP TABLE IF EXISTS Users;");

            // Create Users table
            stmt.execute("""
                CREATE TABLE Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role TEXT CHECK(role IN ('student', 'teacher')) NOT NULL
                );
            """);

            // Create Resources table
            stmt.execute("""
                CREATE TABLE Resources (
                    resource_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    difficulty TEXT CHECK(difficulty IN ('easy', 'medium', 'hard')) NOT NULL,
                    category TEXT CHECK(category IN ('computer_science', 'economic_science', 'economic_science')) NOT NULL,
                    keywords VARCHAR(255),
                    teacher_id INTEGER NOT NULL,
                    is_approved BOOLEAN DEFAULT 0 NOT NULL,
                    is_private BOOLEAN DEFAULT 0 NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    FOREIGN KEY (teacher_id) REFERENCES Users(user_id) ON DELETE CASCADE
                );
            """);

            // Create RessourceInteraction table
            stmt.execute("""
                CREATE TABLE RessourceInteraction (
                    interaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    resource_id INTEGER NOT NULL,
                    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
                    rated_at DATETIME,
                    FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE CASCADE,
                    UNIQUE(student_id, resource_id)
                );
            """);
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        // Insert users
        try (PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, ?)")) {

            // Students
            insertUser(userStmt, "Alice", "alice@student.com", "pass123", "student");
            insertUser(userStmt, "Bob", "bob@student.com", "pass456", "student");
            insertUser(userStmt, "Charlie", "charlie@student.com", "pass789", "student");
            insertUser(userStmt, "Diana", "diana@student.com", "pass000", "student");

            // Teachers
            insertUser(userStmt, "Dr. Smith", "smith@univ.com", "teachpass", "teacher");
            insertUser(userStmt, "Prof. Johnson", "johnson@univ.com", "johnpass", "teacher");
        }

        // Insert resources
        try (PreparedStatement resourceStmt = conn.prepareStatement(
                "INSERT INTO Resources (title, description, difficulty, category, keywords, teacher_id, is_approved, is_private) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Teacher 5 (Dr. Smith)
            insertResource(resourceStmt, "Intro to CS", "Basic concepts in CS", "easy",
                    "computer_science", "CS, intro", 5, true, false);
            insertResource(resourceStmt, "Advanced Algorithms", "Complex algorithmic strategies", "hard",
                    "computer_science", "algorithms,graphs,dp", 5, true, true);

            // Teacher 6 (Prof. Johnson)
            insertResource(resourceStmt, "Microeconomics Basics", "Intro to microeconomics", "medium",
                    "economic_science", "micro,economy,demand", 6, true, false);
            insertResource(resourceStmt, "Macro Theory", "Advanced macroeconomic theory", "hard",
                    "economic_science", "macro,gdp,inflation", 6, true, true);
            insertResource(resourceStmt, "Management 101", "Principles of management", "easy",
                    "management_science", "planning,organizing,HR", 6, true, false);
            insertResource(resourceStmt, "Operations Management", "Efficiency and processes", "medium",
                    "management_science", "process,optimization", 6, true, true);
        }

        // Insert interactions
        try (PreparedStatement interactionStmt = conn.prepareStatement(
                "INSERT INTO RessourceInteraction (student_id, resource_id, rating) VALUES (?, ?, ?)")) {

            // Student 1 (Alice)
            insertInteraction(interactionStmt, 1, 1, 5);
            insertInteraction(interactionStmt, 1, 2, 4);
            insertInteraction(interactionStmt, 1, 6, 3);

            // Student 2 (Bob)
            insertInteraction(interactionStmt, 2, 1, 4);
            insertInteraction(interactionStmt, 2, 3, 5);
            insertInteraction(interactionStmt, 2, 5, 2);

            // Student 3 (Charlie)
            insertInteraction(interactionStmt, 3, 4, 3);
            insertInteraction(interactionStmt, 3, 2, 2);

            // Student 4 (Diana)
            insertInteraction(interactionStmt, 4, 5, 4);
            insertInteraction(interactionStmt, 4, 6, 5);
        }
    }

    private static void insertUser(PreparedStatement stmt, String name, String email,
                                   String password, String role) throws SQLException {
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, password);
        stmt.setString(4, role);
        stmt.executeUpdate();
    }

    private static void insertResource(PreparedStatement stmt, String title, String description,
                                       String difficulty, String category, String keywords,
                                       int teacherId, boolean isApproved, boolean isPrivate) throws SQLException {
        stmt.setString(1, title);
        stmt.setString(2, description);
        stmt.setString(3, difficulty);
        stmt.setString(4, category);
        stmt.setString(5, keywords);
        stmt.setInt(6, teacherId);
        stmt.setBoolean(7, isApproved);
        stmt.setBoolean(8, isPrivate);
        stmt.executeUpdate();
    }

    private static void insertInteraction(PreparedStatement stmt, int studentId,
                                          int resourceId, int rating) throws SQLException {
        stmt.setInt(1, studentId);
        stmt.setInt(2, resourceId);
        stmt.setInt(3, rating);
        stmt.executeUpdate();
    }
}