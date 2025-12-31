package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Student;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentManagementController {

    @FXML private TextField searchStudentField;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> colStudentIndex;
    @FXML private TableColumn<Student, String> colStudentId, colStudentName, colStudentCollege, colStudentDepartment, colStudentBlock, colStudentStatus, colStudentGender, colStudentNationality, colStudentRegion, colStudentCity;
    @FXML private TableColumn<Student, Integer> colStudentYear;
    @FXML private TableColumn<Student, Void> colStudentActions;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final Map<String, List<String>> collegeDepartments = new HashMap<>();
    private final Map<String, List<String>> regionCities = new HashMap<>();
    private final List<String> maleBlocks = Arrays.asList("B01", "B02", "B03", "B04", "B05");
    private final List<String> femaleBlocks = Arrays.asList("B06", "B07", "B08", "B09", "B10");
    private final List<String> regions = Arrays.asList("Addis Ababa (Chartered City)", "Afar", "Amhara", "Benishangul-Gumuz", "Dire Dawa (Chartered City)", "Gambela", "Harari", "Oromia", "Sidama", "Somali", "South West Ethiopia Peoples' Region", "Southern Nations, Nationalities, and Peoples' Region (SNNPR)", "Tigray");
    private final List<String> nationalities = Arrays.asList("Ethiopia", "Kenya", "Sudan", "Somalia", "South Sudan", "Other");

    @FXML
    private void initialize() {
        setupStudentTable();
        initializeCollegeData();
        initializeRegionCityData();
        searchStudentField.textProperty().addListener((obs, oldVal, newVal) -> searchStudents());
        loadStudents();
    }

    private void initializeCollegeData() {
        collegeDepartments.put("College of Technology and Engineering", Arrays.asList("Computer Science", "Electrical Engineering", "Computer Engineering", "Architecture", "Construction Technology and Management", "Civil Engineering", "Mechanical Engineering", "Automotive Engineering", "Water Resource and Irrigation Engineering", "Hydraulics and Water Resource Engineering"));
        collegeDepartments.put("College of Business and Economics", Arrays.asList("Economics", "Accounting and Finance", "Logistics and Supply Chain Management", "Public Administration and Development Management", "Management"));
        collegeDepartments.put("College of Health and Medical Sciences", Arrays.asList("Anesthesiology", "Medical Laboratory", "Public Health", "Environmental Health", "Midwifery", "Nursing", "Psychiatry", "Pharmacy", "Medicine"));
        collegeDepartments.put("College of Social Science and Humanities", Arrays.asList("Amharic Language & Literature", "Civics & Ethical Studies", "English Language & Literature", "Gedeo Language & Literature", "Geography & Environmental Studies", "History & Heritage Management", "Journalism & Communication", "Oromo Language & Literature", "Social Anthropology", "Sociology"));
        collegeDepartments.put("College of Agriculture and Natural Resources", Arrays.asList("Agricultural Economics", "Animal and Range Science", "Horticulture", "Land Administration and Surveying", "Natural Resource Management", "Plant Science", "Veterinary Science"));
        collegeDepartments.put("College of Natural and Computational Science", Arrays.asList("Biology", "Chemistry", "Mathematics", "Physics", "Sport Science", "Geology", "Statistics"));
        collegeDepartments.put("College of Indigenous Studies", Arrays.asList("Gadaa Studies", "Oromo Folklore", "Afan Oromo"));
    }
    
    private void initializeRegionCityData() {
        regionCities.put("Addis Ababa (Chartered City)", Arrays.asList("Addis Ketema", "Akaki Kality", "Arada", "Bole", "Gullele", "Kirkos", "Kolfe Keranio", "Lideta", "Nifas Silk-Lafto", "Yeka"));
        regionCities.put("Afar", Arrays.asList("Semera", "Asayita", "Dubti", "Logiya", "Gewane", "Mille", "Amibara", "Elidar", "Dalol", "Dallo"));
        regionCities.put("Amhara", Arrays.asList("Bahir Dar", "Gondar", "Debre Markos", "Dessie", "Woldia", "Debre Tabor", "Lalibela", "Finote Selam", "Kemise", "Debre Sina", "Motta", "Chagni", "Gonder Zuria", "Kombolcha", "Sekota"));
        regionCities.put("Benishangul-Gumuz", Arrays.asList("Assosa", "Mandura", "Gilgel Beles", "Kamashi", "Sherkole", "Mendaya", "Dangur", "Bambasi", "Metekel", "Belo"));
        regionCities.put("Dire Dawa (Chartered City)", Arrays.asList("Dechatu", "Gende Kore", "Melka Jebdu", "Menaheriya", "Tirunesh Beijing", "Addis Ketema", "Segno Gebeya", "Lugdi", "Gende Weyib"));
        regionCities.put("Gambela", Arrays.asList("Gambela", "Itang", "Abobo", "Pachilo", "Jor", "Lare"));
        regionCities.put("Harari", Arrays.asList("Aboker", "Shenkor", "Jugal", "Hakim", "Shahi", "Sofi", "Erer", "Dire-Teyara", "Jardega Jarte"));
        regionCities.put("Oromia", Arrays.asList("Adama (Nazret)", "Jimma", "Bishoftu (Debre Zeit)", "Nekemte", "Asella", "Shashamane", "Ambo", "Gimbi", "Bedele", "Dembi Dolo", "Chiro", "Bako", "Goba"));
        regionCities.put("Sidama", Arrays.asList("Hawassa", "Aleta Wendo", "Dara", "Wondo Genet", "Hula", "Bensa"));
        regionCities.put("Somali", Arrays.asList("Jijiga", "Degehabur", "Gode", "Kebri Dehar", "Shilavo", "Awbare", "Dolo", "Fik", "Hargeysa", "Mustahil"));
        regionCities.put("South West Ethiopia Peoples' Region", Arrays.asList("Mizan Teferi", "Tercha", "Bonga", "Tepi", "Sheko"));
        regionCities.put("Southern Nations, Nationalities, and Peoples' Region (SNNPR)", Arrays.asList("Arba Minch", "Wolaita Sodo", "Dilla", "Mizan Aman", "Jinka", "Bonga", "Sodo", "Tercha", "Konso", "Sawla"));
        regionCities.put("Tigray", Arrays.asList("Mekelle", "Adigrat", "Shire", "Axum (Aksum)", "Wukro", "Alamata", "Hawzen", "Mai Tsebri", "Raya Azebo", "Dansha"));
    }

    private void setupStudentTable() {
        colStudentIndex.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(studentTable.getItems().indexOf(column.getValue()) + 1));
        colStudentIndex.setSortable(false);
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colStudentGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colStudentCollege.setCellValueFactory(new PropertyValueFactory<>("college"));
        colStudentDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colStudentYear.setCellValueFactory(new PropertyValueFactory<>("yearOfStudy"));
        colStudentNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colStudentRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colStudentCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colStudentBlock.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colStudentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colStudentActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, updateButton, deleteButton);
            {
                updateButton.setOnAction(event -> showUpdateStudentDialog(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteStudent(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadStudents() {
        studentList.clear();
        String query = "SELECT student_id, full_name, gender, college, department, year_of_study, nationality, region, city, block_id, status, email FROM student";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getString("student_id"));
                s.setFullName(rs.getString("full_name"));
                s.setGender(rs.getString("gender"));
                s.setCollege(rs.getString("college"));
                s.setDepartment(rs.getString("department"));
                s.setYearOfStudy(rs.getInt("year_of_study"));
                s.setNationality(rs.getString("nationality"));
                s.setRegion(rs.getString("region"));
                s.setCity(rs.getString("city"));
                s.setBlockId(rs.getString("block_id"));
                s.setStatus(rs.getString("status"));
                s.setEmail(rs.getString("email"));
                studentList.add(s);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students.");
        }
    }

    @FXML
    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details");
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField idField = new TextField();
        idField.setPromptText("Student ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email (@gmail.com)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        ComboBox<String> nationalityCombo = new ComboBox<>();
        nationalityCombo.getItems().addAll(nationalities);
        nationalityCombo.setPromptText("Select Nationality");
        TextField otherNationalityField = new TextField();
        otherNationalityField.setPromptText("Please specify");
        otherNationalityField.setVisible(false);
        otherNationalityField.setManaged(false);
        ComboBox<String> regionCombo = new ComboBox<>();
        regionCombo.getItems().addAll(regions);
        regionCombo.setPromptText("Select Region");
        ComboBox<String> cityCombo = new ComboBox<>();
        cityCombo.setPromptText("Select City");
        nationalityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = "Other".equals(newVal);
            otherNationalityField.setVisible(isOther);
            otherNationalityField.setManaged(isOther);
            boolean isEthiopian = "Ethiopia".equals(newVal);
            regionCombo.setVisible(isEthiopian);
            regionCombo.setManaged(isEthiopian);
            cityCombo.setVisible(isEthiopian);
            cityCombo.setManaged(isEthiopian);
            if (!isEthiopian) {
                regionCombo.getSelectionModel().clearSelection();
                cityCombo.getSelectionModel().clearSelection();
            }
        });
        regionCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            cityCombo.getItems().clear();
            if (newVal != null && regionCities.containsKey(newVal)) {
                cityCombo.getItems().setAll(regionCities.get(newVal));
            }
        });
        regionCombo.setVisible(false);
        regionCombo.setManaged(false);
        cityCombo.setVisible(false);
        cityCombo.setManaged(false);
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Gender");
        ComboBox<String> collegeCombo = new ComboBox<>();
        collegeCombo.getItems().addAll(collegeDepartments.keySet());
        collegeCombo.setPromptText("Select College");
        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.setPromptText("Select Department");
        collegeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            departmentCombo.getItems().clear();
            if (newVal != null) {
                departmentCombo.getItems().setAll(collegeDepartments.get(newVal));
            }
        });
        ComboBox<Integer> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7);
        yearCombo.setPromptText("Year");
        ComboBox<String> blockCombo = new ComboBox<>();
        blockCombo.setPromptText("Block");
        genderCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            blockCombo.getItems().clear();
            if ("Male".equals(newVal)) {
                blockCombo.getItems().setAll(maleBlocks);
            } else if ("Female".equals(newVal)) {
                blockCombo.getItems().setAll(femaleBlocks);
            }
        });
        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Gender:"), 0, 2);
        grid.add(genderCombo, 1, 2);
        grid.add(new Label("College:"), 0, 3);
        grid.add(collegeCombo, 1, 3);
        grid.add(new Label("Department:"), 0, 4);
        grid.add(departmentCombo, 1, 4);
        grid.add(new Label("Year:"), 0, 5);
        grid.add(yearCombo, 1, 5);
        grid.add(new Label("Email:"), 0, 6);
        grid.add(emailField, 1, 6);
        grid.add(new Label("Password:"), 0, 7);
        grid.add(passwordField, 1, 7);
        grid.add(new Label("Phone:"), 0, 8);
        grid.add(phoneField, 1, 8);
        grid.add(new Label("Nationality:"), 0, 9);
        grid.add(nationalityCombo, 1, 9);
        grid.add(otherNationalityField, 1, 10);
        grid.add(new Label("Region:"), 0, 11);
        grid.add(regionCombo, 1, 11);
        grid.add(new Label("City:"), 0, 12);
        grid.add(cityCombo, 1, 12);
        grid.add(new Label("Block:"), 0, 13);
        grid.add(blockCombo, 1, 13);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() || collegeCombo.getValue() == null || departmentCombo.getValue() == null || yearCombo.getValue() == null || emailField.getText().isEmpty() || passwordField.getText().isEmpty() || genderCombo.getValue() == null || blockCombo.getValue() == null || phoneField.getText().isEmpty() || nationalityCombo.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all required fields.");
                    return null;
                }
                String nationality = nationalityCombo.getValue();
                if ("Other".equals(nationality)) {
                    nationality = otherNationalityField.getText();
                }
                Student s = new Student();
                s.setId(idField.getText());
                s.setFullName(nameField.getText());
                s.setCollege(collegeCombo.getValue());
                s.setDepartment(departmentCombo.getValue());
                s.setYearOfStudy(yearCombo.getValue());
                s.setGender(genderCombo.getValue());
                s.setEmail(emailField.getText());
                s.setPassword(passwordField.getText());
                s.setPhoneNumber(phoneField.getText());
                s.setNationality(nationality);
                s.setRegion("Ethiopia".equals(nationalityCombo.getValue()) ? regionCombo.getValue() : null);
                s.setCity("Ethiopia".equals(nationalityCombo.getValue()) ? cityCombo.getValue() : null);
                s.setBlockId(blockCombo.getValue());
                return s;
            }
            return null;
        });
        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(this::registerStudentInDatabase);
    }

    private void registerStudentInDatabase(Student student) {
        String hashedPassword = PasswordUtils.hashPassword(student.getPassword());
        String query = "INSERT INTO student (student_id, full_name, email, password, age, gender, year_of_study, college, department, nationality, region, city, phone_number, block_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ASSIGNED')";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, hashedPassword);
            pstmt.setInt(5, 18);
            pstmt.setString(6, student.getGender());
            pstmt.setInt(7, student.getYearOfStudy());
            pstmt.setString(8, student.getCollege());
            pstmt.setString(9, student.getDepartment());
            pstmt.setString(10, student.getNationality());
            pstmt.setString(11, student.getRegion());
            pstmt.setString(12, student.getCity());
            pstmt.setString(13, student.getPhoneNumber());
            pstmt.setString(14, student.getBlockId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student registered successfully.");
            loadStudents();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register student.");
        }
    }

    private void showUpdateStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Update Student");
        dialog.setHeaderText("Editing details for " + student.getFullName());
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField nameField = new TextField(student.getFullName());
        TextField emailField = new TextField(student.getEmail());
        ComboBox<String> collegeComboDialog = new ComboBox<>();
        collegeComboDialog.getItems().addAll(collegeDepartments.keySet());
        collegeComboDialog.setValue(student.getCollege());
        ComboBox<String> departmentComboDialog = new ComboBox<>();
        if (student.getCollege() != null && collegeDepartments.containsKey(student.getCollege())) {
            departmentComboDialog.getItems().setAll(collegeDepartments.get(student.getCollege()));
        }
        departmentComboDialog.setValue(student.getDepartment());
        collegeComboDialog.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            departmentComboDialog.getItems().clear();
            if (newVal != null) {
                departmentComboDialog.getItems().setAll(collegeDepartments.get(newVal));
            }
        });
        ComboBox<Integer> yearComboDialog = new ComboBox<>();
        yearComboDialog.getItems().addAll(1, 2, 3, 4, 5, 6, 7);
        yearComboDialog.setValue(student.getYearOfStudy());
        ComboBox<String> blockCombo = new ComboBox<>();
        if ("Male".equals(student.getGender())) {
            blockCombo.getItems().setAll(maleBlocks);
        } else {
            blockCombo.getItems().setAll(femaleBlocks);
        }
        blockCombo.setValue(student.getBlockId());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (Optional)");
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("College:"), 0, 2);
        grid.add(collegeComboDialog, 1, 2);
        grid.add(new Label("Department:"), 0, 3);
        grid.add(departmentComboDialog, 1, 3);
        grid.add(new Label("Year:"), 0, 4);
        grid.add(yearComboDialog, 1, 4);
        grid.add(new Label("Block:"), 0, 5);
        grid.add(blockCombo, 1, 5);
        grid.add(new Label("New Password:"), 0, 6);
        grid.add(passwordField, 1, 6);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                String email = emailField.getText().trim();
                if (!email.toLowerCase().endsWith("@gmail.com")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Email", "Email address must end with @gmail.com");
                    return null;
                }
                student.setFullName(nameField.getText());
                student.setEmail(email);
                student.setCollege(collegeComboDialog.getValue());
                student.setDepartment(departmentComboDialog.getValue());
                student.setYearOfStudy(yearComboDialog.getValue());
                student.setBlockId(blockCombo.getValue());
                if (!passwordField.getText().isEmpty()) {
                    student.setPassword(passwordField.getText());
                } else {
                    student.setPassword(null);
                }
                return student;
            }
            return null;
        });
        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(this::updateStudentInDatabase);
    }

    private void updateStudentInDatabase(Student student) {
        boolean updatePassword = student.getPassword() != null && !student.getPassword().isEmpty();
        StringBuilder queryBuilder = new StringBuilder("UPDATE student SET full_name = ?, email = ?, college = ?, department = ?, year_of_study = ?, block_id = ?");
        if (updatePassword) {
            queryBuilder.append(", password = ?");
        }
        queryBuilder.append(" WHERE student_id = ?");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            pstmt.setString(paramIndex++, student.getFullName());
            pstmt.setString(paramIndex++, student.getEmail());
            pstmt.setString(paramIndex++, student.getCollege());
            pstmt.setString(paramIndex++, student.getDepartment());
            pstmt.setInt(paramIndex++, student.getYearOfStudy());
            pstmt.setString(paramIndex++, student.getBlockId());
            if (updatePassword) {
                pstmt.setString(paramIndex++, PasswordUtils.hashPassword(student.getPassword()));
            }
            pstmt.setString(paramIndex, student.getId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student details updated successfully.");
            loadStudents();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update student.");
        }
    }

    private void deleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Student: " + student.getFullName());
        alert.setContentText("Are you sure you want to permanently delete this student? This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM student WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, student.getId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully.");
                loadStudents();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete student.");
            }
        }
    }

    @FXML
    private void searchStudents() {
        String searchText = searchStudentField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            studentTable.setItems(studentList);
            return;
        }
        ObservableList<Student> filteredList = FXCollections.observableArrayList();
        for (Student student : studentList) {
            if (student.getId().toLowerCase().contains(searchText) || (student.getFullName() != null && student.getFullName().toLowerCase().contains(searchText))) {
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
