package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import com.sysrec.projet_ds1_java.Model.UtilisateurModel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private StackPane root;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button createAccountButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        createAccountButton.setOnAction(e -> navigateToRegister());
        loginButton.setOnAction(e -> handleLogin());
    }

    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sysrec/projet_ds1_java/View/RegisterView.fxml"));
            Parent newRoot = loader.load();
            Scene currentScene = root.getScene();
            StackPane mainContainer = (StackPane) currentScene.getRoot();

            newRoot.setOpacity(0);
            mainContainer.getChildren().add(newRoot);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), root);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newRoot);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), newRoot);

            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            scaleDown.setFromX(1.0);
            scaleDown.setFromY(1.0);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);

            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            scaleUp.setFromX(0.95);
            scaleUp.setFromY(0.95);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            fadeOut.setOnFinished(e -> {
                mainContainer.getChildren().remove(root);
                ((Stage) currentScene.getWindow()).setTitle("Register");
            });

            new ParallelTransition(fadeOut, fadeIn, scaleDown, scaleUp).play();

        } catch (Exception e) {
            showError("Error loading registration form");
            e.printStackTrace();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required!");
            return;
        }

        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            UtilisateurModel utilisateur = utilisateurDAO.connexion(email, password);

            if (utilisateur != null) {
                showSuccess("Login successful! Redirecting...");
                redirectToDashboard(utilisateur);
            } else {
                showError("Invalid email or password!");
            }
        } catch (Exception e) {
            showError("Error during login. Please try again.");
            e.printStackTrace();
        }
    }

    private void redirectToDashboard(UtilisateurModel utilisateur) {
        try {
            String fxmlPath = utilisateur.getRole().equals("teacher")
                    ? "/com/sysrec/projet_ds1_java/View/TeacherView.fxml"
                    : "/com/sysrec/projet_ds1_java/View/StudentView.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            // Passage des informations de l'utilisateur au contrôleur approprié
            if (utilisateur.getRole().equals("student")) {
                StudentController studentController = loader.getController();
                studentController.setCurrentStudent(utilisateur.getUserId());
            } else if (utilisateur.getRole().equals("teacher")) {
                TeacherController teacherController = loader.getController();
                teacherController.setCurrentTeacher(utilisateur.getUserId());
            }

            Scene currentScene = root.getScene();
            StackPane mainContainer = (StackPane) currentScene.getRoot();

            newRoot.setOpacity(0);
            mainContainer.getChildren().add(newRoot);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newRoot);

            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            fadeOut.setOnFinished(e -> {
                mainContainer.getChildren().remove(root);
                ((Stage) currentScene.getWindow()).setTitle(utilisateur.getRole().equals("teacher")
                        ? "Teacher Dashboard" : "Student Dashboard");
            });

            new ParallelTransition(fadeOut, fadeIn).play();

        } catch (Exception e) {
            showError("Error loading dashboard");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #2ecc71;");

        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
