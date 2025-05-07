package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Model.RessourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ResourceDetailController {

    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private TextField difficultyField;
    @FXML private TextArea descriptionField;
    @FXML private Label ratingLabel;
    @FXML private Label studentCountLabel;
    @FXML private ComboBox<String> statusComboBox;

    private RessourceModel resource;
    private TeacherController teacherController;

    public void setResource(RessourceModel resource, TeacherController controller) {
        this.resource = resource;
        this.teacherController = controller;

        titleField.setText(resource.getTitle());
        categoryField.setText(resource.getCategory());
        difficultyField.setText(resource.getDifficulty());
        descriptionField.setText(resource.getDescription());
        ratingLabel.setText(String.format("%.1f", resource.getAverageRating()));
        studentCountLabel.setText(String.valueOf(resource.getStudentCount()));

        statusComboBox.getItems().addAll("Private", "Public");
        statusComboBox.setValue(resource.isPrivate() ? "Private" : "Public");
    }

    @FXML
    public void handleDelete() {
        if (resource != null) {
            try {
                RessourceDAO dao = new RessourceDAO();
                dao.deleteResource(resource.getResourceId());

                if (teacherController != null) {
                    teacherController.refreshResources();
                }
                closeWindow();
            } catch (SQLException e) {
                showErrorAlert("Error deleting resource", e.getMessage());
            }
        }
    }

    @FXML
    public void handleModify() {
        if (resource != null) {
            try {
                resource.setTitle(titleField.getText());
                resource.setCategory(categoryField.getText());
                resource.setDifficulty(difficultyField.getText());
                resource.setDescription(descriptionField.getText());

                String selectedStatus = statusComboBox.getValue();
                resource.setPrivate("Private".equals(selectedStatus));

                RessourceDAO dao = new RessourceDAO();
                dao.updateResource(resource);

                if (teacherController != null) {
                    teacherController.refreshResources();
                }
                closeWindow();
            } catch (SQLException e) {
                showErrorAlert("Error updating resource", e.getMessage());
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}