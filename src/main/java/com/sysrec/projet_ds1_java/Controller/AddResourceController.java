package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Model.RessourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class AddResourceController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField keywordsField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private TeacherController teacherController;
    private int currentTeacherId;

    @FXML
    public void initialize() {
        // Set difficulty levels
        difficultyCombo.getItems().addAll("Beginner", "Intermediate", "Advanced");
        difficultyCombo.getSelectionModel().selectFirst();

        // Set categories
        categoryCombo.getItems().addAll(
                "Computer Science",
                "Economic Science",
                "Management Science",
                "Mathematics",
                "Physics"
        );
        categoryCombo.getSelectionModel().selectFirst();

        // Initialize button actions (added missing initialization)
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    // Added missing @FXML annotations for handler methods
    @FXML
    private void handleSave() {
        // Validate fields
        if (titleField.getText().isEmpty()) {
            showError("Title is required");
            return;
        }

        if (descriptionArea.getText().isEmpty()) {
            showError("Description is required");
            return;
        }

        if (keywordsField.getText().isEmpty()) {
            showError("Keywords are required");
            return;
        }

        // Create new resource
        RessourceModel newResource = new RessourceModel(
                0, // Temporary ID
                titleField.getText(),
                descriptionArea.getText(),
                difficultyCombo.getValue(),
                categoryCombo.getValue(),
                keywordsField.getText(),
                this.currentTeacherId,
                true, // Approved by default
                LocalDateTime.now()
        );

        // Add resource via parent controller
        if (teacherController != null) {
            teacherController.addResource(newResource);
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // Setters for dependency injection
    public void setTeacherController(TeacherController teacherController) {
        this.teacherController = teacherController;
    }

    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
    }

    // Getters (optional)
    public TextField getTitleField() { return titleField; }
    public TeacherController getTeacherController() { return teacherController; }
    public Button getSaveButton() { return saveButton; }
    public TextField getKeywordsField() { return keywordsField; }
    public Label getErrorLabel() { return errorLabel; }
    public ComboBox<String> getDifficultyCombo() { return difficultyCombo; }
    public TextArea getDescriptionArea() { return descriptionArea; }
    public int getCurrentTeacherId() { return currentTeacherId; }
    public ComboBox<String> getCategoryCombo() { return categoryCombo; }
    public Button getCancelButton() { return cancelButton; }
}