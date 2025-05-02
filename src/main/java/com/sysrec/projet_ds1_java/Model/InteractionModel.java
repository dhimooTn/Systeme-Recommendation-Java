package com.sysrec.projet_ds1_java.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionModel {
    private int id;
    private int etudiantId;

    public void setId(int id) {
        this.id = id;
    }

    private int ressourceId;
    private int avis; // 1 = approuvé, 0 = rejeté, 2 = enregistré

    public InteractionModel(int id, int etudiantId, int ressourceId, int avis) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.ressourceId = ressourceId;
        this.avis = avis;
    }

    public InteractionModel(int etudiantId, int ressourceId, int avis) {
        this.etudiantId = etudiantId;
        this.ressourceId = ressourceId;
        this.avis = avis;
    }

    public int getId() { return id; }
    public int getEtudiantId() { return etudiantId; }
    public int getRessourceId() { return ressourceId; }
    public int getAvis() { return avis; }

    public void setAvis(int avis) {
        this.avis = avis;
    }

    public boolean estApprouvee() {
        return this.avis == 1;
    }

    public boolean estRejetee() {
        return this.avis == 0;
    }

    public boolean estEnregistree() {
        return this.avis == 2;
    }

    public static int countInteractionsParEtudiant(Connection conn, int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int countRessourcesApprouveesParEtudiant(Connection conn, int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ? AND rating = 5";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int countRessourcesEnregistreesParEtudiant(Connection conn, int etudiantId) {
        String sql = "SELECT COUNT(*) FROM RessourceInteraction WHERE student_id = ? AND rating IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}