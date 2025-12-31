package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProctorHomeViewController implements CommonController {

    private Proctor currentUser;

    @FXML private Label welcomeLabel;
    @FXML private Label proctorBlockInfoLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalReportsLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label assignedStudentsLabel;
    @FXML private Label availableRoomsLabel;

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Proctor) {
            this.currentUser = (Proctor) user;
            loadProctorDashboard();
        }
    }

    private void loadProctorDashboard() {
        if (currentUser == null) return;

        welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");
        String blockId = currentUser.getBlockId();
        proctorBlockInfoLabel.setText("You are managing Block: " + blockId);

        try (Connection conn = DatabaseConnection.getConnection()) {
            totalStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE block_id = ?", blockId)));
            totalReportsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM reports WHERE submitted_to_id = ?", currentUser.getId())));
            totalRoomsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rooms WHERE block_id = ?", blockId)));
            assignedStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE block_id = ? AND room_number IS NOT NULL", blockId)));
            availableRoomsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(room_id) FROM rooms WHERE block_id = ? AND current_occupancy < capacity", blockId)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCount(Connection conn, String query, String... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
