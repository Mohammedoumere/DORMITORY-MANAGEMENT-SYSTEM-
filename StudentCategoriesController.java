package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentCategoriesController implements CommonController {

    private Student currentUser;

    @FXML private Label blockLabel;
    @FXML private Label roomLabel;
    @FXML private Label proctorNameLabel;
    @FXML private Label proctorPhoneLabel;
    @FXML private Label proctorEmailLabel;

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Student) {
            this.currentUser = (Student) user;
            loadCategoryDetails();
        }
    }

    private void loadCategoryDetails() {
        if (currentUser == null || currentUser.getBlockId() == null) {
            blockLabel.setText("Not Assigned");
            roomLabel.setText("Not Assigned");
            proctorNameLabel.setText("N/A");
            proctorPhoneLabel.setText("N/A");
            proctorEmailLabel.setText("N/A");
            return;
        }

        blockLabel.setText(currentUser.getBlockId());
        roomLabel.setText(currentUser.getRoomNumber() != null ? currentUser.getRoomNumber() : "Not Assigned");

        // Fetch proctor details
        String query = "SELECT full_name, phone_number, email FROM proctor WHERE block_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getBlockId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                proctorNameLabel.setText(rs.getString("full_name"));
                proctorPhoneLabel.setText(rs.getString("phone_number"));
                proctorEmailLabel.setText(rs.getString("email"));
            } else {
                proctorNameLabel.setText("Not Assigned");
                proctorPhoneLabel.setText("N/A");
                proctorEmailLabel.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            proctorNameLabel.setText("Error");
            proctorPhoneLabel.setText("Error");
            proctorEmailLabel.setText("Error");
        }
    }
}
