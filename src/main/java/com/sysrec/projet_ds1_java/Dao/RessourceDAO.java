package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {
    private final Connection connection;

    public RessourceDAO() {
        this.connection = DBConnection.getConnection();
    }

    public void ajouterRessource(RessourceModel ressource) {
        String sql = """
            INSERT INTO Resources (title, description, difficulty, category, keywords, teacher_id, is_approved, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
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
            System.err.println("Erreur lors de l'ajout de la ressource : " + e.getMessage());
        }
    }

    public RessourceModel getRessourceParId(int id) {
        String sql = "SELECT * FROM Resources WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRessource(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la ressource par ID : " + e.getMessage());
        }
        return null;
    }

    public List<RessourceModel> getToutesLesRessources() {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ressources.add(extractRessource(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de toutes les ressources : " + e.getMessage());
        }
        return ressources;
    }

    public List<RessourceModel> getRessourcesApprouvees() {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources WHERE is_approved = true";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ressources.add(extractRessource(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ressources approuvées : " + e.getMessage());
        }
        return ressources;
    }

    public void modifierRessource(RessourceModel ressource) {
        String sql = """
            UPDATE Resources
            SET title = ?, description = ?, difficulty = ?, category = ?, keywords = ?, is_approved = ?
            WHERE resource_id = ?
        """;
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
            System.err.println("Erreur lors de la modification de la ressource : " + e.getMessage());
        }
    }

    public void supprimerRessource(int id) {
        String sql = "DELETE FROM Resources WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la ressource : " + e.getMessage());
        }
    }

    public List<RessourceModel> getRessourcesParEnseignant(int teacherId) {
        List<RessourceModel> ressources = new ArrayList<>();
        String sql = "SELECT * FROM Resources WHERE teacher_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ressources.add(extractRessource(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ressources par enseignant : " + e.getMessage());
        }
        return ressources;
    }

    private RessourceModel extractRessource(ResultSet rs) throws SQLException {
        return new RessourceModel(
                rs.getInt("resource_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("difficulty"),
                rs.getString("category"),
                rs.getString("keywords"),
                rs.getInt("teacher_id"),
                rs.getBoolean("is_approved"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("format"),
                rs.getString("tags"),
                rs.getString("url")
        );
    }

}
