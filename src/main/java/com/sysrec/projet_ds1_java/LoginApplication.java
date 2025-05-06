package com.sysrec.projet_ds1_java;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create the permanent root container
        StackPane rootContainer = new StackPane();

        // Load the initial login view
        Parent loginView = FXMLLoader.load(getClass().getResource("/com/sysrec/projet_ds1_java/View/LoginView.fxml"));

        // Add the login view to our container
        rootContainer.getChildren().add(loginView);

        // Create scene with purple background
        Scene scene = new Scene(rootContainer, 1000, 600);
        scene.setFill(javafx.scene.paint.Color.valueOf("#483d8b")); // Match your theme color

        // Configure and show stage 
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}