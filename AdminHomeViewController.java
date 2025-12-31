package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminHomeViewController implements CommonController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalStudentsLabel, maleStudentsLabel, femaleStudentsLabel;
    @FXML private Label totalProctorsLabel, maleProctorsLabel, femaleProctorsLabel;
    @FXML private Label totalBlocksLabel, maleBlocksLabel, femaleBlocksLabel;
    @FXML private Label totalReportsLabel;

    @Override
    public void setCurrentUser(User user) {
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
        }
        loadAdminDashboard();
    }

    private void loadAdminDashboard() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            totalStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student")));
            maleStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE gender = 'Male'")));
            femaleStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE gender = 'Female'")));
            totalProctorsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM proctor")));
            maleProctorsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM proctor WHERE gender = 'Male'")));
            femaleProctorsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM proctor WHERE gender = 'Female'")));
            totalBlocksLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM blocks")));
            maleBlocksLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM blocks WHERE gender = 'Male'")));
            femaleBlocksLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM blocks WHERE gender = 'Female'")));
            totalReportsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM reports")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCount(Connection conn, String query) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
