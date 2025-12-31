package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import com.dormitory.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CategoriesViewController implements CommonController {

    private User currentUser;

    // Student Management
    @FXML private TextField studentIdField, studentNameField, collegeField, studentEmailField, searchStudentField;
    @FXML private PasswordField studentPasswordField;
    @FXML private ComboBox<String> blockCombo, studentGenderCombo;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId, colStudentName, colStudentCollege, colStudentBlock, colStudentStatus, colStudentGender;
    @FXML private TableColumn<Student, Void> colStudentActions;

    // Proctor Management
    @FXML private TextField proctorIdField, proctorNameField, proctorPhoneField, proctorEmailField, searchProctorField;
    @FXML private PasswordField proctorPasswordField;
    @FXML private ComboBox<String> proctorBlockCombo, proctorGenderCombo;
    @FXML private TableView<Proctor> proctorTable;
    @FXML private TableColumn<Proctor, String> colProctorId, colProctorName, colProctorBlock, colProctorPhone, colProctorGender;
    @FXML private TableColumn<Proctor, Void> colProctorActions;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final ObservableList<Proctor> proctorList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        setupStudentTable();
        setupProctorTable();
        
        studentGenderCombo.getItems().addAll("Male", "Female");
        proctorGenderCombo.getItems().addAll("Male", "Female");
        blockCombo.getItems().addAll("B01", "B02", "B03", "B04", "B05", "B06", "B07", "B08", "B09", "B10");
        proctorBlockCombo.getItems().addAll("B01", "B02", "B03", "B04", "B05", "B06", "B07", "B08", "B09", "B10");

        searchStudentField.textProperty().addListener((obs, oldVal, newVal) -> searchStudents());
        searchProctorField.textProperty().addListener((obs, oldVal, newVal) -> searchProctors());

        loadStudents();
        loadProctors();
    }

    // --- Student Management ---
    private void setupStudentTable() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStudentGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colStudentCollege.setCellValueFactory(new PropertyValueFactory<>("college"));
        colStudentBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colStudentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colStudentActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> deleteStudent(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void loadStudents() {
        studentList.clear();
        String query = "SELECT student_id, full_name, gender, college, block_id, status FROM student";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getString("student_id"));
                s.setFullName(rs.getString("full_name"));
                s.setGender(rs.getString("gender"));
                s.setCollege(rs.getString("college"));
                s.setBlockId(rs.getString("block_id"));
                s.setStatus(rs.getString("status"));
                studentList.add(s);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students.");
        }
    }

    @FXML
    private void registerStudent() {
        // ... (Implementation from previous steps)
    }

    @FXML
    private void searchStudents() {
        // ... (Implementation from previous steps)
    }

    private void deleteStudent(Student student) {
        // ... (Implementation from previous steps)
    }
    
    private void clearStudentFields() {
        // ... (Implementation from previous steps)
    }

    // --- Proctor Management ---
    private void setupProctorTable() {
        colProctorId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProctorName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProctorGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colProctorBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colProctorPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        colProctorActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(event -> deleteProctor(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    private void loadProctors() {
        proctorList.clear();
        String query = "SELECT proctor_id, full_name, gender, block_id, phone_number FROM proctor";
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
                proctorList.add(p);
            }
            proctorTable.setItems(proctorList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load proctors.");
        }
    }
    
    @FXML
    private void registerProctor() {
        // ... (Implementation from previous steps)
    }

    @FXML
    private void searchProctors() {
        // ... (Implementation from previous steps)
    }

    private void deleteProctor(Proctor proctor) {
        // ... (Implementation from previous steps)
    }
    
    private void clearProctorFields() {
        // ... (Implementation from previous steps)
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}