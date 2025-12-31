package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.AuditLog;
import com.dormitory.models.User;
import com.dormitory.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SettingsViewController implements CommonController {

    private User currentUser;

    @FXML private VBox navMenu;
    @FXML private Button backupButton;
    @FXML private Button privacyButton;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button logoutButton;

    @FXML private VBox changePasswordView;
    @FXML private VBox privacyView;
    @FXML private VBox backupView;
    @FXML private VBox aboutView;

    @FXML private TableView<AuditLog> auditLogTable;
    @FXML private TableColumn<AuditLog, String> colAction, colDescription, colIpAddress;
    @FXML private TableColumn<AuditLog, LocalDateTime> colTimestamp;
    private final ObservableList<AuditLog> auditLogList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            if (!"STUDENT_SERVICE".equals(currentUser.getUserType())) {
                navMenu.getChildren().remove(backupButton);
            }
            if ("STUDENT".equals(currentUser.getUserType())) {
                navMenu.getChildren().remove(privacyButton);
            }
        }
    }

    @FXML
    private void initialize() {
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colIpAddress.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        showChangePasswordView();
    }

    private void showView(Node viewToShow) {
        changePasswordView.setVisible(false);
        privacyView.setVisible(false);
        backupView.setVisible(false);
        aboutView.setVisible(false);
        viewToShow.setVisible(true);
    }

    @FXML private void showChangePasswordView() { showView(changePasswordView); }
    @FXML private void showPrivacyView() { showView(privacyView); loadAuditLog(); }
    @FXML private void showBackupView() { showView(backupView); }
    @FXML private void showAboutView() { showView(aboutView); }
    
    private void loadAuditLog() {
        auditLogList.clear();
        String query = "SELECT action, description, ip_address, created_at FROM audit_log WHERE user_id = ? AND user_type = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            pstmt.setString(2, currentUser.getUserType());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setAction(rs.getString("action"));
                log.setDescription(rs.getString("description"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                auditLogList.add(log);
            }
            auditLogTable.setItems(auditLogList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database Backup");
        fileChooser.setInitialFileName("dormitory_db_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
        File file = fileChooser.showSaveDialog(backupView.getScene().getWindow());

        if (file != null) {
            try {
                String pgDumpPath = "\"C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe\"";
                ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath, "-U", "postgres", "-h", "localhost", "-d", "dormitory_db", "-f", file.getAbsolutePath()
                );
                pb.environment().put("PGPASSWORD", "your_postgres_password"); // Replace with actual password or handle securely
                
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Database backup created successfully at:\n" + file.getAbsolutePath());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Backup Failed", "An error occurred during backup. Check console for details.");
                }
            } catch (IOException | InterruptedException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not execute backup command.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRestore() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Restore");
        confirmation.setHeaderText("WARNING: This will overwrite the current database.");
        confirmation.setContentText("Are you sure you want to restore the database from a backup file? All current data will be lost.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Backup File to Restore");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
            File file = fileChooser.showOpenDialog(backupView.getScene().getWindow());

            if (file != null) {
                try {
                    String psqlPath = "\"C:\\Program Files\\PostgreSQL\\18\\bin\\psql.exe\"";
                    ProcessBuilder pb = new ProcessBuilder(
                        psqlPath, "-U", "postgres", "-h", "localhost", "-d", "dormitory_db", "-f", file.getAbsolutePath()
                    );
                    pb.environment().put("PGPASSWORD", "your_postgres_password");

                    Process process = pb.start();
                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Database restored successfully. Please restart the application.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Restore Failed", "An error occurred during restore. Check console for details.");
                    }
                } catch (IOException | InterruptedException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not execute restore command.");
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void changePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all password fields.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "New passwords do not match.");
            return;
        }

        String[] tableInfo = getTableAndIdColumn();
        if (tableInfo == null) return;

        String query = "SELECT password FROM " + tableInfo[0] + " WHERE " + tableInfo[1] + " = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if ("student_service".equals(tableInfo[0])) {
                pstmt.setInt(1, Integer.parseInt(currentUser.getId()));
            } else {
                pstmt.setString(1, currentUser.getId());
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (PasswordUtils.verifyPassword(currentPassword, rs.getString("password"))) {
                    String newHash = PasswordUtils.hashPassword(newPassword);
                    String updateQuery = "UPDATE " + tableInfo[0] + " SET password = ? WHERE " + tableInfo[1] + " = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, newHash);
                        
                        if ("student_service".equals(tableInfo[0])) {
                            updatePstmt.setInt(2, Integer.parseInt(currentUser.getId()));
                        } else {
                            updatePstmt.setString(2, currentUser.getId());
                        }

                        updatePstmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
                        currentPasswordField.clear();
                        newPasswordField.clear();
                        confirmPasswordField.clear();
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Authentication Error", "Current password is incorrect.");
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to change password.");
        }
    }

    private String[] getTableAndIdColumn() {
        if (currentUser == null || currentUser.getUserType() == null) {
            return null;
        }
        switch (currentUser.getUserType()) {
            case "STUDENT":
                return new String[]{"student", "student_id"};
            case "PROCTOR":
                return new String[]{"proctor", "proctor_id"};
            case "STUDENT_SERVICE":
                return new String[]{"student_service", "service_id"};
            default:
                return null;
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
        
        Parent root = FXMLLoader.load(getClass().getResource("/com/dormitory/controllers/Login.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
