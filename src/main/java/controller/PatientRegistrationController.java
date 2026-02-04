package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import model.*;
import dao.*;
import util.ValidationUtil;
import view.VitalAidApp;
import java.time.LocalDate;

/**
 * PatientRegistrationController - Handles patient registration
 */
public class PatientRegistrationController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField emergencyContactField;
    @FXML private TextArea addressArea;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> stateCombo;
    @FXML private TextField pincodeField;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label errorLabel;
    
    private final UserDAO userDAO = new UserDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    
    @FXML
    public void initialize() {
        // Populate combos
        bloodGroupCombo.setItems(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
        
        genderCombo.setItems(FXCollections.observableArrayList(
            "Male", "Female", "Other"
        ));
        
        stateCombo.setItems(FXCollections.observableArrayList(
            "Andhra Pradesh", "Karnataka", "Maharashtra", "Tamil Nadu", "Telangana"
            // Add more states as needed
        ));
        
        stateCombo.setValue("Karnataka");
    }
    
    @FXML
    private void handleRegister() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Check username
            if (userDAO.usernameExists(usernameField.getText().trim())) {
                showError("Username already exists");
                return;
            }
            
            // Create user
            User user = new User(
                usernameField.getText().trim(),
                passwordField.getText(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                User.UserType.PATIENT
            );
            
            int userId = userDAO.createUser(user);
            
            if (userId == -1) {
                showError("Failed to create user account");
                return;
            }
            
            // Create patient
            Patient patient = new Patient();
            patient.setUserId(userId);
            patient.setFullName(fullNameField.getText().trim());
            patient.setBloodGroup(parseBloodGroup(bloodGroupCombo.getValue()));
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setGender(parseGender(genderCombo.getValue()));
            patient.setEmergencyContact(emergencyContactField.getText().trim());
            patient.setAddress(addressArea.getText().trim());
            patient.setCity(cityField.getText().trim());
            patient.setState(stateCombo.getValue());
            patient.setPincode(pincodeField.getText().trim());
            
            int patientId = patientDAO.registerPatient(patient);
            
            if (patientId != -1) {
                VitalAidApp.showSuccessAlert("Success", 
                    "Registration successful!\nYou can now login.");
                handleCancel();
            } else {
                showError("Failed to complete registration");
            }
            
        } catch (Exception e) {
            showError("Registration error: " + e.getMessage());
        }
    }
    
    private boolean validateInputs() {
        // Similar validation as DonorRegistrationController
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            return false;
        }
        
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }
        
        if (!termsCheckbox.isSelected()) {
            showError("You must agree to the terms");
            return false;
        }
        
        // Add more validations...
        
        return true;
    }
    
    private Donor.BloodGroup parseBloodGroup(String bg) {
        return Donor.BloodGroup.valueOf(
            bg.replace("+", "_POSITIVE").replace("-", "_NEGATIVE")
        );
    }
    
    private Donor.Gender parseGender(String gender) {
        return Donor.Gender.valueOf(gender.toUpperCase());
    }
    
    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}