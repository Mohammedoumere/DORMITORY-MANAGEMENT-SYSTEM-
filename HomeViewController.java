package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeViewController implements CommonController {

    private User currentUser;

    @FXML private Label welcomeLabel, proctorBlockInfoLabel;
    @FXML private Label totalStudentsLabel, maleStudentsLabel, femaleStudentsLabel, totalReportsLabel;
    @FXML private Label totalStudentsDescLabel, totalReportsDescLabel;
    @FXML private Label totalProctorsLabel, maleProctorsLabel, femaleProctorsLabel;
    @FXML private Label totalBlocksLabel, maleBlocksLabel, femaleBlocksLabel;
    @FXML private Label totalRoomsLabel, assignedStudentsLabel, availableRoomsLabel;
    
    // Cards
    @FXML private VBox studentStatCard, maleStudentCard, femaleStudentCard, reportStatCard;
    @FXML private VBox proctorStatCard, maleProctorCard, femaleProctorCard;
    @FXML private VBox blockStatCard1, blockStatCard2, blockStatCard3;
    @FXML private VBox roomStatCard1, roomStatCard2, roomStatCard3;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadDashboardData();
    }

    @FXML
    private void initialize() {
        // Data is loaded via setCurrentUser
    }

    private void loadDashboardData() {
        if (currentUser == null) return;

        welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");

        if ("STUDENT_SERVICE".equals(currentUser.getUserType())) {
            loadAdminDashboard();
        } else if (currentUser instanceof Proctor) {
            loadProctorDashboard((Proctor) currentUser);
        }
    }

    private void loadAdminDashboard() {
        setCardVisibility(studentStatCard, true);
        setCardVisibility(maleStudentCard, true);
        setCardVisibility(femaleStudentCard, true);
        setCardVisibility(reportStatCard, true);
        setCardVisibility(proctorStatCard, true);
        setCardVisibility(maleProctorCard, true);
        setCardVisibility(femaleProctorCard, true);
        setCardVisibility(blockStatCard1, true);
        setCardVisibility(blockStatCard2, true);
        setCardVisibility(blockStatCard3, true);
        setCardVisibility(roomStatCard1, false);
        setCardVisibility(roomStatCard2, false);
        setCardVisibility(roomStatCard3, false);
        proctorBlockInfoLabel.setVisible(false);

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

    private void loadProctorDashboard(Proctor proctor) {
        String blockId = proctor.getBlockId();
        
        proctorBlockInfoLabel.setText("You are managing Block: " + blockId);
        proctorBlockInfoLabel.setVisible(true);
        totalStudentsDescLabel.setText("Students in Your Block");
        totalReportsDescLabel.setText("Reports to You");

        setCardVisibility(maleStudentCard, false);
        setCardVisibility(femaleStudentCard, false);
        setCardVisibility(proctorStatCard, false);
        setCardVisibility(maleProctorCard, false);
        setCardVisibility(femaleProctorCard, false);
        setCardVisibility(blockStatCard1, false);
        setCardVisibility(blockStatCard2, false);
        setCardVisibility(blockStatCard3, false);
        
        setCardVisibility(studentStatCard, true);
        setCardVisibility(reportStatCard, true);
        setCardVisibility(roomStatCard1, true);
        setCardVisibility(roomStatCard2, true);
        setCardVisibility(roomStatCard3, true);

        try (Connection conn = DatabaseConnection.getConnection()) {
            totalStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE block_id = ?", blockId)));
            totalReportsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM reports WHERE submitted_to_id = ?", proctor.getId())));
            totalRoomsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rooms WHERE block_id = ?", blockId)));
            assignedStudentsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM student WHERE block_id = ? AND room_number IS NOT NULL", blockId)));
            availableRoomsLabel.setText(String.valueOf(getCount(conn, "SELECT COUNT(room_id) FROM rooms WHERE block_id = ? AND current_occupancy < capacity", blockId)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setCardVisibility(VBox card, boolean isVisible) {
        card.setVisible(isVisible);
        card.setManaged(isVisible);
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
