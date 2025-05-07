package com.sysrec.projet_ds1_java.Dao;

import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {

    public int insert(RessourceModel resource) throws SQLException {
        String sql = "INSERT INTO Resources (title, description, difficulty, category, keywords, " +
                "teacher_id, is_approved, is_private) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, resource.getTitle());
            stmt.setString(2, resource.getDescription());
            stmt.setString(3, resource.getDifficulty());
            stmt.setString(4, resource.getCategory());
            stmt.setString(5, resource.getKeywords());
            stmt.setInt(6, resource.getTeacherId());
            stmt.setBoolean(7, resource.isApproved());
            stmt.setBoolean(8, resource.isPrivate());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating resource failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Creating resource failed, no ID obtained.");
            }
        }
    }

    public void updateResource(RessourceModel resource) throws SQLException {
        String sql = "UPDATE Resources SET title = ?, description = ?, difficulty = ?, " +
                "category = ?, keywords = ?, is_approved = ?, is_private = ? " +
                "WHERE resource_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resource.getTitle());
            stmt.setString(2, resource.getDescription());
            stmt.setString(3, resource.getDifficulty());
            stmt.setString(4, resource.getCategory());
            stmt.setString(5, resource.getKeywords());
            stmt.setBoolean(6, resource.isApproved());
            stmt.setBoolean(7, resource.isPrivate());
            stmt.setInt(8, resource.getResourceId());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Updating resource failed, no rows affected.");
            }
        }
    }

    public void deleteResource(int resourceId) throws SQLException {
        String sql = "DELETE FROM Resources WHERE resource_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Deleting resource failed, no rows affected.");
            }
        }
    }

    public List<RessourceModel> getResourcesByTeacherId(int teacherId) throws SQLException {
        List<RessourceModel> resources = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "COALESCE((SELECT COUNT(*) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0) as student_count, " +
                "COALESCE((SELECT AVG(rating) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0.0) as average_rating " +
                "FROM Resources r WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resources.add(mapResourceFromResultSet(rs));
                }
            }
        }
        return resources;
    }

    public int getStudentCountForTeacher(int teacherId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT ri.student_id) AS student_count " +
                "FROM RessourceInteraction ri " +
                "JOIN Resources r ON ri.resource_id = r.resource_id " +
                "WHERE r.teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("student_count") : 0;
            }
        }
    }

    public double getAverageRatingForTeacher(int teacherId) throws SQLException {
        String sql = "SELECT AVG(ri.rating) AS avg_rating " +
                "FROM RessourceInteraction ri " +
                "JOIN Resources r ON ri.resource_id = r.resource_id " +
                "WHERE r.teacher_id = ? AND ri.rating IS NOT NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble("avg_rating") : 0.0;
            }
        }
    }

    public List<RessourceModel> getRessourcesApprouvees() throws SQLException {
        List<RessourceModel> resources = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "COALESCE((SELECT COUNT(*) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0) as student_count, " +
                "COALESCE((SELECT AVG(rating) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0.0) as average_rating " +
                "FROM Resources r WHERE is_approved = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resources.add(mapResourceFromResultSet(rs));
                }
            }
        }
        return resources;
    }

    public RessourceModel getRessourceParId(int resourceId) throws SQLException {
        String sql = "SELECT r.*, " +
                "COALESCE((SELECT COUNT(*) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0) as student_count, " +
                "COALESCE((SELECT AVG(rating) FROM RessourceInteraction WHERE resource_id = r.resource_id), 0.0) as average_rating " +
                "FROM Resources r WHERE resource_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResourceFromResultSet(rs) : null;
            }
        }
    }

    private RessourceModel mapResourceFromResultSet(ResultSet rs) throws SQLException {
        return new RessourceModel(
                rs.getInt("resource_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("difficulty"),
                rs.getString("category"),
                rs.getString("keywords"),
                rs.getInt("teacher_id"),
                rs.getBoolean("is_approved"),
                rs.getBoolean("is_private"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getInt("student_count"),
                rs.getDouble("average_rating")
        );
    }
}