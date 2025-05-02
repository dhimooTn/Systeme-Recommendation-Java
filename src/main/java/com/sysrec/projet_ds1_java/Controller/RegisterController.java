package com.sysrec.projet_ds1_java.Controller;

import com.sysrec.projet_ds1_java.Dao.UtilisateurDAO;
import com.sysrec.projet_ds1_java.Model.UtilisateurModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;

public class RegisterController {

    @FXML private StackPane root;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton teacherRadio;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button loginButton;  // Back to Login button
    @FXML private Label errorLabel;

    private ToggleGroup roleGroup;

    @FXML
    public void initialize() {
        // Initialize toggle group
        roleGroup = new ToggleGroup();
        studentRadio.setToggleGroup(roleGroup);
        studentRadio.setUserData("student");
        teacherRadio.setToggleGroup(roleGroup);
        teacherRadio.setUserData("teacher");
        studentRadio.setSelected(true);

        // Set button actions
        registerButton.setOnAction(e -> handleRegister());
        loginButton.setOnAction(e -> navigateToLogin());  // Fixed back navigation
    }

    private void navigateToLogin() {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource("/com/sysrec/projet_ds1_java/View/LoginView.fxml"));
            Scene currentScene = loginButton.getScene();
            StackPane container = (StackPane) currentScene.getRoot();

            newRoot.setOpacity(0);
            container.getChildren().add(newRoot);

            // Create transitions
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
                container.getChildren().remove(root);
                ((Stage) currentScene.getWindow()).setTitle("Login");
            });

            new ParallelTransition(fadeOut, fadeIn, scaleDown, scaleUp).play();

        } catch (Exception e) {
            showError("Error loading login form");
            e.printStackTrace();
        }
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleGroup.getSelectedToggle().getUserData().toString();

        // Input validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email!");
            return;
        }

        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

            // Check if email already exists
            if (utilisateurDAO.emailExisteDeja(email)) {
                showError("Email already exists!");
                return;
            }

            // Create new user model
            UtilisateurModel newUser = new UtilisateurModel(fullName, email, password, role);

            // Add user to database
            utilisateurDAO.ajouterUtilisateur(newUser);

            showSuccess("Registration successful! Redirecting...");

            // Delay before transition
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> navigateToLogin());
            delay.play();

        } catch (Exception e) {
            showError("Error during registration. Please try again.");
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