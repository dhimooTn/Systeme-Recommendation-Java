package com.sysrec.projet_ds1_java.Model;

import com.sysrec.projet_ds1_java.Dao.InteractionDAO;
import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;

import java.sql.SQLException;
import java.util.List;

public class StudentModel extends UtilisateurModel {
    private final InteractionDAO interactionDAO;
    private final RessourceDAO ressourceDAO;

    public StudentModel(int id, String name, String email, String password,
                        InteractionDAO interactionDAO, RessourceDAO ressourceDAO) {
        super(id, name, email, password, "student");
        this.interactionDAO = interactionDAO;
        this.ressourceDAO = ressourceDAO;
    }

    public void noterRessource(int resourceId, int rating) throws SQLException {
        InteractionModel interaction = new InteractionModel(getUserId(), resourceId, rating);
        interaction.setRatedAt(java.time.LocalDateTime.now());
        interactionDAO.ajouterInteraction(interaction);
    }

    public double getAverageRating() throws SQLException {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(getUserId());
        if (interactions.isEmpty()) return 0;

        int sum = 0;
        int count = 0;
        for (InteractionModel interaction : interactions) {
            if (interaction.getRating() >= 1 && interaction.getRating() <= 5) {
                sum += interaction.getRating();
                count++;
            }
        }
        return count > 0 ? (double) sum / count : 0;
    }
}