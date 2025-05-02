package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Dao.InteractionDAO;
import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import com.sysrec.projet_ds1_java.Model.InteractionModel;
import com.sysrec.projet_ds1_java.Model.RessourceModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.util.List;

public class TeacherController {
    @FXML private Label studentCountLabel;
    @FXML private Label resourceCountLabel;
    @FXML private Label averageRatingLabel;
    @FXML private TextField searchField;
    @FXML private TableView<RessourceModel> resourceTable;
    @FXML private TableColumn<RessourceModel, String> titleCol;
    @FXML private TableColumn<RessourceModel, String> categoryCol;
    @FXML private TableColumn<RessourceModel, String> difficultyCol;
    @FXML private TableColumn<RessourceModel, Double> ratingCol;
    @FXML private TableColumn<RessourceModel, Void> actionCol;

    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private final InteractionDAO interactionDAO = new InteractionDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private ObservableList<RessourceModel> resources = FXCollections.observableArrayList();
    private int currentTeacherId;

    public void setCurrentTeacher(int teacherId) {
        this.currentTeacherId = teacherId;
        loadResources();
        updateStats();
    }

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        categoryCol.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        difficultyCol.setCellValueFactory(cellData -> cellData.getValue().difficultyProperty());
        ratingCol.setCellValueFactory(cellData -> cellData.getValue().averageRatingProperty().asObject());

        resourceTable.setItems(resources);
        addActionButtonsToTable();
    }

    private void loadResources() {
        resources.clear();
        List<RessourceModel> teacherResources = ressourceDAO.getRessourcesParEnseignant(currentTeacherId);
        resources.addAll(teacherResources);

        // Calculate average ratings for each resource
        resources.forEach(resource -> {
            List<InteractionModel> interactions = interactionDAO.getInteractionsParRessource(resource.getResourceId());
            double average = interactions.stream()
                    .mapToInt(InteractionModel::getAvis)
                    .average()
                    .orElse(0.0);
            resource.setAverageRating(average);
        });
    }

    private void updateStats() {
        int totalResources = resources.size();
        double totalRating = resources.stream()
                .mapToDouble(RessourceModel::getAverageRating)
                .sum();
        double averageRating = totalResources > 0 ? totalRating / totalResources : 0.0;

        int totalStudents = utilisateurDAO.countStudents();

        studentCountLabel.setText(String.valueOf(totalStudents));
        resourceCountLabel.setText(String.valueOf(totalResources));
        averageRatingLabel.setText(String.format("%.1f", averageRating));
    }

    @FXML
    public void onAddResource() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/AddResourceView.fxml"));
            Scene scene = new Scene(loader.load());

            AddResourceController controller = loader.getController();
            controller.setTeacherController(this);
            controller.setCurrentTeacherId(currentTeacherId);

            Stage stage = new Stage();
            stage.setTitle("Add Resource");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open add resource window");
        }
    }

    @FXML
    public void handleLogout() {
        Stage stage = (Stage) studentCountLabel.getScene().getWindow();
        stage.close();
    }

    public void addResource(RessourceModel resource) {
        ressourceDAO.ajouterRessource(resource);
        loadResources();
        updateStats();
    }

    private void addActionButtonsToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View Details");

            {
                btn.setOnAction(event -> {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    showResourceDetails(resource);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void showResourceDetails(RessourceModel resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/ResourceDetailView.fxml"));
            Scene scene = new Scene(loader.load());

            ResourceDetailController controller = loader.getController();
            controller.setResource(resource);

            Stage stage = new Stage();
            stage.setTitle("Resource Details");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open resource details: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadResources();
            return;
        }

        List<RessourceModel> filtered = resources.stream()
                .filter(resource ->
                        resource.getTitle().toLowerCase().contains(query) ||
                                resource.getCategory().toLowerCase().contains(query) ||
                                resource.getDifficulty().toLowerCase().contains(query) ||
                                resource.getKeywords().toLowerCase().contains(query))
                .toList();

        resources.setAll(filtered);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}