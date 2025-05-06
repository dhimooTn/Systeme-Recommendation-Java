package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.InteractionModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InteractionDAO {
    private Connection connection;

    public InteractionDAO() {
        this.connection = DBConnection.getConnection();
    }

    public void ajouterInteraction(InteractionModel interaction) {
        String sql = "INSERT INTO RessourceInteraction (student_id, resource_id, rating) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, interaction.getEtudiantId());
            stmt.setInt(2, interaction.getRessourceId());
            stmt.setInt(3, interaction.getAvis());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    interaction.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public InteractionModel getInteractionParId(int id) {
        String sql = "SELECT * FROM RessourceInteraction WHERE interaction_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new InteractionModel(
                        rs.getInt("interaction_id"),
                        rs.getInt("student_id"),
                        rs.getInt("resource_id"),
                        rs.getInt("rating") // Using rating column for avis
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InteractionModel> getInteractionsParEtudiant(int etudiantId) {
        List<InteractionModel> interactions = new ArrayList<>();
        String sql = "SELECT * FROM RessourceInteraction WHERE student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                interactions.add(new InteractionModel(
                        rs.getInt("interaction_id"),
                        rs.getInt("student_id"),
                        rs.getInt("resource_id"),
                        rs.getInt("rating") // Using rating column for avis
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return interactions;
    }

    public List<InteractionModel> getInteractionsParRessource(int ressourceId) {
        List<InteractionModel> interactions = new ArrayList<>();
        String sql = "SELECT * FROM RessourceInteraction WHERE resource_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ressourceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                interactions.add(new InteractionModel(
                        rs.getInt("interaction_id"),
                        rs.getInt("student_id"),
                        rs.getInt("resource_id"),
                        rs.getInt("rating") // Using rating column for avis
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return interactions;
    }

    public void modifierInteraction(InteractionModel interaction) {
        String sql = "UPDATE RessourceInteraction SET rating = ? WHERE interaction_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, interaction.getAvis());
            stmt.setInt(2, interaction.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerInteraction(int id) {
        String sql = "DELETE FROM RessourceInteraction WHERE interaction_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Additional methods to support InteractionModel's static methods
    public int countInteractionsParEtudiant(int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countRessourcesApprouveesParEtudiant(int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ? AND rating = 5";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countRessourcesEnregistreesParEtudiant(int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ? AND rating IS NOT NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}