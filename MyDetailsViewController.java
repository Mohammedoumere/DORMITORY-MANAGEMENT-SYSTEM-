package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyDetailsViewController implements CommonController {

    private Student currentUser;

    // Read-Only Fields
    @FXML private TextField studentIdField, fullNameField, emailField, blockField, roomField;

    // Editable Fields
    @FXML private TextField phoneField, ageField, cityField, residentNameField, residentPhoneField;
    @FXML private ComboBox<String> genderCombo, collegeCombo, departmentCombo, nationalityCombo, regionCombo, residentRelationCombo;
    @FXML private ComboBox<Integer> yearCombo;

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Student) {
            this.currentUser = (Student) user;
            loadStudentDetails();
        }
    }

    @FXML
    private void initialize() {
        // Populate ComboBoxes
        genderCombo.getItems().addAll("Male", "Female");
        yearCombo.getItems().addAll(1, 2, 3, 4, 5);
        collegeCombo.getItems().addAll("Engineering", "Science", "Business", "Arts", "Medicine");
        departmentCombo.getItems().addAll("Computer Science", "Electrical", "Mechanical", "Civil", "Chemistry");
        nationalityCombo.getItems().addAll("Ethiopian", "Other");
        regionCombo.getItems().addAll("Addis Ababa", "Oromia", "Amhara", "Tigray", "SNNPR", "Other");
        residentRelationCombo.getItems().addAll("Father", "Mother", "Guardian", "Sibling", "Other");
    }

    private void loadStudentDetails() {
        if (currentUser == null) return;

        String query = "SELECT * FROM student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Set read-only fields
                studentIdField.setText(rs.getString("student_id"));
                fullNameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                blockField.setText(rs.getString("block_id"));
                roomField.setText(rs.getString("room_number"));

                // Set editable fields
                phoneField.setText(rs.getString("phone_number"));
                ageField.setText(String.valueOf(rs.getInt("age")));
                genderCombo.setValue(rs.getString("gender"));
                yearCombo.setValue(rs.getInt("year_of_study"));
                collegeCombo.setValue(rs.getString("college"));
                departmentCombo.setValue(rs.getString("department"));
                nationalityCombo.setValue(rs.getString("nationality"));
                regionCombo.setValue(rs.getString("region"));
                cityField.setText(rs.getString("city"));
                residentNameField.setText(rs.getString("resident_full_name"));
                residentRelationCombo.setValue(rs.getString("resident_relation"));
                residentPhoneField.setText(rs.getString("resident_phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load your details.");
        }
    }

    @FXML
    private void updateDetails() {
        // Validation (simplified for brevity)
        if (phoneField.getText().isEmpty() || ageField.getText().isEmpty() || cityField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill all required fields.");
            return;
        }

        String query = "UPDATE student SET phone_number = ?, age = ?, gender = ?, year_of_study = ?, college = ?, " +
                       "department = ?, nationality = ?, region = ?, city = ?, resident_full_name = ?, " +
                       "resident_relation = ?, resident_phone = ?, status = 'ACTIVE' WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, phoneField.getText());
            pstmt.setInt(2, Integer.parseInt(ageField.getText()));
            pstmt.setString(3, genderCombo.getValue());
            pstmt.setInt(4, yearCombo.getValue());
            pstmt.setString(5, collegeCombo.getValue());
            pstmt.setString(6, departmentCombo.getValue());
            pstmt.setString(7, nationalityCombo.getValue());
            pstmt.setString(8, regionCombo.getValue());
            pstmt.setString(9, cityField.getText());
            pstmt.setString(10, residentNameField.getText());
            pstmt.setString(11, residentRelationCombo.getValue());
            pstmt.setString(12, residentPhoneField.getText());
            pstmt.setString(13, currentUser.getId());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your details have been updated successfully.");

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update your details. Please check your input.");
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