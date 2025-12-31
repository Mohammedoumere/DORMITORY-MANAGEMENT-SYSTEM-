package com.dormitory.controllers;

import com.dormitory.models.Proctor;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import com.dormitory.DatabaseConnection;
import com.dormitory.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.Optional;

public class StudentController implements CommonController {
    
    private User currentUser;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colStudentEmail;
    @FXML private TableColumn<Student, String> colStudentCollege;
    @FXML private TableColumn<Student, String> colStudentBlock;
    @FXML private TableColumn<Student, String> colStudentRoom;
    @FXML private TableColumn<Student, String> colStudentStatus;
    @FXML private TableColumn<Student, Void> colStudentActions;
    
    @FXML private Label welcomeLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label studentBlockLabel;
    @FXML private Label studentRoomLabel;
    @FXML private Label studentStatusLabel;
    @FXML private Label studentCollegeLabel;
    @FXML private Label studentYearLabel;
    @FXML private VBox dashboardStats;
    
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initialize();
    }
    
    @FXML
    private void initialize() {
        if (currentUser == null) return;

        if (studentTable != null) {
            initializeStudentManagement();
        }
        if (welcomeLabel != null) {
            initializeDashboard();
        }
    }
    
    private void initializeStudentManagement() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStudentCollege.setCellValueFactory(new PropertyValueFactory<>("college"));
        colStudentBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colStudentRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colStudentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colStudentActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, deleteButton);
            
            {
                deleteButton.getStyleClass().add("secondary-button");
                deleteButton.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    deleteStudent(student);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        loadStudents();
    }
    
    private void initializeDashboard() {
        if (currentUser instanceof Student) {
            Student student = (Student) currentUser;
            welcomeLabel.setText("Welcome, " + student.getFullName() + "!");
            studentIdLabel.setText(student.getId());
            studentBlockLabel.setText(student.getBlockId() != null ? student.getBlockId() : "Not Assigned");
            studentRoomLabel.setText(student.getRoomNumber() != null ? student.getRoomNumber() : "Not Assigned");
            studentStatusLabel.setText(student.getStatus() != null ? student.getStatus() : "Pending");
            studentCollegeLabel.setText(student.getCollege() != null ? student.getCollege() : "Not Specified");
            studentYearLabel.setText("Year " + student.getYearOfStudy());
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
                student.setEmail(rs.getString("email"));
                student.setCollege(rs.getString("college"));
                student.setBlockId(rs.getString("block_id"));
                student.setRoomNumber(rs.getString("room_number"));
                student.setStatus(rs.getString("status"));
                student.setGender(rs.getString("gender"));
                student.setYearOfStudy(rs.getInt("year_of_study"));
                student.setPhoneNumber(rs.getString("phone_number"));
                studentList.add(student);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void deleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete student: " + student.getFullName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM student WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, student.getId());
                pstmt.executeUpdate();
                showAlert("Success", "Student deleted successfully", Alert.AlertType.INFORMATION);
                loadStudents();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete student: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}