package com.sysrec.projet_ds1_java.Utils;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class DbToCsv {
    public static void main(String[] args) {
        // Utilisation de DBConnection pour obtenir la connexion à la base de données
        String csvFile = "src/main/resources/database/interactions.csv";


        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             FileWriter writer = new FileWriter(csvFile)) {

            // Création du fichier CSV avec l'en-tête
            writer.append("userId,itemId,rating\n");

            // Récupération des interactions
            String query = "SELECT student_id, resource_id, rating FROM RessourceInteraction";
            ResultSet rs = stmt.executeQuery(query);

            // Boucle sur les résultats et écriture dans le CSV
            while (rs.next()) {
                int userId = rs.getInt("student_id");
                int resourceId = rs.getInt("resource_id");
                int rating = rs.getInt("rating");

                // Ajout des données au fichier CSV
                writer.append(userId + "," + resourceId + "," + rating + "\n");
            }

            System.out.println("✅ Exportation des interactions vers CSV réussie : " + csvFile);

        } catch (SQLException | IOException e) {
            System.out.println("❌ Erreur lors de l'exportation : " + e.getMessage());
        }
    }
}
