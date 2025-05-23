package com.sysrec.projet_ds1_java.Model;

/**
 * Model class representing a user in the system.
 */
public class UtilisateurModel {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role; // 'student' or 'teacher'

    public UtilisateurModel(int userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UtilisateurModel(String name, String email, String password, String role) {
        this(0, name, email, password, role);
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    public boolean isStudent() { return "student".equalsIgnoreCase(role); }
    public boolean isTeacher() { return "teacher".equalsIgnoreCase(role); }
}