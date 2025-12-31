package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Report;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class StudentReportController implements CommonController {

    private Student currentUser;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, String> colTitle, colStatus, colReply;
    @FXML private TableColumn<Report, LocalDateTime> colDate;
    @FXML private TableColumn<Report, Void> colActions;

    private final ObservableList<Report> reportList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Student) {
            this.currentUser = (Student) user;
            loadReports();
        }
    }

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReply.setCellValueFactory(new PropertyValueFactory<>("reply"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    confirmDeleteReport(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Report report = getTableView().getItems().get(getIndex());
                    if ("PENDING".equalsIgnoreCase(report.getStatus())) {
                        setGraphic(deleteBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadReports() {
        reportList.clear();
        String query = "SELECT report_id, title, status, reply, created_at FROM reports WHERE submitted_by_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getInt("report_id"));
                report.setTitle(rs.getString("title"));
                report.setStatus(rs.getString("status"));
                report.setReply(rs.getString("reply"));
                report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                reportList.add(report);
            }
            reportsTable.setItems(reportList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load your reports.");
        }
    }

    @FXML
    private void submitReport() {
        String title = titleField.getText();
        String description = descriptionArea.getText();

        if (title.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please provide both a title and a description for your report.");
            return;
        }

        String proctorId = findProctorIdForBlock(currentUser.getBlockId());
        if (proctorId == null) {
            showAlert(Alert.AlertType.ERROR, "System Error", "Could not find a proctor assigned to your block. Please contact Student Service.");
            return;
        }

        String query = "INSERT INTO reports (submitted_by_id, submitted_by_role, submitted_to_id, title, problem_description, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            pstmt.setString(2, "STUDENT");
            pstmt.setString(3, proctorId);
            pstmt.setString(4, title);
            pstmt.setString(5, description);
            pstmt.setString(6, "PENDING");
            
            pstmt.executeUpdate();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your report has been submitted to your proctor.");
            loadReports();
            titleField.clear();
            descriptionArea.clear();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit your report.");
        }
    }

    private void confirmDeleteReport(Report report) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Report: " + report.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this report?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteReportFromDatabase(report.getReportId());
        }
    }

    private void deleteReportFromDatabase(int reportId) {
        String query = "DELETE FROM reports WHERE report_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reportId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report deleted successfully.");
                loadReports();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete report. It may have already been accepted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete report.");
        }
    }

    private String findProctorIdForBlock(String blockId) {
        if (blockId == null) return null;
        String query = "SELECT proctor_id FROM proctor WHERE block_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, blockId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("proctor_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
