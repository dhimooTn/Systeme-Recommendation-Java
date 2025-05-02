package com.sysrec.projet_ds1_java.Model;

import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import com.sysrec.projet_ds1_java.Dao.InteractionDAO;
import com.sysrec.projet_ds1_java.Dao.RessourceDAO;

import java.util.ArrayList;
import java.util.List;

public class StudentModel extends UtilisateurModel {
    private final UtilisateurDAO utilisateurDAO;
    private final InteractionDAO interactionDAO;
    private final RessourceDAO ressourceDAO;

    // Constructeur
    public StudentModel(int id, String name, String email, String password, String role, UtilisateurDAO utilisateurDAO, InteractionDAO interactionDAO, RessourceDAO ressourceDAO) {
        super(id, name, email, password, role);
        this.utilisateurDAO = utilisateurDAO;
        this.interactionDAO = interactionDAO;
        this.ressourceDAO = ressourceDAO;
    }

    // Méthode pour afficher les informations d'un étudiant
    public UtilisateurModel afficherInfosEtudiant() {
        return utilisateurDAO.getUtilisateurParId(this.getUserId());
    }

    // Méthode pour récupérer toutes les ressources auxquelles un étudiant a interagi
    public List<RessourceModel> getRessources() {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(this.getUserId());
        List<RessourceModel> ressources = new ArrayList<>();

        for (InteractionModel interaction : interactions) {
            RessourceModel ressource = ressourceDAO.getRessourceParId(interaction.getRessourceId());
            if (ressource != null) {
                ressources.add(ressource);
            }
        }
        return ressources;
    }

    // Méthode pour noter une ressource
    public void noterRessource(int ressourceId, int avis) {
        InteractionModel interaction = new InteractionModel(this.getUserId(), ressourceId, avis);
        interactionDAO.ajouterInteraction(interaction);
    }

    // Méthode pour afficher les avis d'un étudiant sur toutes ses interactions
    public void afficherAvis() {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(this.getUserId());
        for (InteractionModel interaction : interactions) {
            RessourceModel ressource = ressourceDAO.getRessourceParId(interaction.getRessourceId());
            if (ressource != null) {
                System.out.println("Ressource: " + ressource.getTitle() + " - Avis: " + interaction.getAvis());
            }
        }
    }

    // Méthode pour récupérer la moyenne des avis de cet étudiant sur ses ressources
    public double moyenneAvis() {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(this.getUserId());
        int total = 0, count = 0;

        for (InteractionModel interaction : interactions) {
            total += interaction.getAvis();
            count++;
        }

        return count == 0 ? 0 : (double) total / count;
    }

    // Méthode pour rechercher des ressources par mot-clé
    public List<RessourceModel> rechercherRessourcesParMotCle(String motCle) {
        List<RessourceModel> ressources = ressourceDAO.getToutesLesRessources();
        List<RessourceModel> resultats = new ArrayList<>();

        for (RessourceModel ressource : ressources) {
            if (ressource.getTitle().contains(motCle) || ressource.getDescription().contains(motCle)) {
                resultats.add(ressource);
            }
        }

        return resultats;
    }

    // Méthode pour rechercher des ressources par catégorie
    public List<RessourceModel> rechercherRessourcesParCategorie(String categorie) {
        List<RessourceModel> ressources = ressourceDAO.getToutesLesRessources();
        List<RessourceModel> resultats = new ArrayList<>();

        for (RessourceModel ressource : ressources) {
            if (ressource.getCategory().equalsIgnoreCase(categorie)) {
                resultats.add(ressource);
            }
        }

        return resultats;
    }

    // Méthode pour afficher les ressources disponibles dans une catégorie spécifique
    public void afficherRessourcesParCategorie(String categorie) {
        List<RessourceModel> ressources = rechercherRessourcesParCategorie(categorie);
        if (ressources.isEmpty()) {
            System.out.println("Aucune ressource trouvée pour cette catégorie.");
        } else {
            for (RessourceModel ressource : ressources) {
                ressource.afficheRessource();
            }
        }
    }
}
