package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Model.RessourceModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TeacherController {

    @FXML private TableView<RessourceModel> resourceTable;
    @FXML private TextField searchField;
    @FXML private Label studentCountLabel;
    @FXML private Label resourceCountLabel;
    @FXML private Label averageRatingLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label welcomeBackLabel;

    @FXML private TableColumn<RessourceModel, String> titleCol;
    @FXML private TableColumn<RessourceModel, String> categoryCol;
    @FXML private TableColumn<RessourceModel, String> difficultyCol;
    @FXML private TableColumn<RessourceModel, Double> ratingCol;
    @FXML private TableColumn<RessourceModel, Void> actionCol;

    private ObservableList<RessourceModel> resourcesList = FXCollections.observableArrayList();
    private RessourceDAO ressourceDAO;
    private int currentTeacherId;
    private String teacherName;

    public void initialize() {
        ressourceDAO = new RessourceDAO();

        // Initialize table columns
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        ratingCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getAverageRating()).asObject());

        // Add action buttons to the action column
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailButton = new Button("View");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonsBox = new HBox(5, detailButton, deleteButton);

            {
                // Style the buttons
                detailButton.setStyle("-fx-background-color: #9c64c3; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

                detailButton.setOnAction(event -> {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    showResourceDetails(resource);
                });

                deleteButton.setOnAction(event -> {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    deleteResource(resource);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResources(newValue);
        });

        resourceTable.setItems(resourcesList);
    }

    private void filterResources(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            resourceTable.setItems(resourcesList);
        } else {
            ObservableList<RessourceModel> filteredList = FXCollections.observableArrayList();
            for (RessourceModel resource : resourcesList) {
                if (resource.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                        resource.getCategory().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(resource);
                }
            }
            resourceTable.setItems(filteredList);
        }
    }

    private void showResourceDetails(RessourceModel resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/ResourceDetailView.fxml"));
            Parent root = loader.load();

            ResourceDetailController controller = loader.getController();
            controller.setResource(resource, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Resource Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open resource details: " + e.getMessage());
        }
    }

    private void deleteResource(RessourceModel resource) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Resource");
        alert.setContentText("Are you sure you want to delete '" + resource.getTitle() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ressourceDAO.deleteResource(resource.getResourceId());
                    resourcesList.remove(resource);
                    updateDashboardStats();
                    showAlert("Success", "Resource deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete resource: " + e.getMessage());
                }
            }
        });
    }

    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadResources();
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
        welcomeLabel.setText(teacherName);
        welcomeBackLabel.setText("👋 Welcome back, " + teacherName + "!");
    }

    private void loadResources() {
        try {
            resourcesList.clear();
            List<RessourceModel> loadedResources = ressourceDAO.getResourcesByTeacherId(currentTeacherId);

            // Set default values if null
            for (RessourceModel resource : loadedResources) {
                if (resource.getAverageRating() <= 0) {
                    resource.setAverageRating(0.0);
                }
                if (resource.getStudentCount() < 0) {
                    resource.setStudentCount(0);
                }
            }

            resourcesList.addAll(loadedResources);
            updateDashboardStats();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load resources: " + e.getMessage());
        }
    }

    private void updateDashboardStats() {
        try {
            resourceCountLabel.setText(String.valueOf(resourcesList.size()));

            int studentCount = ressourceDAO.getStudentCountForTeacher(currentTeacherId);
            studentCountLabel.setText(String.valueOf(studentCount));

            double avgRating = ressourceDAO.getAverageRatingForTeacher(currentTeacherId);
            averageRatingLabel.setText(String.format("%.1f", avgRating));

        } catch (SQLException e) {
            showAlert("Error", "Failed to update statistics: " + e.getMessage());
        }
    }

    public void refreshResources() {
        loadResources();
    }

    public void addResource(RessourceModel resource) {
        try {
            int generatedId = ressourceDAO.insert(resource);
            resource.setResourceId(generatedId);
            resourcesList.add(resource);
            updateDashboardStats();
            showAlert("Success", "Resource added successfully!");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add resource: " + e.getMessage());
        }
    }

    @FXML
    public void onAddResource() {
        openAddResourceForm();
    }

    @FXML
    public void handleLogout() {
        try {
            // Get reference to current stage
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();

            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/LoginView.fxml"));
            Parent loginRoot = loader.load();

            // Create new scene and replace current scene
            Scene loginScene = new Scene(loginRoot);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();

            // Clear any sensitive data
            this.currentTeacherId = 0;
            this.teacherName = null;
            resourcesList.clear();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login view: " + e.getMessage());
        }
    }

    private void openAddResourceForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/AddResourceView.fxml"));
            Parent root = loader.load();

            AddResourceController controller = loader.getController();
            controller.setTeacherController(this);
            controller.setCurrentTeacherId(currentTeacherId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Resource");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}