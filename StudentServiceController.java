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
import java.util.Optional;

public class StudentServiceController implements CommonController {

    private User currentUser;

    // Student Management
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private ComboBox<String> studentGenderCombo;
    @FXML private TextField studentCollegeField;
    @FXML private ComboBox<String> studentBlockCombo;
    @FXML private TextField studentEmailField;
    @FXML private TextField studentPasswordField;
    @FXML private TextField searchStudentField;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colStudentGender;
    @FXML private TableColumn<Student, String> colStudentCollege;
    @FXML private TableColumn<Student, String> colStudentBlock;
    @FXML private TableColumn<Student, String> colStudentEmail;
    @FXML private TableColumn<Student, String> colStudentStatus;

    // Proctor Management
    @FXML private TextField proctorIdField;
    @FXML private TextField proctorNameField;
    @FXML private ComboBox<String> proctorGenderCombo;
    @FXML private ComboBox<String> proctorBlockCombo;
    @FXML private TextField proctorEmailField;
    @FXML private TextField proctorPasswordField;
    @FXML private TextField proctorPhoneField;
    @FXML private TextField searchProctorField;
    @FXML private TableView<Proctor> proctorTable;
    @FXML private TableColumn<Proctor, String> colProctorId;
    @FXML private TableColumn<Proctor, String> colProctorName;
    @FXML private TableColumn<Proctor, String> colProctorGender;
    @FXML private TableColumn<Proctor, String> colProctorBlock;
    @FXML private TableColumn<Proctor, String> colProctorEmail;
    @FXML private TableColumn<Proctor, String> colProctorPhone;
    @FXML private TableColumn<Proctor, String> colProctorStatus;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private ObservableList<Proctor> proctorList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        // Initialize Student Table
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStudentGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colStudentCollege.setCellValueFactory(new PropertyValueFactory<>("college"));
        colStudentBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStudentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Proctor Table
        colProctorId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProctorName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProctorGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colProctorBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colProctorEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colProctorPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colProctorStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Populate ComboBoxes
        studentGenderCombo.getItems().addAll("Male", "Female");
        studentBlockCombo.getItems().addAll("B01", "B02", "B03", "B04", "B05", "B06", "B07", "B08", "B09", "B10");
        
        proctorGenderCombo.getItems().addAll("Male", "Female");
        proctorBlockCombo.getItems().addAll("B01", "B02", "B03", "B04", "B05", "B06", "B07", "B08", "B09", "B10");

        loadStudents();
        loadProctors();
    }

    @FXML
    private void registerStudent() {
        String studentId = studentIdField.getText().trim();
        String fullName = studentNameField.getText().trim();
        String gender = studentGenderCombo.getValue();
        String college = studentCollegeField.getText().trim();
        String blockId = studentBlockCombo.getValue();
        String email = studentEmailField.getText().trim();
        String password = studentPasswordField.getText();

        // Validation
        if (studentId.isEmpty() || fullName.isEmpty() || gender == null || 
            college.isEmpty() || blockId == null || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            showAlert("Error", "Email must be a @gmail.com address", Alert.AlertType.ERROR);
            return;
        }

        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            
            String query = "INSERT INTO student (student_id, full_name, gender, college, block_id, email, password, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setString(1, studentId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, gender);
                pstmt.setString(4, college);
                pstmt.setString(5, blockId);
                pstmt.setString(6, email);
                pstmt.setString(7, hashedPassword);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    showAlert("Success", "Student registered successfully", Alert.AlertType.INFORMATION);
                    clearStudentForm();
                    loadStudents();
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                showAlert("Error", "Student ID or Email already exists", Alert.AlertType.ERROR);
            } else {
                showAlert("Error", "Failed to register student: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void registerProctor() {
        String proctorId = proctorIdField.getText().trim();
        String fullName = proctorNameField.getText().trim();
        String gender = proctorGenderCombo.getValue();
        String blockId = proctorBlockCombo.getValue();
        String email = proctorEmailField.getText().trim();
        String password = proctorPasswordField.getText();
        String phone = proctorPhoneField.getText().trim();

        if (proctorId.isEmpty() || fullName.isEmpty() || gender == null || 
            blockId == null || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            showAlert("Error", "Email must be a @gmail.com address", Alert.AlertType.ERROR);
            return;
        }

        // Check if block already has 2 proctors
        String checkQuery = "SELECT COUNT(*) FROM proctor WHERE block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setString(1, blockId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) >= 2) {
                showAlert("Error", "This block already has two proctors", Alert.AlertType.ERROR);
                return;
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error", Alert.AlertType.ERROR);
            return;
        }

        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            
            String query = "INSERT INTO proctor (proctor_id, full_name, gender, block_id, email, password, phone_number, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setString(1, proctorId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, gender);
                pstmt.setString(4, blockId);
                pstmt.setString(5, email);
                pstmt.setString(6, hashedPassword);
                pstmt.setString(7, phone);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    showAlert("Success", "Proctor registered successfully", Alert.AlertType.INFORMATION);
                    clearProctorForm();
                    loadProctors();
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                showAlert("Error", "Proctor ID or Email already exists", Alert.AlertType.ERROR);
            } else {
                showAlert("Error", "Failed to register proctor: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadStudents() {
        studentList.clear();
        String query = "SELECT * FROM student ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("student_id"));
                student.setFullName(rs.getString("full_name"));
                student.setGender(rs.getString("gender"));
                student.setCollege(rs.getString("college"));
                student.setBlockId(rs.getString("block_id"));
                student.setEmail(rs.getString("email"));
                student.setStatus(rs.getString("status"));
                studentList.add(student);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadProctors() {
        proctorList.clear();
        String query = "SELECT * FROM proctor ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Proctor proctor = new Proctor();
                proctor.setId(rs.getString("proctor_id"));
                proctor.setFullName(rs.getString("full_name"));
                proctor.setGender(rs.getString("gender"));
                proctor.setBlockId(rs.getString("block_id"));
                proctor.setEmail(rs.getString("email"));
                proctor.setPhoneNumber(rs.getString("phone_number"));
                proctor.setStatus(rs.getString("status"));
                proctorList.add(proctor);
            }
            proctorTable.setItems(proctorList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load proctors: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchStudent() {
        String filter = searchStudentField.getText().trim().toLowerCase();
        if (filter.isEmpty()) {
            studentTable.setItems(studentList);
            return;
        }
        
        ObservableList<Student> filtered = FXCollections.observableArrayList();
        for (Student student : studentList) {
            if (student.getId().toLowerCase().contains(filter) ||
                student.getFullName().toLowerCase().contains(filter) ||
                student.getEmail().toLowerCase().contains(filter) ||
                student.getCollege().toLowerCase().contains(filter)) {
                filtered.add(student);
            }
        }
        studentTable.setItems(filtered);
    }

    @FXML
    private void searchProctor() {
        String filter = searchProctorField.getText().trim().toLowerCase();
        if (filter.isEmpty()) {
            proctorTable.setItems(proctorList);
            return;
        }
        
        ObservableList<Proctor> filtered = FXCollections.observableArrayList();
        for (Proctor proctor : proctorList) {
            if (proctor.getId().toLowerCase().contains(filter) ||
                proctor.getFullName().toLowerCase().contains(filter) ||
                proctor.getEmail().toLowerCase().contains(filter) ||
                proctor.getBlockId().toLowerCase().contains(filter)) {
                filtered.add(proctor);
            }
        }
        proctorTable.setItems(filtered);
    }

    private void clearStudentForm() {
        studentIdField.clear();
        studentNameField.clear();
        studentGenderCombo.getSelectionModel().clearSelection();
        studentCollegeField.clear();
        studentBlockCombo.getSelectionModel().clearSelection();
        studentEmailField.clear();
        studentPasswordField.clear();
    }

    private void clearProctorForm() {
        proctorIdField.clear();
        proctorNameField.clear();
        proctorGenderCombo.getSelectionModel().clearSelection();
        proctorBlockCombo.getSelectionModel().clearSelection();
        proctorEmailField.clear();
        proctorPasswordField.clear();
        proctorPhoneField.clear();
    }

    public void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}