package com.sysrec.projet_ds1_java.Utils;

import java.sql.*;

public class Dbm {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:src/main/resources/database/my.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // === Drop Existing Tables if Needed ===
            stmt.execute("DROP TABLE IF EXISTS RessourceInteraction;");
            stmt.execute("DROP TABLE IF EXISTS SavedResources;");
            stmt.execute("DROP TABLE IF EXISTS Ratings;");
            stmt.execute("DROP TABLE IF EXISTS Resources;");
            stmt.execute("DROP TABLE IF EXISTS Users;");

            // === Create Users Table ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100),
                    email VARCHAR(100),
                    password VARCHAR(255),
                    role TEXT CHECK(role IN ('student', 'teacher'))
                );
            """);

            // === Create Resources Table ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Resources (
                    resource_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(255),
                    description TEXT,
                    difficulty TEXT CHECK(difficulty IN ('easy', 'medium', 'hard')),
                    category TEXT CHECK(category IN ('computer_science', 'economic_science', 'management_science')),
                    keywords VARCHAR(255),
                    teacher_id INTEGER,
                    is_approved BOOLEAN,
                    created_at DATETIME,
                    FOREIGN KEY (teacher_id) REFERENCES Users(user_id)
                );
            """);

            // === Create RessourceInteraction Table ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS RessourceInteraction (
                    interaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER,
                    resource_id INTEGER,
                    saved_at DATETIME,
                    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
                    rated_at DATETIME,
                    FOREIGN KEY (student_id) REFERENCES Users(user_id),
                    FOREIGN KEY (resource_id) REFERENCES Resources(resource_id)
                );
            """);

            // === Insert Example Users (students + teachers) ===
            stmt.executeUpdate("""
                INSERT INTO Users (name, email, password, role)
                VALUES 
                    ('Alice', 'alice@student.com', 'pass123', 'student'),
                    ('Bob', 'bob@student.com', 'pass456', 'student'),
                    ('Charlie', 'charlie@student.com', 'pass789', 'student'),
                    ('Diana', 'diana@student.com', 'pass000', 'student'),
                    ('Dr. Smith', 'smith@univ.com', 'teachpass', 'teacher'),
                    ('Prof. Johnson', 'johnson@univ.com', 'johnpass', 'teacher');
            """);

            // === Insert Example Resources ===
            stmt.executeUpdate("""
                INSERT INTO Resources (title, description, difficulty, category, keywords, teacher_id, is_approved, created_at)
                VALUES 
                    ('Intro to CS', 'Basic concepts in CS', 'easy', 'computer_science', 'CS, intro', 5, 1, DATETIME('now')),
                    ('Advanced Algorithms', 'Complex algorithmic strategies', 'hard', 'computer_science', 'algorithms,graphs,dp', 5, 1, DATETIME('now')),
                    ('Microeconomics Basics', 'Intro to microeconomics', 'medium', 'economic_science', 'micro,economy,demand', 6, 1, DATETIME('now')),
                    ('Macro Theory', 'Advanced macroeconomic theory', 'hard', 'economic_science', 'macro,gdp,inflation', 6, 1, DATETIME('now')),
                    ('Management 101', 'Principles of management', 'easy', 'management_science', 'planning,organizing,HR', 6, 1, DATETIME('now')),
                    ('Operations Management', 'Efficiency and processes', 'medium', 'management_science', 'process,optimization', 6, 1, DATETIME('now'));
            """);

            // === Insert RessourceInteraction (students rating resources) ===
            stmt.executeUpdate("""
                INSERT INTO RessourceInteraction (student_id, resource_id, saved_at, rating, rated_at)
                VALUES 
                    (1, 1, DATETIME('now'), 5, DATETIME('now')),
                    (1, 2, DATETIME('now'), 4, DATETIME('now')),
                    (2, 1, DATETIME('now'), 4, DATETIME('now')),
                    (2, 3, DATETIME('now'), 5, DATETIME('now')),
                    (3, 4, DATETIME('now'), 3, DATETIME('now')),
                    (3, 2, DATETIME('now'), 2, DATETIME('now')),
                    (4, 5, DATETIME('now'), 4, DATETIME('now')),
                    (4, 6, DATETIME('now'), 5, DATETIME('now')),
                    (1, 6, DATETIME('now'), 3, DATETIME('now')),
                    (2, 5, DATETIME('now'), 2, DATETIME('now'));
            """);

            System.out.println("✅ Base de données remplie avec plus d'utilisateurs, ressources et interactions !");

        } catch (SQLException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
}
