package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Report;
import com.dormitory.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.util.Optional;

public class StudentServiceReportsController implements CommonController {

    private User currentUser;

    @FXML private TableView<Report> proctorReportsTable;
    @FXML private TableColumn<Report, String> colProctorName, colProctorBlock, colProctorReportTitle, colProctorReportDesc, colProctorReportStatus;
    @FXML private TableColumn<Report, Void> colProctorReportActions;

    private final ObservableList<Report> proctorReportList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadProctorReports();
    }

    @FXML
    private void initialize() {
        colProctorName.setCellValueFactory(new PropertyValueFactory<>("submittedByName"));
        colProctorBlock.setCellValueFactory(new PropertyValueFactory<>("submittedByBlockId"));
        colProctorReportTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colProctorReportDesc.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        colProctorReportStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colProctorReportActions.setCellFactory(param -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button replyBtn = new Button("Reply");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, acceptBtn, replyBtn, deleteBtn);
            {
                deleteBtn.getStyleClass().add("danger-button");
                acceptBtn.setOnAction(event -> acceptReport(getTableView().getItems().get(getIndex())));
                replyBtn.setOnAction(event -> showReplyDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> confirmDeleteReport(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadProctorReports() {
        proctorReportList.clear();
        String query = "SELECT r.report_id, p.full_name, p.block_id, r.title, r.problem_description, r.status " +
                       "FROM reports r JOIN proctor p ON r.submitted_by_id = p.proctor_id " +
                       "WHERE r.submitted_by_role = 'PROCTOR' ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getInt("report_id"));
                report.setSubmittedByName(rs.getString("full_name"));
                report.setSubmittedByBlockId(rs.getString("block_id"));
                report.setTitle(rs.getString("title"));
                report.setProblemDescription(rs.getString("problem_description"));
                report.setStatus(rs.getString("status"));
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
            loadProctorReports();
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

    private void confirmDeleteReport(Report report) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Report: " + report.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this report? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteReportFromDatabase(report.getReportId());
        }
    }

    private void deleteReportFromDatabase(int reportId) {
        String query = "DELETE FROM reports WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reportId);
            pstmt.executeUpdate();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Report deleted successfully.");
            loadProctorReports();
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
