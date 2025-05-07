package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.UtilisateurModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public void ajouterUtilisateur(UtilisateurModel utilisateur) throws SQLException {
        String sql = "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utilisateur.getName());
            stmt.setString(2, utilisateur.getEmail());
            stmt.setString(3, utilisateur.getPassword());
            stmt.setString(4, utilisateur.getRole());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    utilisateur.setUserId(rs.getInt(1));
                }
            }
        }
    }

    public UtilisateurModel getUtilisateurParId(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UtilisateurModel(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    public List<UtilisateurModel> getUtilisateursParRole(String role) throws SQLException {
        List<UtilisateurModel> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utilisateurs.add(new UtilisateurModel(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role")
                    ));
                }
            }
        }
        return utilisateurs;
    }

    public UtilisateurModel connexion(String email, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UtilisateurModel(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    public boolean emailExisteDeja(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}