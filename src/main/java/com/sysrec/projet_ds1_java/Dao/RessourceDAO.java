package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {
    private Connection connection;

    public RessourceDAO() {
        this.connection = DBConnection.getConnection();
    }

    public void ajouterRessource(RessourceModel ressource) {
        String sql = "INSERT INTO Resources (title, description, difficulty, category, keywords, teacher_id, is_approved, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ressource.getTitle());
            stmt.setString(2, ressource.getDescription());
            stmt.setString(3, ressource.getDifficulty());
            stmt.setString(4, ressource.getCategory());
            stmt.setString(5, ressource.getKeywords());
            stmt.setInt(6, ressource.getTeacherId());
            stmt.setBoolean(7, ressource.isApproved());
            stmt.setTimestamp(8, Timestamp.valueOf(ressource.getCreatedAt()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ressource.setResourceId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RessourceModel getRessourceParId(int id) {
        String sql = "SELECT * FROM Resources WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new RessourceModel(
                        rs.getInt("resource_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("difficulty"),
                        rs.getString("category"),
                        rs.getString("keywords"),
                        rs.getInt("teacher_id"),
                        rs.getBoolean("is_approved"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RessourceModel> getToutesLesRessources() {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ressources.add(new RessourceModel(
                        rs.getInt("resource_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("difficulty"),
                        rs.getString("category"),
                        rs.getString("keywords"),
                        rs.getInt("teacher_id"),
                        rs.getBoolean("is_approved"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ressources;
    }

    public List<RessourceModel> getRessourcesApprouvees() {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources WHERE is_approved = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ressources.add(new RessourceModel(
                        rs.getInt("resource_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("difficulty"),
                        rs.getString("category"),
                        rs.getString("keywords"),
                        rs.getInt("teacher_id"),
                        true,
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ressources;
    }

    public void modifierRessource(RessourceModel ressource) {
        String sql = "UPDATE Resources SET title = ?, description = ?, difficulty = ?, " +
                "category = ?, keywords = ?, is_approved = ? WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ressource.getTitle());
            stmt.setString(2, ressource.getDescription());
            stmt.setString(3, ressource.getDifficulty());
            stmt.setString(4, ressource.getCategory());
            stmt.setString(5, ressource.getKeywords());
            stmt.setBoolean(6, ressource.isApproved());
            stmt.setInt(7, ressource.getResourceId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerRessource(int id) {
        String sql = "DELETE FROM Resources WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RessourceModel> getRessourcesParEnseignant(int teacherId) {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources WHERE teacher_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ressources.add(new RessourceModel(
                        rs.getInt("resource_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("difficulty"),
                        rs.getString("category"),
                        rs.getString("keywords"),
                        rs.getInt("teacher_id"),
                        rs.getBoolean("is_approved"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ressources;
    }
}