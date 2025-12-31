package com.dormitory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.Optional;

public class App extends Application {
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        if (!connectToDatabase()) {
            Platform.exit();
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/com/dormitory/controllers/Login.fxml"));
        Scene scene = new Scene(root);
        
        scene.getStylesheets().add(getClass().getResource("/com/dormitory/controllers/login.css").toExternalForm());
        
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        
        stage.setTitle("Dormitory Management System");
        stage.initStyle(StageStyle.UNDECORATED);
        
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dormitory/images/Picture-1.jpg")));
        } catch (Exception e) {
            System.out.println("Icon not found.");
        }

        stage.setScene(scene);
        stage.show();
    }
    
    private boolean connectToDatabase() {
        try {
            DatabaseConnection.connect("postgres", "postgres"); // Try default
            System.out.println("Default database connection successful.");
            return true;
        } catch (SQLException e) {
            // This is expected if the default password is wrong
        }
        return showDatabaseLoginDialog();
    }

    private boolean showDatabaseLoginDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Database Connection");
        dialog.setHeaderText("Could not connect to database.\nPlease enter your PostgreSQL credentials.");

        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/com/dormitory/images/Picture-1.jpg")));
            logoView.setFitHeight(60);
            logoView.setPreserveRatio(true);
            dialog.setGraphic(logoView);
        } catch (Exception e) {
            // Ignore if logo not found
        }

        ButtonType loginButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField("postgres");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(password::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        while (true) {
            Optional<Pair<String, String>> result = dialog.showAndWait();
            if (result.isPresent()) {
                Pair<String, String> credentials = result.get();
                try {
                    DatabaseConnection.connect(credentials.getKey(), credentials.getValue());
                    System.out.println("Database connection established successfully via dialog.");
                    return true;
                } catch (SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection Failed");
                    alert.setHeaderText("Could not connect to database");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            } else {
                return false;
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}