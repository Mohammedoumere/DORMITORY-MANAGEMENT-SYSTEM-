package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileViewController implements CommonController {

    private User currentUser;

    @FXML private ImageView profileImageView;
    @FXML private Circle profileCircle;
    @FXML private TextField profileUsernameField;
    @FXML private TextField profileEmailField;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadProfileData();
    }

    @FXML
    private void initialize() {
        Circle clip = new Circle(57.5, 57.5, 57.5);
        profileImageView.setClip(clip);
    }

    private void loadProfileData() {
        if (currentUser == null) return;

        profileUsernameField.setText(currentUser.getFullName());
        profileEmailField.setText(currentUser.getEmail());
        
        String imagePath = getProfilePicturePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                profileImageView.setImage(new Image(new File(imagePath).toURI().toString()));
                profileCircle.setFill(Color.TRANSPARENT);
            } catch (Exception e) {
                setDefaultProfileLook();
            }
        } else {
            setDefaultProfileLook();
        }
    }
    
    private void setDefaultProfileLook() {
        profileImageView.setImage(null);
        profileCircle.setFill(Color.LIGHTGRAY);
    }

    @FXML
    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) profileImageView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                profileCircle.setFill(Color.TRANSPARENT);
                saveProfilePicturePath(selectedFile.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Image Error", "Could not load the selected image.");
            }
        }
    }

    private String getProfilePicturePath() {
        String[] tableInfo = getTableAndIdColumn();
        if (tableInfo == null) return null;

        String path = null;
        String query = "SELECT profile_picture_path FROM " + tableInfo[0] + " WHERE " + tableInfo[1] + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if ("student_service".equals(tableInfo[0])) {
                pstmt.setInt(1, Integer.parseInt(currentUser.getId()));
            } else {
                pstmt.setString(1, currentUser.getId());
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                path = rs.getString("profile_picture_path");
            }
        } catch (SQLException e) {
            System.err.println("WARNING: Could not get profile picture path. " + e.getMessage());
        }
        return path;
    }

    private void saveProfilePicturePath(String path) {
        String[] tableInfo = getTableAndIdColumn();
        if (tableInfo == null) return;

        String query = "UPDATE " + tableInfo[0] + " SET profile_picture_path = ? WHERE " + tableInfo[1] + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, path);
            
            if ("student_service".equals(tableInfo[0])) {
                pstmt.setInt(2, Integer.parseInt(currentUser.getId()));
            } else {
                pstmt.setString(2, currentUser.getId());
            }

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture updated successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.WARNING, "Database Update Needed", 
                "Profile picture updated for this session only.\n\n" +
                "To save it permanently, please ensure the database schema is up to date.");
        }
    }

    @FXML
    private void updateProfile() {
        String[] tableInfo = getTableAndIdColumn();
        if (tableInfo == null) return;

        String fullName = profileUsernameField.getText();
        if (fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username cannot be empty.");
            return;
        }

        String query = "UPDATE " + tableInfo[0] + " SET full_name = ? WHERE " + tableInfo[1] + " = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fullName);
            
            if ("student_service".equals(tableInfo[0])) {
                pstmt.setInt(2, Integer.parseInt(currentUser.getId()));
            } else {
                pstmt.setString(2, currentUser.getId());
            }
            pstmt.executeUpdate();

            currentUser.setFullName(fullName);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile name updated successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update profile name.");
            e.printStackTrace();
        }
    }

    private String[] getTableAndIdColumn() {
        if (currentUser == null || currentUser.getUserType() == null) {
            return null;
        }
        switch (currentUser.getUserType()) {
            case "STUDENT":
                return new String[]{"student", "student_id"};
            case "PROCTOR":
                return new String[]{"proctor", "proctor_id"};
            case "STUDENT_SERVICE":
                return new String[]{"student_service", "service_id"};
            default:
                return null; // This ensures all paths return a value
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
