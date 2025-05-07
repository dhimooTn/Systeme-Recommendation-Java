package com.sysrec.projet_ds1_java.Model;

import com.sysrec.projet_ds1_java.Dao.InteractionDAO;
import com.sysrec.projet_ds1_java.Dao.RessourceDAO;

import java.sql.SQLException;
import java.util.List;

public class TeacherModel extends UtilisateurModel {
    private final RessourceDAO ressourceDAO;
    private final InteractionDAO interactionDAO;

    public TeacherModel(int id, String name, String email, String password,
                        RessourceDAO ressourceDAO, InteractionDAO interactionDAO) {
        super(id, name, email, password, "teacher");
        this.ressourceDAO = ressourceDAO;
        this.interactionDAO = interactionDAO;
    }

    public int countStudentsByResource(int resourceId) throws SQLException {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParRessource(resourceId);
        return (int) interactions.stream()
                .map(InteractionModel::getStudentId)
                .distinct()
                .count();
    }

    public double getAverageRatingByResource(int resourceId) throws SQLException {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParRessource(resourceId);
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