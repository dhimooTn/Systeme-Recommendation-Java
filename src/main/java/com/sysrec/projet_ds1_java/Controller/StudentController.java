package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.InteractionDAO;
import com.sysrec.projet_ds1_java.Dao.RessourceDAO;
import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import com.sysrec.projet_ds1_java.Model.InteractionModel;
import com.sysrec.projet_ds1_java.Model.RessourceModel;
import com.sysrec.projet_ds1_java.Model.StudentModel;
import com.sysrec.projet_ds1_java.Model.UtilisateurModel;
import com.sysrec.projet_ds1_java.Service.RecommandationService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StudentController {

    @FXML private TableView<RessourceModel> savedResourcesTable;
    @FXML private Label savedLabel;
    @FXML private Label completedLabel;
    @FXML private TextField searchField;
    @FXML private VBox resultsContainer;
    @FXML private Label welcomeLabel;
    @FXML private ListView<RessourceModel> recommendationsListView;
    @FXML private Label recommendationTitle;
    @FXML private Label errorLabel;

    // Filters
    private boolean filterByTitle = true;
    private boolean filterByCategory = true;
    private boolean filterByTeacher = true;

    private int currentStudentId;
    private StudentModel currentStudent;
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private final InteractionDAO interactionDAO = new InteractionDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final RecommandationService recommandationService = new RecommandationService();

    private final ObservableList<RessourceModel> savedResources = FXCollections.observableArrayList();
    private final ObservableList<RessourceModel> recommendedResources = FXCollections.observableArrayList();

    public void setCurrentStudent(int studentId) {
        this.currentStudentId = studentId;
        UtilisateurModel user = utilisateurDAO.getUtilisateurParId(studentId);
        if (user != null) {
            this.currentStudent = new StudentModel(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole(),
                    utilisateurDAO,
                    interactionDAO,
                    ressourceDAO
            );
            welcomeLabel.setText("👋 Welcome back, " + user.getName() + "!");
            loadPersonalizedData();
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        resultsContainer.getChildren().clear();

        if (recommendationTitle != null) {
            recommendationTitle.setText("Recommended For You");
        }

        recommendationsListView.setCellFactory(param -> new ListCell<RessourceModel>() {
            @Override
            protected void updateItem(RessourceModel resource, boolean empty) {
                super.updateItem(resource, empty);
                if (empty || resource == null) {
                    setGraphic(null);
                } else {
                    VBox card = createResourceCard(resource);
                    setGraphic(card);
                }
            }
        });
    }

    private VBox createResourceCard(RessourceModel resource) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8;");

        Label titleLabel = new Label(resource.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6a3093;");

        UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
        String teacherName = teacher != null ? teacher.getName() : "Unknown";
        Label detailsLabel = new Label(teacherName + " • " + resource.getCategory());
        detailsLabel.setStyle("-fx-text-fill: #9c64c3;");

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white;");
        viewButton.setOnAction(e -> viewDetails(resource));

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #d9c2f0; -fx-text-fill: #4a235a;");
        saveButton.setOnAction(e -> saveResource(resource));

        buttonBox.getChildren().addAll(viewButton, saveButton);
        card.getChildren().addAll(titleLabel, detailsLabel, buttonBox);
        return card;
    }

    private void loadPersonalizedData() {
        loadSavedResources();
        loadRecommendedResources();
        updateCounters();
    }

    private void setupTableColumns() {
        // Status column
        TableColumn<RessourceModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            RessourceModel resource = cellData.getValue();
            return new SimpleStringProperty(resource.isApproved() ? "✓ Completed" : "● Saved");
        });
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("✓ Completed")) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6a3093;");
                    }
                }
            }
        });
        statusCol.setPrefWidth(100);

        TableColumn<RessourceModel, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<RessourceModel, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(cellData -> {
            int teacherId = cellData.getValue().getTeacherId();
            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(teacherId);
            return new SimpleStringProperty(teacher != null ? teacher.getName() : "Unknown");
        });
        teacherCol.setPrefWidth(150);

        TableColumn<RessourceModel, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<RessourceModel, String> difficultyCol = new TableColumn<>("Difficulty");
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        difficultyCol.setPrefWidth(100);

        TableColumn<RessourceModel, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button doneButton = new Button("✓ Done");
            private final Button deleteButton = new Button("✗ Delete");
            private final Button rateButton = new Button("★ Rate");
            private final HBox actionButtons = new HBox(5, doneButton, deleteButton, rateButton);

            {
                doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                rateButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");

                doneButton.setOnAction(e -> markResourceAsCompleted(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> removeSavedResource(getTableView().getItems().get(getIndex())));
                rateButton.setOnAction(e -> showRatingWindow(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionButtons);
            }
        });
        actionsCol.setPrefWidth(200);

        savedResourcesTable.getColumns().setAll(statusCol, titleCol, teacherCol, categoryCol, difficultyCol, actionsCol);
        savedResourcesTable.setItems(savedResources);
    }

    private void loadSavedResources() {
        if (currentStudentId == 0) return;

        savedResources.clear();
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(currentStudentId);

        for (InteractionModel interaction : interactions) {
            if (interaction.estEnregistree() || interaction.estApprouvee()) {
                RessourceModel resource = ressourceDAO.getRessourceParId(interaction.getRessourceId());
                if (resource != null) {
                    // Mark completed resources
                    if (interaction.estApprouvee()) {
                        resource.setApproved(true);
                    }
                    savedResources.add(resource);
                }
            }
        }

        // Sort by completion status (completed first) then by title
        savedResources.sort((r1, r2) -> {
            boolean r1Completed = r1.isApproved();
            boolean r2Completed = r2.isApproved();
            if (r1Completed == r2Completed) {
                return r1.getTitle().compareTo(r2.getTitle());
            }
            return r1Completed ? -1 : 1;
        });
    }

    private void loadRecommendedResources() {
        if (currentStudentId == 0) return;

        recommendedResources.clear();

        try {
            // Get recommendations from both approaches
            List<RessourceModel> userBasedRecs = recommandationService.getUserBasedRecommendations(currentStudentId);
            List<RessourceModel> itemBasedRecs = recommandationService.getItemBasedRecommendations(currentStudentId);

            // Filter out already saved resources
            List<Integer> savedResourceIds = savedResources.stream()
                    .map(RessourceModel::getResourceId)
                    .collect(Collectors.toList());

            userBasedRecs = userBasedRecs.stream()
                    .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                    .collect(Collectors.toList());

            itemBasedRecs = itemBasedRecs.stream()
                    .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                    .collect(Collectors.toList());

            // Combine with weighted scores
            Map<RessourceModel, Double> combinedScores = new HashMap<>();
            addToRecommendations(combinedScores, userBasedRecs, 0.7); // Higher weight for user-based
            addToRecommendations(combinedScores, itemBasedRecs, 0.3);

            // Get top recommendations
            List<RessourceModel> topRecommendations = combinedScores.entrySet().stream()
                    .sorted(Map.Entry.<RessourceModel, Double>comparingByValue().reversed())
                    .limit(5) // Show top 5 recommendations
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            recommendedResources.addAll(topRecommendations);
            recommendationsListView.setItems(recommendedResources);

        } catch (Exception e) {
            showAlert("Recommendation Error", "Could not load personalized recommendations: " + e.getMessage());
            // Fallback: show popular resources
            loadPopularResourcesAsFallback();
        }
    }

    private void loadPopularResourcesAsFallback() {
        List<RessourceModel> allResources = ressourceDAO.getToutesLesRessources();
        List<RessourceModel> popularResources = allResources.stream()
                .sorted((r1, r2) -> Double.compare(
                        getAverageRating(r2.getResourceId()),
                        getAverageRating(r1.getResourceId())))
                .limit(5)
                .collect(Collectors.toList());

        recommendedResources.addAll(popularResources);
        recommendationsListView.setItems(recommendedResources);

        recommendationTitle.setText("Popular Resources");
    }

    private void addToRecommendations(Map<RessourceModel, Double> combined, List<RessourceModel> recs, double weight) {
        for (int i = 0; i < recs.size(); i++) {
            RessourceModel res = recs.get(i);
            double score = (recs.size() - i) * weight;
            combined.merge(res, score, Double::sum);
        }
    }

    private double getAverageRating(int resourceId) {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParRessource(resourceId);
        if (interactions.isEmpty()) return 0;

        double sum = interactions.stream()
                .mapToInt(InteractionModel::getAvis)
                .sum();
        return sum / interactions.size();
    }

    private void markResourceAsCompleted(RessourceModel resource) {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
        if (interaction != null) {
            interaction.setAvis(1);
            interactionDAO.modifierInteraction(interaction);
            resource.setApproved(true);
            savedResourcesTable.refresh();
            showAlert("Success", "Resource marked as completed!");
            updateCounters();
        }
    }

    private void removeSavedResource(RessourceModel resource) {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
        if (interaction != null) {
            interactionDAO.supprimerInteraction(interaction.getId());
            savedResources.remove(resource);
            showAlert("Success", "Resource removed from your list!");
            updateCounters();
        }
    }

    private InteractionModel findInteractionForResource(int resourceId) {
        if (currentStudentId == 0) return null;

        return interactionDAO.getInteractionsParEtudiant(currentStudentId).stream()
                .filter(i -> i.getRessourceId() == resourceId)
                .findFirst()
                .orElse(null);
    }

    private void saveResource(RessourceModel resource) {
        if (currentStudentId == 0) return;

        InteractionModel existing = findInteractionForResource(resource.getResourceId());
        if (existing != null) {
            showAlert("Info", "You've already saved this resource!");
            return;
        }

        InteractionModel newInteraction = new InteractionModel(
                currentStudentId,
                resource.getResourceId(),
                2
        );
        interactionDAO.ajouterInteraction(newInteraction);
        savedResources.add(resource);
        showAlert("Success", "Resource saved to your list!");
        updateCounters();
    }

    private void updateCounters() {
        if (currentStudentId == 0) return;

        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(currentStudentId);

        long savedCount = interactions.stream()
                .filter(InteractionModel::estEnregistree)
                .count();

        long completedCount = interactions.stream()
                .filter(InteractionModel::estApprouvee)
                .count();

        savedLabel.setText(String.valueOf(savedCount));
        completedLabel.setText(String.valueOf(completedCount));
    }

    private void showRatingWindow(RessourceModel resource) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Rate: " + resource.getTitle());

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20;");

        Label prompt = new Label("How would you rate this resource?");
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 3);
        ratingSpinner.setEditable(true);

        Button submitButton = new Button("Submit Rating");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            int rating = ratingSpinner.getValue();
            saveRating(resource, rating);
            stage.close();
        });

        root.getChildren().addAll(prompt, ratingSpinner, submitButton);
        stage.setScene(new Scene(root, 300, 150));
        stage.show();
    }

    private void saveRating(RessourceModel resource, int rating) {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());

        if (interaction != null) {
            interaction.setAvis(rating);
            interactionDAO.modifierInteraction(interaction);
        } else {
            InteractionModel newInteraction = new InteractionModel(
                    currentStudentId,
                    resource.getResourceId(),
                    rating
            );
            interactionDAO.ajouterInteraction(newInteraction);
        }

        showAlert("Thank You", "Your rating has been saved!");
        loadRecommendedResources();
    }

    private void viewDetails(RessourceModel resource) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resource.getTitle());

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
        String teacherName = teacher != null ? teacher.getName() : "Unknown";

        InteractionModel studentInteraction = findInteractionForResource(resource.getResourceId());
        String status = "Not saved";
        if (studentInteraction != null) {
            status = studentInteraction.estApprouvee() ? "Completed" :
                    studentInteraction.estEnregistree() ? "Saved" : "Rated";
        }

        Label titleLabel = new Label(resource.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label teacherLabel = new Label("By: " + teacherName);
        Label categoryLabel = new Label("Category: " + resource.getCategory());
        Label difficultyLabel = new Label("Difficulty: " + resource.getDifficulty());
        Label statusLabel = new Label("Your status: " + status);
        Label ratingLabel = new Label("Rating: ★ " + String.format("%.1f", getAverageRating(resource.getResourceId())));

        TextArea descriptionArea = new TextArea(resource.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(150);

        HBox buttonBox = new HBox(10);
        Button saveButton = new Button("Save Resource");
        Button rateButton = new Button("Rate Resource");
        Button closeButton = new Button("Close");

        saveButton.setOnAction(e -> {
            saveResource(resource);
            stage.close();
        });
        rateButton.setOnAction(e -> {
            stage.close();
            showRatingWindow(resource);
        });
        closeButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(saveButton, rateButton, closeButton);
        root.getChildren().addAll(titleLabel, teacherLabel, categoryLabel,
                difficultyLabel, statusLabel, ratingLabel, new Label("Description:"),
                descriptionArea, buttonBox);

        stage.setScene(new Scene(root, 500, 400));
        stage.show();
    }

    @FXML
    private void filterResources() {
        String query = searchField.getText().trim().toLowerCase();
        resultsContainer.getChildren().clear();

        if (query.isEmpty()) return;

        List<RessourceModel> filtered = ressourceDAO.getToutesLesRessources().stream()
                .filter(resource -> matchesSearch(resource, query))
                .collect(Collectors.toList());

        showSearchResults(filtered);
    }

    private boolean matchesSearch(RessourceModel resource, String query) {
        boolean matches = false;

        if (filterByTitle && resource.getTitle().toLowerCase().contains(query)) {
            matches = true;
        }

        if (!matches && filterByCategory && resource.getCategory().toLowerCase().contains(query)) {
            matches = true;
        }

        if (!matches && filterByTeacher) {
            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
            if (teacher != null && teacher.getName().toLowerCase().contains(query)) {
                matches = true;
            }
        }

        return matches;
    }

    private void showSearchResults(List<RessourceModel> results) {
        resultsContainer.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("No resources found matching your search.");
            noResults.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            resultsContainer.getChildren().add(noResults);
            return;
        }

        for (RessourceModel resource : results) {
            VBox card = new VBox(8);
            card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");

            Label titleLabel = new Label(resource.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold;");

            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
            String teacherName = teacher != null ? teacher.getName() : "Unknown";

            Label detailsLabel = new Label(String.format("%s • %s • %s",
                    teacherName,
                    resource.getCategory(),
                    resource.getDifficulty()));

            HBox buttonBox = new HBox(10);
            Button viewButton = new Button("View");
            Button saveButton = new Button("Save");

            viewButton.setOnAction(e -> viewDetails(resource));
            saveButton.setOnAction(e -> saveResource(resource));

            buttonBox.getChildren().addAll(viewButton, saveButton);
            card.getChildren().addAll(titleLabel, detailsLabel, buttonBox);
            resultsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/LoginView.fxml"));
            Parent newRoot = loader.load();

            Scene currentScene = savedResourcesTable.getScene();

            if (!(currentScene.getRoot() instanceof StackPane)) {
                StackPane container = new StackPane();
                container.getChildren().add(currentScene.getRoot());
                currentScene.setRoot(container);
            }

            StackPane mainContainer = (StackPane) currentScene.getRoot();
            Parent currentRoot = (Parent) mainContainer.getChildren().get(0);

            newRoot.setOpacity(0);
            newRoot.setScaleX(0.95);
            newRoot.setScaleY(0.95);
            mainContainer.getChildren().add(newRoot);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), currentRoot);
            scaleDown.setFromX(1.0);
            scaleDown.setFromY(1.0);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), newRoot);
            scaleUp.setFromX(0.95);
            scaleUp.setFromY(0.95);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            fadeOut.setOnFinished(e -> {
                mainContainer.getChildren().remove(currentRoot);
                Stage stage = (Stage) currentScene.getWindow();
                stage.setTitle("Login");
                StackPane.setAlignment(newRoot, Pos.CENTER);
            });

            new ParallelTransition(fadeOut, scaleDown, fadeIn, scaleUp).play();

        } catch (IOException e) {
            showError("Failed to load login view: " + e.getMessage());
            e.printStackTrace();
            showLoginViewDirectly();
        } catch (Exception e) {
            showError("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginViewDirectly() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) savedResourcesTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            System.err.println("Critical error: Could not load login view");
            e.printStackTrace();
        }
    }

    @FXML
    private void openFilters() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Search Filters");

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20;");

        CheckBox titleCheck = new CheckBox("Search in titles");
        CheckBox categoryCheck = new CheckBox("Search in categories");
        CheckBox teacherCheck = new CheckBox("Search in teacher names");

        titleCheck.setSelected(filterByTitle);
        categoryCheck.setSelected(filterByCategory);
        teacherCheck.setSelected(filterByTeacher);

        Button applyButton = new Button("Apply Filters");
        applyButton.setOnAction(e -> {
            filterByTitle = titleCheck.isSelected();
            filterByCategory = categoryCheck.isSelected();
            filterByTeacher = teacherCheck.isSelected();
            stage.close();
            filterResources();
        });

        root.getChildren().addAll(
                new Label("Select search filters:"),
                titleCheck,
                categoryCheck,
                teacherCheck,
                applyButton
        );

        stage.setScene(new Scene(root, 300, 200));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}