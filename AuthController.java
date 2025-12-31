package com.dormitory.controllers;

import com.dormitory.models.*;
import com.dormitory.DatabaseConnection;
import com.dormitory.controllers.CommonController;
import com.dormitory.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.*;

public class AuthController {
    
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private TextField visiblePassword;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button signUpStudentButton;
    @FXML private Button signUpProctorButton;
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    @FXML
    private void initialize() {
        userTypeCombo.getItems().addAll("Student", "Proctor", "Student Service");
        userTypeCombo.getSelectionModel().selectFirst();
        
        if (showPasswordCheckBox != null) {
            visiblePassword.managedProperty().bind(showPasswordCheckBox.selectedProperty());
            visiblePassword.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
            loginPassword.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
            loginPassword.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
            visiblePassword.textProperty().bindBidirectional(loginPassword.textProperty());
        }
        
        loginPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });
        if (visiblePassword != null) {
            visiblePassword.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) handleLogin();
            });
        }
    }
    
    @FXML
    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();
        String userType = userTypeCombo.getValue();
        
        if (email.isEmpty() || password.isEmpty() || userType == null) {
            errorLabel.setText("Please fill all fields");
            return;
        }
        
        try {
            User user = authenticateUser(email, password, userType);
            if (user != null) {
                redirectToHome(user);
            } else {
                errorLabel.setText("Invalid email or password");
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private User authenticateUser(String email, String password, String userType) throws SQLException {
        String query = "";
        
        switch (userType) {
            case "Student":
                // CORRECTED: Added block_id to the query
                query = "SELECT student_id as id, full_name, email, password, status, block_id FROM student WHERE email = ?";
                break;
            case "Proctor":
                query = "SELECT proctor_id as id, full_name, email, password, block_id, status FROM proctor WHERE email = ?";
                break;
            case "Student Service":
                query = "SELECT service_id as id, full_name, email, password FROM student_service WHERE email = ?";
                break;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (PasswordUtils.verifyPassword(password, storedPassword)) {
                    switch (userType) {
                        case "Student":
                            Student student = new Student();
                            student.setId(rs.getString("id"));
                            student.setEmail(rs.getString("email"));
                            student.setFullName(rs.getString("full_name"));
                            student.setStatus(rs.getString("status"));
                            student.setBlockId(rs.getString("block_id")); // CORRECTED: Set the blockId
                            return student;
                            
                        case "Proctor":
                            Proctor proctor = new Proctor();
                            proctor.setId(rs.getString("id"));
                            proctor.setEmail(rs.getString("email"));
                            proctor.setFullName(rs.getString("full_name"));
                            proctor.setBlockId(rs.getString("block_id"));
                            proctor.setStatus(rs.getString("status"));
                            return proctor;
                            
                        case "Student Service":
                            StudentService ss = new StudentService();
                            ss.setId(rs.getString("id"));
                            ss.setEmail(rs.getString("email"));
                            ss.setFullName(rs.getString("full_name"));
                            return ss;
                    }
                }
            }
        }
        return null;
    }
    
    private void redirectToHome(User user) {
        try {
            String fxmlFile = "";
            
            switch (user.getUserType()) {
                case "STUDENT":
                    fxmlFile = "/com/dormitory/controllers/StudentHome.fxml";
                    break;
                case "PROCTOR":
                    fxmlFile = "/com/dormitory/controllers/ProctorHome.fxml";
                    break;
                case "STUDENT_SERVICE":
                    fxmlFile = "/com/dormitory/controllers/StudentServiceHome.fxml";
                    break;
            }
            
            Stage currentStage = (Stage) loginEmail.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            CommonController controller = loader.getController();
            controller.setCurrentUser(user);
            
            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            
            root.setOnMouseDragged(event -> {
                newStage.setX(event.getScreenX() - xOffset);
                newStage.setY(event.getScreenY() - yOffset);
            });
            
            newStage.setScene(scene);
            newStage.initStyle(StageStyle.UNDECORATED);
            
            try {
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dormitory/images/Picture-1.jpg")));
            } catch (Exception e) {
                System.out.println("Icon not found.");
            }
            
            newStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading home page: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToStudentSignUp() {
        openSignUpWindow("SignUpStudent.fxml", "Student Registration", signUpStudentButton);
    }

    @FXML
    private void goToProctorSignUp() {
        openSignUpWindow("SignUpProctor.fxml", "Proctor Registration", signUpProctorButton);
    }

    private void openSignUpWindow(String fxmlFile, String title, Button sourceButton) {
        try {
            Stage currentStage = (Stage) sourceButton.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dormitory/controllers/" + fxmlFile));
            Parent root = loader.load();
            
            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            newStage.setScene(scene);
            newStage.setTitle(title);
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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