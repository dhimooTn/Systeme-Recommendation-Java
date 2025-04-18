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

            // === Insert Example Users ===
            stmt.executeUpdate("""
                INSERT INTO Users (name, email, password, role)
                VALUES 
                    ('Alice', 'alice@student.com', 'pass123', 'student'),
                    ('Bob', 'bob@student.com', 'pass456', 'student'),
                    ('Dr. Smith', 'smith@univ.com', 'teachpass', 'teacher');
            """);

            // === Insert Example Resources ===
            stmt.executeUpdate("""
                INSERT INTO Resources (title, description, difficulty, category, keywords, teacher_id, is_approved, created_at)
                VALUES 
                    ('Intro to CS', 'Basic concepts in CS', 'easy', 'computer_science', 'basics,CS,intro', 3, 1, DATETIME('now')),
                    ('Economics 101', 'Microeconomics notes', 'medium', 'economic_science', 'supply,demand,macro', 3, 1, DATETIME('now'));
            """);

            System.out.println("✅ RessourceInteraction created and Resources updated (type column removed, category fixed)!");

        } catch (SQLException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
}
