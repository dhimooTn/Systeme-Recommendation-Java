package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResourceDetailController {
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label ratingLabel;
    @FXML private Label teacherLabel;
    @FXML private Label keywordsLabel;
    @FXML private Label createdAtLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public void setResource(RessourceModel resource) {
        titleLabel.setText(resource.getTitle());
        categoryLabel.setText(resource.getCategory());
        difficultyLabel.setText(resource.getDifficulty());
        descriptionLabel.setText(resource.getDescription());
        keywordsLabel.setText(resource.getKeywords());
        createdAtLabel.setText(resource.getCreatedAt().toString());

        // Get teacher name
        String teacherName = utilisateurDAO.getUtilisateurParId(resource.getTeacherId()).getName();
        teacherLabel.setText(teacherName);

        // Calculate average rating
        double averageRating = resource.getAverageRating();
        ratingLabel.setText(String.format("%.1f / 5.0", averageRating));
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}