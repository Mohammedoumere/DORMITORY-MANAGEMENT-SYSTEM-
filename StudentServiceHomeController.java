package com.dormitory.controllers;

import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentServiceHomeController implements CommonController {

    private User currentUser;

    @FXML private BorderPane contentPane;
    @FXML private VBox navBox;
    @FXML private Button homeButton;
    @FXML private Button categoriesButton;
    @FXML private Button reportsButton;
    @FXML private Button profileButton;
    @FXML private Button settingsButton;
    
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        showHomeView();
    }

    @FXML
    private void initialize() {
        // Initialization is handled after user is set
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            
            if (loader.getController() instanceof CommonController) {
                ((CommonController) loader.getController()).setCurrentUser(currentUser);
            }
            
            contentPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveNav(Node selectedButton) {
        for (Node node : navBox.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("nav-button-active");
            }
        }
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    private void showHomeView() {
        loadView("AdminHomeView.fxml"); // Corrected View
        setActiveNav(homeButton);
    }

    @FXML
    private void showCategoriesView() {
        loadView("CategoriesView.fxml");
        setActiveNav(categoriesButton);
    }

    @FXML
    private void showReportsView() {
        loadView("StudentServiceReportsView.fxml");
        setActiveNav(reportsButton);
    }

    @FXML
    private void showProfileView() {
        loadView("Profile.fxml");
        setActiveNav(profileButton);
    }

    @FXML
    private void showSettingsView() {
        loadView("Settings.fxml");
        setActiveNav(settingsButton);
    }

    @FXML
    public void closeApplication() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    public void minimizeApplication() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void maximizeApplication() {
        Stage stage = (Stage) maximizeButton.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
}
