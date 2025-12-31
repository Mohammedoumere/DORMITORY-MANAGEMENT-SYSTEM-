package com.dormitory.controllers;

import com.dormitory.DatabaseConnection;
import com.dormitory.models.Student;
import com.dormitory.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDetailsController implements CommonController {

    private Student currentUser;

    @FXML private Label nameLabel, idLabel, genderLabel, collegeLabel, departmentLabel, yearLabel, blockLabel, roomLabel;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> nationalityCombo, regionCombo, cityCombo;
    @FXML private TextField residentNameField, residentRelationField, residentPhoneField;

    private final List<String> nationalities = Arrays.asList("Ethiopia", "Kenya", "Sudan", "Somalia", "South Sudan", "Other");
    private final List<String> regions = Arrays.asList("Addis Ababa (Chartered City)", "Afar", "Amhara", "Benishangul-Gumuz", "Dire Dawa (Chartered City)", "Gambela", "Harari", "Oromia", "Sidama", "Somali", "South West Ethiopia Peoples' Region", "Southern Nations, Nationalities, and Peoples' Region (SNNPR)", "Tigray");
    private final Map<String, List<String>> regionCities = new HashMap<>();

    @Override
    public void setCurrentUser(User user) {
        if (user instanceof Student) {
            this.currentUser = (Student) user;
            initializeRegionCityData();
            nationalityCombo.getItems().addAll(nationalities);
            regionCombo.getItems().addAll(regions);
            loadStudentDetails();
        }
    }
    
    @FXML
    private void initialize() {
        nationalityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEthiopian = "Ethiopia".equals(newVal);
            regionCombo.setVisible(isEthiopian);
            regionCombo.setManaged(isEthiopian);
            cityCombo.setVisible(isEthiopian);
            cityCombo.setManaged(isEthiopian);
            if (!isEthiopian) {
                regionCombo.getSelectionModel().clearSelection();
            }
        });

        regionCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            cityCombo.getItems().clear();
            if (newVal != null && regionCities.containsKey(newVal)) {
                cityCombo.getItems().setAll(regionCities.get(newVal));
            }
        });
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

    private void loadStudentDetails() {
        String query = "SELECT * FROM student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("full_name"));
                idLabel.setText(rs.getString("student_id"));
                genderLabel.setText(rs.getString("gender"));
                collegeLabel.setText(rs.getString("college"));
                departmentLabel.setText(rs.getString("department"));
                yearLabel.setText(String.valueOf(rs.getInt("year_of_study")));
                blockLabel.setText(rs.getString("block_id"));
                roomLabel.setText(rs.getString("room_number"));
                phoneField.setText(rs.getString("phone_number"));
                nationalityCombo.setValue(rs.getString("nationality"));
                regionCombo.setValue(rs.getString("region"));
                cityCombo.setValue(rs.getString("city"));
                residentNameField.setText(rs.getString("resident_full_name"));
                residentRelationField.setText(rs.getString("resident_relation"));
                residentPhoneField.setText(rs.getString("resident_phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateContactInfo() {
        String query = "UPDATE student SET phone_number = ?, nationality = ?, region = ?, city = ?, " +
                       "resident_full_name = ?, resident_relation = ?, resident_phone = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String nationality = nationalityCombo.getValue();
            if ("Other".equals(nationality)) {
                // In a real app, you'd have an "otherNationalityField"
                // For now, we just save "Other"
            }

            pstmt.setString(1, phoneField.getText());
            pstmt.setString(2, nationality);
            pstmt.setString(3, "Ethiopia".equals(nationalityCombo.getValue()) ? regionCombo.getValue() : null);
            pstmt.setString(4, "Ethiopia".equals(nationalityCombo.getValue()) ? cityCombo.getValue() : null);
            pstmt.setString(5, residentNameField.getText());
            pstmt.setString(6, residentRelationField.getText());
            pstmt.setString(7, residentPhoneField.getText());
            pstmt.setString(8, currentUser.getId());
            
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your information has been updated.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update your information.");
            e.printStackTrace();
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
