package com.sysrec.projet_ds1_java.Utils;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class DbToCsv {
    public static void main(String[] args) {
        String csvFile = "src/main/resources/database/interactions.csv";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             FileWriter writer = new FileWriter(csvFile)) {

            // Updated header to include all required fields
            writer.append("userId,itemId,rating,category,keywords\n");

            // Updated query to include category and keywords
            String query = "SELECT ri.student_id, ri.resource_id, ri.rating, r.category, r.keywords " +
                    "FROM RessourceInteraction ri " +
                    "JOIN Resources r ON ri.resource_id = r.resource_id";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int userId = rs.getInt("student_id");
                int resourceId = rs.getInt("resource_id");
                int rating = rs.getInt("rating");
                String category = rs.getString("category");
                String keywords = rs.getString("keywords");

                writer.append(userId + "," + resourceId + "," + rating + "," +
                        category + "," + keywords + "\n");
            }

            System.out.println("✅ Exportation des interactions vers CSV réussie : " + csvFile);

        } catch (SQLException | IOException e) {
            System.out.println("❌ Erreur lors de l'exportation : " + e.getMessage());
        }
    }
}