package com.sysrec.projet_ds1_java.Model;

import com.sysrec.projet_ds1_java.Dao.*;
import java.util.List;

public class TeacherModel extends UtilisateurModel {
    private UtilisateurDAO utilisateurDAO;
    private RessourceDAO ressourceDAO;
    private InteractionDAO interactionDAO;

    public TeacherModel(int id, String name, String email, String password, String role, UtilisateurDAO utilisateurDAO, RessourceDAO ressourceDAO, InteractionDAO interactionDAO) {
        super(id, name, email, password, role);
        this.utilisateurDAO = utilisateurDAO;
        this.ressourceDAO = ressourceDAO;
        this.interactionDAO = interactionDAO;
    }

    public int totalEtudiantsParRessource(int ressourceId) {
        return (int) interactionDAO.getInteractionsParRessource(ressourceId).stream()
                .filter(i -> i.getRessourceId() == ressourceId)
                .map(i -> i.getEtudiantId())
                .distinct().count();
    }

    public int totalRessources() {
        return ressourceDAO.getToutesLesRessources().size();
    }

    public double moyenneAvisParRessource(int ressourceId) {
        List<InteractionModel> list = interactionDAO.getInteractionsParRessource(ressourceId);
        int total = 0, count = 0;
        for (var i : list) {
            if (i.getRessourceId() == ressourceId) {
                total += i.getAvis();
                count++;
            }
        }
        return count == 0 ? 0 : (double) total / count;
    }
}
//total ressources par enseignat
// get nombre total detudient par enseignat