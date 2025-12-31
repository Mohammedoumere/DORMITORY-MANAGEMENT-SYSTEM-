package com.dormitory.controllers;

import com.dormitory.models.User;
import javafx.scene.control.Alert;

public interface CommonController {
    void setCurrentUser(User user);

    default void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}