package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.utils.PasswordUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProctorManagementController {

    @FXML private TextField searchProctorField;
    @FXML private TableView<Proctor> proctorTable;
    @FXML private TableColumn<Proctor, Integer> colProctorIndex;
    @FXML private TableColumn<Proctor, String> colProctorId, colProctorName, colProctorBlock, colProctorPhone, colProctorGender;
    @FXML private TableColumn<Proctor, Void> colProctorActions;

    private final ObservableList<Proctor> proctorList = FXCollections.observableArrayList();
    private final List<String> maleBlocks = Arrays.asList("B01", "B02", "B03", "B04", "B05");
    private final List<String> femaleBlocks = Arrays.asList("B06", "B07", "B08", "B09", "B10");

    @FXML
    private void initialize() {
        setupProctorTable();
        searchProctorField.textProperty().addListener((obs, oldVal, newVal) -> searchProctors());
        loadProctors();
    }

    private void setupProctorTable() {
        colProctorIndex.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(proctorTable.getItems().indexOf(column.getValue()) + 1));
        colProctorIndex.setSortable(false);

        colProctorId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProctorName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProctorGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colProctorBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colProctorPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        colProctorActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, updateButton, deleteBtn);
            {
                updateButton.setOnAction(event -> showUpdateProctorDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> deleteProctor(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadProctors() {
        proctorList.clear();
        String query = "SELECT proctor_id, full_name, gender, block_id, phone_number, email FROM proctor";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Proctor p = new Proctor();
                p.setId(rs.getString("proctor_id"));
                p.setFullName(rs.getString("full_name"));
                p.setGender(rs.getString("gender"));
                p.setBlockId(rs.getString("block_id"));
                p.setPhoneNumber(rs.getString("phone_number"));
                p.setEmail(rs.getString("email"));
                proctorList.add(p);
            }
            proctorTable.setItems(proctorList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load proctors.");
        }
    }
    
    @FXML
    private void showAddProctorDialog() {
        Dialog<Proctor> dialog = new Dialog<>();
        dialog.setTitle("Add New Proctor");
        dialog.setHeaderText("Enter proctor details");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Proctor ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        TextField emailField = new TextField();
        emailField.setPromptText("Email (@gmail.com)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Gender");
        
        ComboBox<String> blockCombo = new ComboBox<>();
        blockCombo.setPromptText("Select Block");

        // Dependent Block ComboBox
        genderCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            blockCombo.getItems().clear();
            if ("Male".equals(newVal)) {
                blockCombo.getItems().setAll(maleBlocks);
            } else if ("Female".equals(newVal)) {
                blockCombo.getItems().setAll(femaleBlocks);
            }
        });

        grid.add(new Label("Proctor ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderCombo, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Password:"), 0, 5);
        grid.add(passwordField, 1, 5);
        grid.add(new Label("Block:"), 0, 6);
        grid.add(blockCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() || 
                    phoneField.getText().isEmpty() || emailField.getText().isEmpty() || 
                    passwordField.getText().isEmpty() || genderCombo.getValue() == null || 
                    blockCombo.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all fields.");
                    return null;
                }
                
                if (!emailField.getText().toLowerCase().endsWith("@gmail.com")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Email", "Email must end with @gmail.com");
                    return null;
                }

                Proctor p = new Proctor();
                p.setId(idField.getText());
                p.setFullName(nameField.getText());
                p.setPhoneNumber(phoneField.getText());
                p.setGender(genderCombo.getValue());
                p.setEmail(emailField.getText());
                p.setPassword(passwordField.getText());
                p.setBlockId(blockCombo.getValue());
                return p;
            }
            return null;
        });

        Optional<Proctor> result = dialog.showAndWait();
        result.ifPresent(this::registerProctorInDatabase);
    }

    private void registerProctorInDatabase(Proctor proctor) {
        String countQuery = "SELECT COUNT(*) FROM proctor WHERE block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
            
            countStmt.setString(1, proctor.getBlockId());
            ResultSet rs = countStmt.executeQuery();
            if (rs.next() && rs.getInt(1) >= 2) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Block " + proctor.getBlockId() + " already has 2 proctors.");
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(proctor.getPassword());
            String insertQuery = "INSERT INTO proctor (proctor_id, full_name, email, password, phone_number, block_id, gender, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, proctor.getId());
                insertStmt.setString(2, proctor.getFullName());
                insertStmt.setString(3, proctor.getEmail());
                insertStmt.setString(4, hashedPassword);
                insertStmt.setString(5, proctor.getPhoneNumber());
                insertStmt.setString(6, proctor.getBlockId());
                insertStmt.setString(7, proctor.getGender());
                
                insertStmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Proctor registered successfully.");
                loadProctors();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register proctor. ID or Email may already exist.");
        }
    }

    @FXML
    private void searchProctors() {
        String searchText = searchProctorField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            proctorTable.setItems(proctorList);
            return;
        }
        ObservableList<Proctor> filteredList = FXCollections.observableArrayList();
        for (Proctor proctor : proctorList) {
            if (proctor.getFullName().toLowerCase().contains(searchText) || 
                proctor.getId().toLowerCase().contains(searchText) ||
                proctor.getBlockId().toLowerCase().contains(searchText)) {
                filteredList.add(proctor);
            }
        }
        proctorTable.setItems(filteredList);
    }

    private void showUpdateProctorDialog(Proctor proctor) {
        Dialog<Proctor> dialog = new Dialog<>();
        dialog.setTitle("Update Proctor");
        dialog.setHeaderText("Editing details for " + proctor.getFullName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(proctor.getFullName());
        TextField emailField = new TextField(proctor.getEmail());
        TextField phoneField = new TextField(proctor.getPhoneNumber());
        
        ComboBox<String> blockCombo = new ComboBox<>();
        if (proctor.getGender() != null) {
            if ("Male".equals(proctor.getGender())) {
                blockCombo.getItems().setAll(maleBlocks);
            } else {
                blockCombo.getItems().setAll(femaleBlocks);
            }
        }
        blockCombo.setValue(proctor.getBlockId());
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (Optional)");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Block:"), 0, 3);
        grid.add(blockCombo, 1, 3);
        grid.add(new Label("New Password:"), 0, 4);
        grid.add(passwordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                String email = emailField.getText().trim();
                if (!email.toLowerCase().endsWith("@gmail.com")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Email", "Email address must end with @gmail.com");
                    return null;
                }
                proctor.setFullName(nameField.getText());
                proctor.setEmail(email);
                proctor.setPhoneNumber(phoneField.getText());
                proctor.setBlockId(blockCombo.getValue());
                if (!passwordField.getText().isEmpty()) {
                    proctor.setPassword(passwordField.getText());
                } else {
                    proctor.setPassword(null);
                }
                return proctor;
            }
            return null;
        });

        Optional<Proctor> result = dialog.showAndWait();
        result.ifPresent(this::updateProctorInDatabase);
    }

    private void updateProctorInDatabase(Proctor proctor) {
        boolean updatePassword = proctor.getPassword() != null && !proctor.getPassword().isEmpty();
        
        String query;
        if (updatePassword) {
            query = "UPDATE proctor SET full_name = ?, email = ?, phone_number = ?, block_id = ?, password = ? WHERE proctor_id = ?";
        } else {
            query = "UPDATE proctor SET full_name = ?, email = ?, phone_number = ?, block_id = ? WHERE proctor_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            int paramIndex = 1;
            pstmt.setString(paramIndex++, proctor.getFullName());
            pstmt.setString(paramIndex++, proctor.getEmail());
            pstmt.setString(paramIndex++, proctor.getPhoneNumber());
            pstmt.setString(paramIndex++, proctor.getBlockId());
            
            if (updatePassword) {
                pstmt.setString(paramIndex++, PasswordUtils.hashPassword(proctor.getPassword()));
            }
            pstmt.setString(paramIndex, proctor.getId());
            
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Proctor details updated successfully.");
            loadProctors();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update proctor.");
        }
    }

    private void deleteProctor(Proctor proctor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Proctor: " + proctor.getFullName());
        alert.setContentText("Are you sure you want to delete this proctor?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM proctor WHERE proctor_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, proctor.getId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Proctor deleted successfully.");
                loadProctors();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete proctor.");
            }
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
