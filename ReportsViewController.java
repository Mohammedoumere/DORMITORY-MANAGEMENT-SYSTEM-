package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReportsViewController implements CommonController {

    @FXML private TableView<Student> reportTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colBlock;
    @FXML private TableColumn<Student, String> colStatus;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        // Not needed for this controller, but required by the interface
    }

    @FXML
    private void initialize() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        loadReportData();
    }

    private void loadReportData() {
        studentList.clear();
        String query = "SELECT student_id, full_name, block_id, status FROM student WHERE status = 'ASSIGNED' AND room_number IS NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("student_id"));
                student.setFullName(rs.getString("full_name"));
                student.setBlockId(rs.getString("block_id"));
                student.setStatus(rs.getString("status"));
                studentList.add(student);
            }
            reportTable.setItems(studentList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load report data.");
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