package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Proctor;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ProctorCategoriesController implements CommonController {

    private Proctor currentUser;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> colIndex;
    @FXML private TableColumn<Student, String> colId, colName, colGender, colDept, colNationality, colRegion, colCity, colRoom, colResidentName, colResidentRelation, colResidentPhone;
    @FXML private TableColumn<Student, Integer> colYear;
    @FXML private TableColumn<Student, Void> colActions;
    @FXML private TextField searchField;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Proctor) {
            this.currentUser = (Proctor) user;
            loadAssignedStudents();
        }
    }

    @FXML
    private void initialize() {
        colIndex.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(studentTable.getItems().indexOf(column.getValue()) + 1));
        colIndex.setSortable(false);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("yearOfStudy"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colResidentName.setCellValueFactory(new PropertyValueFactory<>("residentFullName"));
        colResidentRelation.setCellValueFactory(new PropertyValueFactory<>("residentRelation"));
        colResidentPhone.setCellValueFactory(new PropertyValueFactory<>("residentPhone"));
        
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Change Room");
            private final Button removeBtn = new Button("Remove");
            private final HBox pane = new HBox(5, updateBtn, removeBtn);
            {
                updateBtn.setOnAction(event -> showAssignRoomDialog(getTableView().getItems().get(getIndex())));
                removeBtn.getStyleClass().add("secondary-button");
                removeBtn.setOnAction(event -> removeStudentFromRoom(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchStudents(newVal));
    }

    private void loadAssignedStudents() {
        studentList.clear();
        String query = "SELECT student_id, full_name, gender, year_of_study, department, nationality, region, city, room_number, resident_full_name, resident_relation, resident_phone FROM student WHERE block_id = ? ORDER BY room_number, full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentUser.getBlockId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getString("student_id"));
                s.setFullName(rs.getString("full_name"));
                s.setGender(rs.getString("gender"));
                s.setYearOfStudy(rs.getInt("year_of_study"));
                s.setDepartment(rs.getString("department"));
                s.setNationality(rs.getString("nationality"));
                s.setRegion(rs.getString("region"));
                s.setCity(rs.getString("city"));
                s.setRoomNumber(rs.getString("room_number"));
                s.setResidentFullName(rs.getString("resident_full_name"));
                s.setResidentRelation(rs.getString("resident_relation"));
                s.setResidentPhone(rs.getString("resident_phone"));
                studentList.add(s);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students.");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showAssignRoomDialog() {
        showAssignRoomDialog(null);
    }

    private void showAssignRoomDialog(Student student) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Assign Student to Room" : "Change Student's Room");
        dialog.setHeaderText(student == null ? "Select a room for the student." : "Select a new room for " + student.getFullName());

        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        if (student != null) {
            studentIdField.setText(student.getId());
            studentIdField.setEditable(false);
        }

        ComboBox<String> roomCombo = new ComboBox<>();
        loadAvailableRooms(roomCombo);

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(new Label("Room:"), 0, 1);
        grid.add(roomCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return roomCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(roomNumber -> {
            String studentId = studentIdField.getText();
            if (studentId.isEmpty() || roomNumber == null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
                return;
            }
            updateStudentRoom(studentId, roomNumber);
        });
    }

    private void loadAvailableRooms(ComboBox<String> combo) {
        combo.getItems().clear();
        String query = "SELECT room_number FROM rooms WHERE block_id = ? AND current_occupancy < capacity ORDER BY room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentUser.getBlockId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                combo.getItems().add(rs.getString("room_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStudentRoom(String studentId, String newRoom) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get student's current room
            String oldRoom = null;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT room_number FROM student WHERE student_id = ?")) {
                pstmt.setString(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    oldRoom = rs.getString("room_number");
                }
            }

            // If no change, do nothing
            if (newRoom.equals(oldRoom)) {
                conn.rollback();
                return;
            }

            // Check capacity of the new room
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT current_occupancy, capacity FROM rooms WHERE room_number = ? AND block_id = ?")) {
                pstmt.setString(1, newRoom);
                pstmt.setString(2, currentUser.getBlockId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt("current_occupancy") >= rs.getInt("capacity")) {
                    showAlert(Alert.AlertType.ERROR, "Room Full", "Cannot assign student. Room " + newRoom + " is already at maximum capacity.");
                    conn.rollback();
                    return;
                }
            }

            // Update student's room
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE student SET room_number = ? WHERE student_id = ?")) {
                pstmt.setString(1, newRoom);
                pstmt.setString(2, studentId);
                pstmt.executeUpdate();
            }

            // Increment new room's occupancy
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE rooms SET current_occupancy = current_occupancy + 1 WHERE room_number = ? AND block_id = ?")) {
                pstmt.setString(1, newRoom);
                pstmt.setString(2, currentUser.getBlockId());
                pstmt.executeUpdate();
            }

            // Decrement old room's occupancy if the student was in one
            if (oldRoom != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE rooms SET current_occupancy = current_occupancy - 1 WHERE room_number = ? AND block_id = ?")) {
                    pstmt.setString(1, oldRoom);
                    pstmt.setString(2, currentUser.getBlockId());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student room updated successfully.");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the room.");
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            loadAssignedStudents();
        }
    }

    private void removeStudentFromRoom(Student student) {
        String roomNumber = student.getRoomNumber();
        if (roomNumber == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Action", "This student is not currently assigned to a room.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This will remove the student from the room. Are you sure?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove " + student.getFullName() + " from room " + roomNumber + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);

                    // Set student's room to null
                    try (PreparedStatement pstmt = conn.prepareStatement("UPDATE student SET room_number = NULL WHERE student_id = ?")) {
                        pstmt.setString(1, student.getId());
                        pstmt.executeUpdate();
                    }

                    // Decrement room occupancy
                    try (PreparedStatement pstmt = conn.prepareStatement("UPDATE rooms SET current_occupancy = current_occupancy - 1 WHERE room_number = ? AND block_id = ?")) {
                        pstmt.setString(1, roomNumber);
                        pstmt.setString(2, currentUser.getBlockId());
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student removed from room.");

                } catch (SQLException e) {
                    if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove student from room.");
                    e.printStackTrace();
                } finally {
                    if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                    loadAssignedStudents();
                }
            }
        });
    }

    private void searchStudents(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            studentTable.setItems(studentList);
            return;
        }
        ObservableList<Student> filteredList = FXCollections.observableArrayList();
        for (Student student : studentList) {
            if (student.getFullName().toLowerCase().contains(searchText.toLowerCase()) ||
                (student.getRoomNumber() != null && student.getRoomNumber().toLowerCase().contains(searchText.toLowerCase()))) {
                filteredList.add(student);
            }
        }
        studentTable.setItems(filteredList);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
