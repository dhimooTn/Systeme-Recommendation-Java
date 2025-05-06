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
    @FXML private HBox recommendationsHBox;  // Changed from ListView to HBox to match FXML
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
            welcomeLabel.setText("👋 Welcome, " + user.getName() + "!");
            loadPersonalizedData();
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        resultsContainer.getChildren().clear();

        // Set up listener for search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResources();
        });
    }

    private VBox createResourceCard(RessourceModel resource) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label titleLabel = new Label(resource.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6a3093; -fx-font-size: 14px;");

        UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
        String teacherName = teacher != null ? teacher.getName() : "Unknown";
        Label detailsLabel = new Label(teacherName + " • " + resource.getCategory());
        detailsLabel.setStyle("-fx-text-fill: #9c64c3; -fx-font-size: 12px;");

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-background-radius: 4;");
        viewButton.setOnAction(e -> viewDetails(resource));

        // Modified save button logic
        InteractionModel existingInteraction = findInteractionForResource(resource.getResourceId());
        boolean isSaved = existingInteraction != null && (existingInteraction.estEnregistree() || existingInteraction.estApprouvee());

        Button saveButton = new Button(isSaved ? "Saved" : "Save");
        saveButton.setStyle(isSaved ?
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;" :
                "-fx-background-color: #d9c2f0; -fx-text-fill: #4a235a; -fx-background-radius: 4;");
        saveButton.setOnAction(e -> {
            if (isSaved) {
                showAlert("Info", "You've already saved this resource!");
            } else {
                saveResource(resource);
                saveButton.setText("Saved");
                saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;");
            }
        });

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
        savedResourcesTable.getColumns().clear();

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

        // Title column
        TableColumn<RessourceModel, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        // Teacher column
        TableColumn<RessourceModel, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(cellData -> {
            int teacherId = cellData.getValue().getTeacherId();
            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(teacherId);
            return new SimpleStringProperty(teacher != null ? teacher.getName() : "Unknown");
        });
        teacherCol.setPrefWidth(150);

        // Category column
        TableColumn<RessourceModel, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        // Difficulty column
        TableColumn<RessourceModel, String> difficultyCol = new TableColumn<>("Difficulty");
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        difficultyCol.setPrefWidth(100);

        // Actions column (unchanged)
        TableColumn<RessourceModel, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button doneButton = new Button("✓ Done");
            private final Button deleteButton = new Button("✗ Delete");
            private final Button rateButton = new Button("★ Rate");
            private final HBox actionButtons = new HBox(5, doneButton, deleteButton, rateButton);

            {
                doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 4;");
                rateButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 4;");

                doneButton.setOnAction(e -> {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    if (!resource.isApproved()) {
                        markResourceAsCompleted(resource);
                    } else {
                        showAlert("Info", "This resource is already marked as completed!");
                    }
                });

                deleteButton.setOnAction(e -> removeSavedResource(getTableView().getItems().get(getIndex())));
                rateButton.setOnAction(e -> showRatingWindow(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    if (resource.isApproved()) {
                        doneButton.setDisable(true);
                        doneButton.setStyle("-fx-background-color: #a5d6a7; -fx-text-fill: white;");
                    } else {
                        doneButton.setDisable(false);
                        doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    }
                    setGraphic(actionButtons);
                }
            }
        });
        actionsCol.setPrefWidth(200);

        savedResourcesTable.getColumns().addAll(statusCol, titleCol, teacherCol, categoryCol, difficultyCol, actionsCol);
        savedResourcesTable.setItems(savedResources);
    }

    private void loadSavedResources() {
        if (currentStudentId == 0) return;

        savedResources.clear();
        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(currentStudentId);

        for (InteractionModel interaction : interactions) {
            if (interaction.getAvis() == 2 || interaction.getAvis() == 1) {
                RessourceModel resource = ressourceDAO.getRessourceParId(interaction.getRessourceId());
                if (resource != null) {
                    resource.setApproved(interaction.getAvis() == 1);
                    savedResources.add(resource);
                }
            }
        }

        savedResources.sort((r1, r2) -> {
            boolean r1Completed = r1.isApproved();
            boolean r2Completed = r2.isApproved();
            if (r1Completed == r2Completed) {
                return r1.getTitle().compareToIgnoreCase(r2.getTitle());
            }
            return r1Completed ? -1 : 1;
        });
    }

    private void loadRecommendedResources() {
        if (currentStudentId == 0) return;

        recommendedResources.clear();
        recommendationsHBox.getChildren().clear();  // Clear existing recommendations
        recommendationTitle.setText("Recommended For You");

        try {
            List<RessourceModel> userBasedRecs = recommandationService.getUserBasedRecommendations(currentStudentId);
            List<RessourceModel> itemBasedRecs = recommandationService.getItemBasedRecommendations(currentStudentId);

            List<Integer> savedResourceIds = savedResources.stream()
                    .map(RessourceModel::getResourceId)
                    .collect(Collectors.toList());

            userBasedRecs = userBasedRecs.stream()
                    .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                    .collect(Collectors.toList());

            itemBasedRecs = itemBasedRecs.stream()
                    .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                    .collect(Collectors.toList());

            Map<RessourceModel, Double> combinedScores = new HashMap<>();
            addToRecommendations(combinedScores, userBasedRecs, 0.7);
            addToRecommendations(combinedScores, itemBasedRecs, 0.3);

            List<RessourceModel> topRecommendations = combinedScores.entrySet().stream()
                    .sorted(Map.Entry.<RessourceModel, Double>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (topRecommendations.isEmpty()) {
                loadPopularResourcesAsFallback();
            } else {
                topRecommendations.forEach(resource -> {
                    VBox card = createResourceCard(resource);
                    recommendationsHBox.getChildren().add(card);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
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

        popularResources.forEach(resource -> {
            VBox card = createResourceCard(resource);
            recommendationsHBox.getChildren().add(card);
        });
        recommendationTitle.setText("Popular Resources");
    }

    private double getAverageRating(int resourceId) {
        List<InteractionModel> interactions = interactionDAO.getInteractionsParRessource(resourceId);
        if (interactions.isEmpty()) return 0;

        double sum = interactions.stream()
                .filter(i -> i.getAvis() > 0)
                .mapToInt(InteractionModel::getAvis)
                .sum();
        return sum / interactions.stream().filter(i -> i.getAvis() > 0).count();
    }

    private void addToRecommendations(Map<RessourceModel, Double> combined, List<RessourceModel> recs, double weight) {
        for (int i = 0; i < recs.size(); i++) {
            RessourceModel res = recs.get(i);
            double score = (recs.size() - i) * weight;
            combined.merge(res, score, Double::sum);
        }
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
            loadRecommendedResources();
        }
    }

    private void removeSavedResource(RessourceModel resource) {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
        if (interaction != null) {
            interactionDAO.supprimerInteraction(interaction.getId());
            savedResources.remove(resource);
            showAlert("Success", "Resource removed from your list!");
            updateCounters();
            loadRecommendedResources();
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
        resource.setApproved(false);
        savedResources.add(resource);
        showAlert("Success", "Resource saved to your list!");
        updateCounters();
        loadRecommendedResources();

        if (!searchField.getText().isEmpty()) {
            filterResources();
        }
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
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label prompt = new Label("How would you rate this resource?");
        prompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a235a;");

        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 3);
        ratingSpinner.setStyle("-fx-font-size: 14px;");
        ratingSpinner.setEditable(true);

        Button submitButton = new Button("Submit Rating");
        submitButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        submitButton.setOnAction(e -> {
            int rating = ratingSpinner.getValue();
            saveRating(resource, rating);
            stage.close();
        });

        root.getChildren().addAll(prompt, ratingSpinner, submitButton);
        root.setAlignment(Pos.CENTER);
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
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
        String teacherName = teacher != null ? teacher.getName() : "Unknown";

        InteractionModel studentInteraction = findInteractionForResource(resource.getResourceId());
        String status = "Not saved";
        if (studentInteraction != null) {
            status = studentInteraction.estApprouvee() ? "Completed" :
                    studentInteraction.estEnregistree() ? "Saved" : "Rated";
        }

        Label titleLabel = new Label(resource.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6a3093;");

        Label teacherLabel = new Label("By: " + teacherName);
        teacherLabel.setStyle("-fx-text-fill: #4a235a;");

        Label categoryLabel = new Label("Category: " + resource.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #4a235a;");

        Label difficultyLabel = new Label("Difficulty: " + resource.getDifficulty());
        difficultyLabel.setStyle("-fx-text-fill: #4a235a;");

        Label statusLabel = new Label("Your status: " + status);
        statusLabel.setStyle("-fx-text-fill: #4a235a;");

        double avgRating = getAverageRating(resource.getResourceId());
        Label ratingLabel = new Label("Rating: " + (avgRating > 0 ?
                String.format("★ %.1f/5", avgRating) : "Not rated yet"));
        ratingLabel.setStyle("-fx-text-fill: #4a235a;");

        TextArea descriptionArea = new TextArea(resource.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(150);
        descriptionArea.setStyle("-fx-border-color: #d9c2f0;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveButton = new Button(status.equals("Saved") ? "Already Saved" : "Save Resource");
        saveButton.setStyle("-fx-background-color: #9c64c3; -fx-text-fill: white; -fx-background-radius: 4;");
        saveButton.setDisable(status.equals("Saved") || status.equals("Completed"));
        saveButton.setOnAction(e -> {
            saveResource(resource);
            stage.close();
        });

        Button rateButton = new Button("Rate Resource");
        rateButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 4;");
        rateButton.setOnAction(e -> {
            stage.close();
            showRatingWindow(resource);
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-background-radius: 4;");
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

        if (query.isEmpty()) {
            return;
        }

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
            card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            Label titleLabel = new Label(resource.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6a3093;");

            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
            String teacherName = teacher != null ? teacher.getName() : "Unknown";

            Label detailsLabel = new Label(String.format("%s • %s • %s",
                    teacherName,
                    resource.getCategory(),
                    resource.getDifficulty()));
            detailsLabel.setStyle("-fx-text-fill: #9c64c3;");

            HBox buttonBox = new HBox(10);
            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-background-radius: 4;");
            viewButton.setOnAction(e -> viewDetails(resource));

            Button saveButton = new Button("Save");
            saveButton.setStyle("-fx-background-color: #d9c2f0; -fx-text-fill: #4a235a; -fx-background-radius: 4;");
            saveButton.setOnAction(e -> saveResource(resource));

            buttonBox.getChildren().addAll(viewButton, saveButton);
            card.getChildren().addAll(titleLabel, detailsLabel, buttonBox);
            resultsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void openFilters() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Search Filters");

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label titleLabel = new Label("Search Filters");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6a3093;");

        CheckBox titleCheck = new CheckBox("Search in titles");
        titleCheck.setSelected(filterByTitle);

        CheckBox categoryCheck = new CheckBox("Search in categories");
        categoryCheck.setSelected(filterByCategory);

        CheckBox teacherCheck = new CheckBox("Search in teacher names");
        teacherCheck.setSelected(filterByTeacher);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button applyButton = new Button("Apply");
        applyButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-background-radius: 4;");
        applyButton.setOnAction(e -> {
            filterByTitle = titleCheck.isSelected();
            filterByCategory = categoryCheck.isSelected();
            filterByTeacher = teacherCheck.isSelected();
            stage.close();
            filterResources();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-background-radius: 4;");
        cancelButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(cancelButton, applyButton);
        root.getChildren().addAll(titleLabel, titleCheck, categoryCheck, teacherCheck, buttonBox);

        stage.setScene(new Scene(root, 300, 200));
        stage.show();
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

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), currentRoot);
            scaleDown.setFromX(1.0);
            scaleDown.setFromY(1.0);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), newRoot);
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