package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.InteractionModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InteractionDAO {

    public void ajouterInteraction(InteractionModel interaction) throws SQLException {
        String sql = "INSERT INTO RessourceInteraction (student_id, resource_id, rating) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, interaction.getStudentId());
            stmt.setInt(2, interaction.getResourceId());
            stmt.setInt(3, interaction.getRating());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    interaction.setInteractionId(rs.getInt(1));
                }
            }
        }
    }

    public List<InteractionModel> getInteractionsParEtudiant(int studentId) throws SQLException {
        List<InteractionModel> interactions = new ArrayList<>();
        String sql = "SELECT * FROM RessourceInteraction WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interactions.add(mapInteractionFromResultSet(rs));
                }
            }
        }
        return interactions;
    }

    public List<InteractionModel> getInteractionsParRessource(int resourceId) throws SQLException {
        List<InteractionModel> interactions = new ArrayList<>();
        String sql = "SELECT * FROM RessourceInteraction WHERE resource_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interactions.add(mapInteractionFromResultSet(rs));
                }
            }
        }
        return interactions;
    }

    public void modifierInteraction(InteractionModel interaction) throws SQLException {
        String sql = "UPDATE RessourceInteraction SET rating = ? WHERE interaction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, interaction.getRating());
            stmt.setInt(2, interaction.getInteractionId());
            stmt.executeUpdate();
        }
    }

    public void supprimerInteraction(int interactionId) throws SQLException {
        String sql = "DELETE FROM RessourceInteraction WHERE interaction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, interactionId);
            stmt.executeUpdate();
        }
    }

    private InteractionModel mapInteractionFromResultSet(ResultSet rs) throws SQLException {
        return new InteractionModel(
                rs.getInt("interaction_id"),
                rs.getInt("student_id"),
                rs.getInt("resource_id"),
                rs.getTimestamp("saved_at") != null ? rs.getTimestamp("saved_at").toLocalDateTime() : null,
                rs.getInt("rating"),
                rs.getTimestamp("rated_at") != null ? rs.getTimestamp("rated_at").toLocalDateTime() : null
        );
    }
}