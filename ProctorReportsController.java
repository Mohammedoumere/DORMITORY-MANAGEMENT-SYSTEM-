package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.models.Report;
import com.dormitory.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class ProctorReportsController implements CommonController {

    private Proctor currentUser;

    // Student Reports Tab
    @FXML private TableView<Report> studentReportsTable;
    @FXML private TableColumn<Report, String> colStudentName, colStudentReportTitle, colStudentReportDesc, colStudentReportStatus;
    @FXML private TableColumn<Report, Void> colStudentReportActions;

    // Proctor Reports Tab
    @FXML private TextField proctorTitleField;
    @FXML private TextArea proctorDescriptionArea;
    @FXML private TableView<Report> proctorReportsTable;
    @FXML private TableColumn<Report, String> colProctorReportTitle, colProctorReportStatus, colProctorReportReply;
    @FXML private TableColumn<Report, LocalDateTime> colProctorReportDate;
    @FXML private TableColumn<Report, Void> colProctorReportActions;

    private final ObservableList<Report> studentReportList = FXCollections.observableArrayList();
    private final ObservableList<Report> proctorReportList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Proctor) {
            this.currentUser = (Proctor) user;
            loadStudentReports();
            loadProctorReports();
        }
    }

    @FXML
    private void initialize() {
        // Setup for Student Reports Table
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("submittedByName"));
        colStudentReportTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStudentReportDesc.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        colStudentReportStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStudentReportActions.setCellFactory(param -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button replyBtn = new Button("Reply");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, acceptBtn, replyBtn, deleteBtn);
            {
                deleteBtn.getStyleClass().add("danger-button");
                acceptBtn.setOnAction(event -> acceptReport(getTableView().getItems().get(getIndex())));
                replyBtn.setOnAction(event -> showReplyDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> confirmDeleteReport(getTableView().getItems().get(getIndex()), true));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Setup for Proctor Reports Table
        colProctorReportTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colProctorReportStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProctorReportReply.setCellValueFactory(new PropertyValueFactory<>("reply"));
        colProctorReportDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colProctorReportActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setOnAction(event -> confirmDeleteReport(getTableView().getItems().get(getIndex()), false));
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

    private void loadStudentReports() {
        studentReportList.clear();
        String query = "SELECT r.report_id, s.full_name, r.title, r.problem_description, r.status " +
                       "FROM reports r JOIN student s ON r.submitted_by_id = s.student_id " +
                       "WHERE r.submitted_to_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getInt("report_id"));
                report.setSubmittedByName(rs.getString("full_name"));
                report.setTitle(rs.getString("title"));
                report.setProblemDescription(rs.getString("problem_description"));
                report.setStatus(rs.getString("status"));
                studentReportList.add(report);
            }
            studentReportsTable.setItems(studentReportList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProctorReports() {
        proctorReportList.clear();
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
                proctorReportList.add(report);
            }
            proctorReportsTable.setItems(proctorReportList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void acceptReport(Report report) {
        updateReportStatus(report.getReportId(), "ACCEPTED");
    }

    private void showReplyDialog(Report report) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply to Report");
        dialog.setHeaderText("Replying to: " + report.getTitle());
        dialog.setContentText("Your Reply:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reply -> {
            updateReportReply(report.getReportId(), reply);
            updateReportStatus(report.getReportId(), "RESOLVED");
        });
    }

    private void updateReportStatus(int reportId, String status) {
        String query = "UPDATE reports SET status = ? WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
            loadStudentReports();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateReportReply(int reportId, String reply) {
        String query = "UPDATE reports SET reply = ? WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, reply);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void submitProctorReport() {
        String title = proctorTitleField.getText();
        String description = proctorDescriptionArea.getText();

        if (title.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please provide a title and description.");
            return;
        }

        String query = "INSERT INTO reports (submitted_by_id, submitted_by_role, submitted_to_id, title, problem_description, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            pstmt.setString(2, "PROCTOR");
            pstmt.setString(3, "1"); // Assuming Student Service Admin is always ID 1
            pstmt.setString(4, title);
            pstmt.setString(5, description);
            pstmt.setString(6, "PENDING");
            
            pstmt.executeUpdate();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Report submitted to Student Service.");
            loadProctorReports();
            proctorTitleField.clear();
            proctorDescriptionArea.clear();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit report.");
        }
    }
    
    private void confirmDeleteReport(Report report, boolean isStudentReport) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Report: " + report.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this report?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteReportFromDatabase(report.getReportId(), isStudentReport);
        }
    }

    private void deleteReportFromDatabase(int reportId, boolean isStudentReport) {
        String query = "DELETE FROM reports WHERE report_id = ?";
        if (!isStudentReport) {
            query += " AND status = 'PENDING'";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reportId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report deleted successfully.");
                if (isStudentReport) {
                    loadStudentReports();
                } else {
                    loadProctorReports();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete report. It may have already been accepted or you lack permission.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete report.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
