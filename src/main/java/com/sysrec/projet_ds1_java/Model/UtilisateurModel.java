package com.sysrec.projet_ds1_java.Model;

public class UtilisateurModel {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;  // 'student' or 'teacher' as defined in Dbm.java

    // Constructor with all fields
    public UtilisateurModel(int userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Constructor without userId (for creation before DB insertion)
    public UtilisateurModel(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Validation methods
    public boolean verifierMotDePasse(String motDePasse) {
        return this.password.equals(motDePasse);
    }

    public boolean verifierEmail(String email) {
        return this.email.equalsIgnoreCase(email);
    }

    // Role check methods
    public boolean isStudent() {
        return "student".equalsIgnoreCase(this.role);
    }

    public boolean isTeacher() {
        return "teacher".equalsIgnoreCase(this.role);
    }

    // Display user info
    public void afficherInfos() {
        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}