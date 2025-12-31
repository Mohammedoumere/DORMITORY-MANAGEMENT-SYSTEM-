package com.dormitory.controllers;

import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StudentWelcomeController implements CommonController {

    @FXML private Label welcomeLabel;

    @Override
    public void setCurrentUser(User user) {
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
        }
    }
}
