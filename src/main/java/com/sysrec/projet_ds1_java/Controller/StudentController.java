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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StudentController {

    // FXML injected components
    @FXML private TableView<RessourceModel> savedResourcesTable;
    @FXML private Label savedLabel;
    @FXML private Label completedLabel;
    @FXML private TextField searchField;
    @FXML private VBox resultsContainer;
    @FXML private Label welcomeLabel;
    @FXML private HBox recommendationsHBox;
    @FXML private Label recommendationTitle;
    @FXML private Label errorLabel;
    @FXML private Label welcomeBackLabel;

    // Filter settings
    private boolean filterByTitle = true;
    private boolean filterByCategory = true;
    private boolean filterByTeacher = true;

    // Data fields
    private int currentStudentId;
    private StudentModel currentStudent;
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private final InteractionDAO interactionDAO = new InteractionDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final RecommandationService recommandationService = new RecommandationService();

    // Observable lists
    private final ObservableList<RessourceModel> savedResources = FXCollections.observableArrayList();
    private final ObservableList<RessourceModel> recommendedResources = FXCollections.observableArrayList();

    public void setCurrentStudent(int studentId) throws SQLException {
        this.currentStudentId = studentId;
        UtilisateurModel user = utilisateurDAO.getUtilisateurParId(studentId);
        if (user != null) {
            this.currentStudent = new StudentModel(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    interactionDAO,
                    ressourceDAO
            );
            safeSetLabelText(welcomeLabel, "👋 Welcome, " + user.getName() + "!");
            safeSetLabelText(welcomeBackLabel, "👋 Welcome back, " + user.getName() + "!");
            loadPersonalizedData();
        } else {
            showError("Failed to load user data.");
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        resultsContainer.getChildren().clear();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterResources());
    }

    private void safeSetLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private VBox createResourceCard(RessourceModel resource) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        try {
            Label titleLabel = new Label(resource.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6a3093; -fx-font-size: 14px;");

            UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
            String teacherName = (teacher != null) ? teacher.getName() : "Unknown";
            Label detailsLabel = new Label(teacherName + " • " + resource.getCategory());
            detailsLabel.setStyle("-fx-text-fill: #9c64c3; -fx-font-size: 12px;");

            HBox buttonBox = new HBox(8);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-background-radius: 4;");
            viewButton.setOnAction(e -> {
                try {
                    viewDetails(resource);
                } catch (SQLException ex) {
                    showError("Error viewing resource details");
                }
            });

            InteractionModel existingInteraction = findInteractionForResource(resource.getResourceId());
            boolean isSaved = existingInteraction != null && existingInteraction.getSavedAt() != null;

            Button saveButton = new Button(isSaved ? "Saved" : "Save");
            saveButton.setStyle(isSaved ?
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;" :
                    "-fx-background-color: #d9c2f0; -fx-text-fill: #4a235a; -fx-background-radius: 4;");
            saveButton.setOnAction(e -> handleSaveResource(resource, saveButton));

            buttonBox.getChildren().addAll(viewButton, saveButton);
            card.getChildren().addAll(titleLabel, detailsLabel, buttonBox);
        } catch (SQLException e) {
            showError("Error creating resource card");
        }
        return card;
    }

    private void handleSaveResource(RessourceModel resource, Button saveButton) {
        try {
            InteractionModel existingInteraction = findInteractionForResource(resource.getResourceId());
            if (existingInteraction != null && existingInteraction.getSavedAt() != null) {
                showAlert("Info", "You've already saved this resource!");
            } else {
                saveResource(resource);
                saveButton.setText("Saved");
                saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4;");
            }
        } catch (SQLException ex) {
            showError("Error saving resource: " + ex.getMessage());
        }
    }

    private void loadPersonalizedData() {
        try {
            loadSavedResources();
            loadRecommendedResources();
            updateCounters();
        } catch (SQLException e) {
            showError("Error loading personal data");
        }
    }

    private void setupTableColumns() {
        savedResourcesTable.getColumns().clear();

        // Status Column
        TableColumn<RessourceModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            try {
                InteractionModel interaction = findInteractionForResource(cellData.getValue().getResourceId());
                return new SimpleStringProperty(interaction != null && interaction.getRatedAt() != null ? "✓ Completed" : "● Saved");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
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
                    setStyle(item.equals("✓ Completed") ?
                            "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" :
                            "-fx-text-fill: #6a3093;");
                }
            }
        });
        statusCol.setPrefWidth(100);

        // Title Column
        TableColumn<RessourceModel, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        // Teacher Column
        TableColumn<RessourceModel, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(cellData -> {
            try {
                UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(cellData.getValue().getTeacherId());
                return new SimpleStringProperty(teacher != null ? teacher.getName() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });
        teacherCol.setPrefWidth(150);

        // Category Column
        TableColumn<RessourceModel, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        // Difficulty Column
        TableColumn<RessourceModel, String> difficultyCol = new TableColumn<>("Difficulty");
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        difficultyCol.setPrefWidth(100);

        // Actions Column
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

                doneButton.setOnAction(e -> handleDoneAction());
                deleteButton.setOnAction(e -> handleDeleteAction());
                rateButton.setOnAction(e -> handleRateAction());
            }

            private void handleDoneAction() {
                RessourceModel resource = getTableView().getItems().get(getIndex());
                try {
                    InteractionModel interaction = findInteractionForResource(resource.getResourceId());
                    if (interaction != null && interaction.getRatedAt() == null) {
                        markResourceAsCompleted(resource);
                    } else {
                        showAlert("Info", "This resource is already marked as completed!");
                    }
                } catch (SQLException ex) {
                    showError("Error marking resource as completed");
                }
            }

            private void handleDeleteAction() {
                try {
                    removeSavedResource(getTableView().getItems().get(getIndex()));
                } catch (SQLException ex) {
                    showError("Error removing resource");
                }
            }

            private void handleRateAction() {
                showRatingWindow(getTableView().getItems().get(getIndex()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RessourceModel resource = getTableView().getItems().get(getIndex());
                    try {
                        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
                        boolean isCompleted = interaction != null && interaction.getRatedAt() != null;
                        doneButton.setDisable(isCompleted);
                        doneButton.setStyle(isCompleted ?
                                "-fx-background-color: #a5d6a7; -fx-text-fill: white;" :
                                "-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        setGraphic(actionButtons);
                    } catch (SQLException e) {
                        setGraphic(null);
                    }
                }
            }
        });
        actionsCol.setPrefWidth(200);

        savedResourcesTable.getColumns().addAll(statusCol, titleCol, teacherCol, categoryCol, difficultyCol, actionsCol);
        savedResourcesTable.setItems(savedResources);
    }

    private void loadSavedResources() {
        if (currentStudentId <= 0) return;

        Task<List<RessourceModel>> task = new Task<>() {
            @Override
            protected List<RessourceModel> call() throws SQLException {
                List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(currentStudentId);
                List<RessourceModel> resources = new ArrayList<>();

                for (InteractionModel interaction : interactions) {
                    if (interaction.getSavedAt() != null) {
                        RessourceModel resource = ressourceDAO.getRessourceParId(interaction.getResourceId());
                        if (resource != null) {
                            resources.add(resource);
                        }
                    }
                }
                return resources;
            }
        };

        task.setOnSucceeded(e -> {
            savedResources.setAll(task.getValue());
            savedResources.sort((r1, r2) -> {
                try {
                    InteractionModel i1 = findInteractionForResource(r1.getResourceId());
                    InteractionModel i2 = findInteractionForResource(r2.getResourceId());
                    boolean r1Completed = i1 != null && i1.getRatedAt() != null;
                    boolean r2Completed = i2 != null && i2.getRatedAt() != null;
                    return r1Completed == r2Completed ?
                            r1.getTitle().compareToIgnoreCase(r2.getTitle()) :
                            r1Completed ? -1 : 1;
                } catch (SQLException ex) {
                    return 0;
                }
            });
            try {
                updateCounters();
            } catch (SQLException ex) {
                showError("Error updating counters");
            }
        });

        task.setOnFailed(e -> showError("Failed to load saved resources"));
        new Thread(task).start();
    }

    private void loadRecommendedResources() {
        if (currentStudentId <= 0) return;

        Task<List<RessourceModel>> task = new Task<>() {
            @Override
            protected List<RessourceModel> call() throws Exception {
                List<Integer> savedResourceIds = interactionDAO.getInteractionsParEtudiant(currentStudentId).stream()
                        .filter(i -> i.getSavedAt() != null)
                        .map(InteractionModel::getResourceId)
                        .collect(Collectors.toList());

                // Get recommendations from both algorithms
                List<RessourceModel> userBasedRecs = recommandationService.getUserBasedRecommendations(currentStudentId);
                List<RessourceModel> itemBasedRecs = recommandationService.getItemBasedRecommendations(currentStudentId);

                // Filter out already saved resources
                userBasedRecs = userBasedRecs.stream()
                        .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                        .collect(Collectors.toList());

                itemBasedRecs = itemBasedRecs.stream()
                        .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                        .collect(Collectors.toList());

                // Combine and deduplicate recommendations
                Set<RessourceModel> combined = new LinkedHashSet<>();
                combined.addAll(userBasedRecs);
                combined.addAll(itemBasedRecs);

                // If we have recommendations, return them
                if (!combined.isEmpty()) {
                    return new ArrayList<>(combined);
                }

                // Fallback to popular resources if no recommendations
                return ressourceDAO.getRessourcesApprouvees().stream()
                        .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                        .sorted((r1, r2) -> Double.compare(
                                r2.getAverageRating(),
                                r1.getAverageRating()))
                        .limit(5)
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            recommendationsHBox.getChildren().clear();
            List<RessourceModel> recommendations = task.getValue();

            if (recommendations.isEmpty()) {
                recommendationTitle.setText("Recommendations");
                Label noRecsLabel = new Label("You don't have recommendations yet. Try saving some resources first!");
                noRecsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
                recommendationsHBox.getChildren().add(noRecsLabel);
            } else {
                recommendationTitle.setText("Recommended For You");
                recommendations.forEach(resource -> {
                    VBox card = createResourceCard(resource);
                    recommendationsHBox.getChildren().add(card);
                });
            }
        });

        task.setOnFailed(e -> {
            showError("Failed to load recommendations");
            loadPopularResourcesAsFallback();
        });

        new Thread(task).start();
    }

    private void loadPopularResourcesAsFallback() {
        Task<List<RessourceModel>> task = new Task<>() {
            @Override
            protected List<RessourceModel> call() throws SQLException {
                List<Integer> savedResourceIds = interactionDAO.getInteractionsParEtudiant(currentStudentId).stream()
                        .filter(i -> i.getSavedAt() != null)
                        .map(InteractionModel::getResourceId)
                        .collect(Collectors.toList());

                return ressourceDAO.getRessourcesApprouvees().stream()
                        .filter(r -> !savedResourceIds.contains(r.getResourceId()))
                        .sorted((r1, r2) -> Double.compare(
                                r2.getAverageRating(),
                                r1.getAverageRating()))
                        .limit(5)
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            recommendationsHBox.getChildren().clear();
            recommendationTitle.setText("Popular Resources");
            task.getValue().forEach(resource -> {
                try {
                    VBox card = createResourceCard(resource);
                    recommendationsHBox.getChildren().add(card);
                } catch (Exception ex) {
                    showError("Error creating resource card");
                }
            });
        });

        task.setOnFailed(e -> showError("Failed to load popular resources"));
        new Thread(task).start();
    }

    private void markResourceAsCompleted(RessourceModel resource) throws SQLException {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
        if (interaction != null) {
            interaction.setRating(5);
            interaction.setRatedAt(LocalDateTime.now());
            interactionDAO.modifierInteraction(interaction);
            savedResourcesTable.refresh();
            showAlert("Success", "Resource marked as completed!");
            updateCounters();
            loadRecommendedResources();
        } else {
            showAlert("Error", "Interaction not found for this resource.");
        }
    }

    private void removeSavedResource(RessourceModel resource) throws SQLException {
        InteractionModel interaction = findInteractionForResource(resource.getResourceId());
        if (interaction != null) {
            interactionDAO.supprimerInteraction(interaction.getInteractionId());
            savedResources.remove(resource);
            showAlert("Success", "Resource removed from your list!");
            updateCounters();
            loadRecommendedResources();
        } else {
            showAlert("Error", "Interaction not found for this resource.");
        }
    }

    private InteractionModel findInteractionForResource(int resourceId) throws SQLException {
        if (currentStudentId <= 0) return null;
        return interactionDAO.getInteractionsParEtudiant(currentStudentId).stream()
                .filter(i -> i.getResourceId() == resourceId)
                .findFirst()
                .orElse(null);
    }

    private void saveResource(RessourceModel resource) throws SQLException {
        if (currentStudentId <= 0 || resource == null || resource.getResourceId() <= 0) {
            showAlert("Error", "Invalid data");
            return;
        }

        InteractionModel existingInteraction = findInteractionForResource(resource.getResourceId());
        if (existingInteraction != null) {
            if (existingInteraction.getSavedAt() == null) {
                existingInteraction.setRatedAt(LocalDateTime.now());
                interactionDAO.modifierInteraction(existingInteraction);
            }
        } else {
            InteractionModel newInteraction = new InteractionModel(
                    currentStudentId,
                    resource.getResourceId(),
                    LocalDateTime.now(),
                    0,
                    null
            );
            interactionDAO.ajouterInteraction(newInteraction);
        }

        savedResources.add(resource);
        showAlert("Success", "Resource saved to your list!");
        updateCounters();
        loadRecommendedResources();

        if (!searchField.getText().isEmpty()) {
            filterResources();
        }
    }

    private void updateCounters() throws SQLException {
        if (currentStudentId <= 0) return;

        List<InteractionModel> interactions = interactionDAO.getInteractionsParEtudiant(currentStudentId);

        long savedCount = interactions.stream()
                .filter(i -> i.getSavedAt() != null)
                .count();

        long completedCount = interactions.stream()
                .filter(i -> i.getRatedAt() != null)
                .count();

        safeSetLabelText(savedLabel, String.valueOf(savedCount));
        safeSetLabelText(completedLabel, String.valueOf(completedCount));
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
            try {
                saveRating(resource, ratingSpinner.getValue());
                stage.close();
            } catch (SQLException ex) {
                showError("Error saving rating");
            }
        });

        root.getChildren().addAll(prompt, ratingSpinner, submitButton);
        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root, 300, 150));
        stage.show();
    }

    private void saveRating(RessourceModel resource, int rating) throws SQLException {
        if (rating < 1 || rating > 5) {
            showAlert("Error", "Rating must be between 1 and 5.");
            return;
        }

        InteractionModel interaction = findInteractionForResource(resource.getResourceId());

        if (interaction != null) {
            interaction.setRating(rating);
            interaction.setRatedAt(LocalDateTime.now());
            interactionDAO.modifierInteraction(interaction);
        } else {
            InteractionModel newInteraction = new InteractionModel(
                    currentStudentId,
                    resource.getResourceId(),
                    null,
                    rating,
                    LocalDateTime.now()
            );
            interactionDAO.ajouterInteraction(newInteraction);
        }

        showAlert("Thank You", "Your rating has been saved!");
        loadRecommendedResources();
    }

    private void viewDetails(RessourceModel resource) throws SQLException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resource.getTitle());

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
        String teacherName = teacher != null ? teacher.getName() : "Unknown";

        InteractionModel studentInteraction = findInteractionForResource(resource.getResourceId());
        String status = studentInteraction != null ?
                (studentInteraction.getRatedAt() != null ? "Completed" :
                        studentInteraction.getSavedAt() != null ? "Saved" : "Rated") :
                "Not saved";

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

        Label ratingLabel = new Label("Rating: " + (resource.getAverageRating() > 0 ?
                String.format("★ %.1f/5", resource.getAverageRating()) : "Not rated yet"));
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
            try {
                saveResource(resource);
                stage.close();
            } catch (SQLException ex) {
                showError("Error saving resource");
            }
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

        if (query.isEmpty()) return;

        Task<List<RessourceModel>> task = new Task<>() {
            @Override
            protected List<RessourceModel> call() throws Exception {
                List<Integer> savedResourceIds = interactionDAO.getInteractionsParEtudiant(currentStudentId).stream()
                        .filter(i -> i.getSavedAt() != null)
                        .map(InteractionModel::getResourceId)
                        .collect(Collectors.toList());

                return ressourceDAO.getRessourcesApprouvees().stream()
                        .filter(resource -> !savedResourceIds.contains(resource.getResourceId()))
                        .filter(resource -> matchesSearch(resource, query))
                        .limit(20)
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> showSearchResults(task.getValue()));
        task.setOnFailed(e -> showError("Failed to search resources"));
        new Thread(task).start();
    }

    private void showSearchResults(List<RessourceModel> results) {
        resultsContainer.getChildren().clear();

        if (results.isEmpty()) {
            Label noResults = new Label("No resources found matching your search.");
            noResults.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            resultsContainer.getChildren().add(noResults);
            return;
        }

        results.forEach(resource -> {
            try {
                if (findInteractionForResource(resource.getResourceId()) == null) {
                    VBox card = createResourceCard(resource);
                    resultsContainer.getChildren().add(card);
                }
            } catch (SQLException e) {
                showError("Error creating resource card");
            }
        });
    }

    private boolean matchesSearch(RessourceModel resource, String query) {
        if (query == null || resource == null) return false;

        String lowerQuery = query.toLowerCase();

        if (filterByTitle && resource.getTitle() != null && resource.getTitle().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        if (filterByCategory && resource.getCategory() != null && resource.getCategory().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        if (filterByTeacher) {
            try {
                UtilisateurModel teacher = utilisateurDAO.getUtilisateurParId(resource.getTeacherId());
                if (teacher != null && teacher.getName() != null && teacher.getName().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }

        return false;
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
        applyButton.setStyle("-fx-background-color: #6a3093; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
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
            Parent root = loader.load();
            Stage stage = (Stage) savedResourcesTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showError("Failed to load login view");
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
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            errorLabel.setVisible(true);

            FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } else {
            System.err.println("Error: " + message);
            showAlert("Error", message);
        }
    }
}