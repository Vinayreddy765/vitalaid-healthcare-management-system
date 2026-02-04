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
 * DonorRegistrationController - Handles donor registration form
 */
public class DonorRegistrationController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField weightField;
    @FXML private TextArea addressArea;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> stateCombo;
    @FXML private TextField pincodeField;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label errorLabel;
    
    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    
    @FXML
    public void initialize() {
        bloodGroupCombo.setItems(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
        
        genderCombo.setItems(FXCollections.observableArrayList(
            "Male", "Female", "Other"
        ));
        
        stateCombo.setItems(FXCollections.observableArrayList(
            "Andhra Pradesh", "Karnataka", "Maharashtra", "Tamil Nadu", "Telangana",
            "Kerala", "Gujarat", "Rajasthan", "Punjab", "Haryana"
        ));
        
        stateCombo.setValue("Karnataka");
    }
    
    @FXML
    private void handleRegister() {
        try {
            if (!validateInputs()) {
                return;
            }
            
            if (userDAO.usernameExists(usernameField.getText().trim())) {
                showError("Username already exists. Please choose another.");
                return;
            }
            
            User user = new User(
                usernameField.getText().trim(),
                passwordField.getText(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                User.UserType.DONOR
            );
            
            int userId = userDAO.createUser(user);
            
            if (userId == -1) {
                showError("Failed to create user account.");
                return;
            }
            
            Donor donor = new Donor();
            donor.setUserId(userId);
            donor.setFullName(fullNameField.getText().trim());
            donor.setBloodGroup(parseBloodGroup(bloodGroupCombo.getValue()));
            donor.setDateOfBirth(dobPicker.getValue());
            donor.setGender(parseGender(genderCombo.getValue()));
            donor.setWeight(Double.parseDouble(weightField.getText().trim()));
            donor.setAddress(addressArea.getText().trim());
            donor.setCity(cityField.getText().trim());
            donor.setState(stateCombo.getValue());
            donor.setPincode(pincodeField.getText().trim());
            donor.setLatitude(12.9716);
            donor.setLongitude(77.5946);
            donor.setAvailable(true);
            
            int donorId = donorDAO.registerDonor(donor);
            
            if (donorId != -1) {
                VitalAidApp.showSuccessAlert("Success", 
                    "Registration successful!\nYou can now login with your credentials.");
                handleCancel();
            } else {
                showError("Failed to complete registration.");
            }
            
        } catch (Exception e) {
            showError("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            return false;
        }
        
        if (!ValidationUtil.isValidUsername(usernameField.getText().trim())) {
            showError("Username must be 3-20 characters");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            return false;
        }
        
        if (!ValidationUtil.isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showError("Phone number is required");
            return false;
        }
        
        if (!ValidationUtil.isValidPhone(phoneField.getText().trim())) {
            showError("Please enter a valid 10-digit number");
            return false;
        }
        
        if (passwordField.getText().isEmpty()) {
            showError("Password is required");
            return false;
        }
        
        if (!ValidationUtil.isValidPassword(passwordField.getText())) {
            showError("Password must be at least 8 characters");
            return false;
        }
        
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }
        
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required");
            return false;
        }
        
        if (bloodGroupCombo.getValue() == null) {
            showError("Please select blood group");
            return false;
        }
        
        if (dobPicker.getValue() == null) {
            showError("Date of birth is required");
            return false;
        }
        
        if (!ValidationUtil.isValidDonorAge(dobPicker.getValue())) {
            showError("You must be between 18 and 65 years old");
            return false;
        }
        
        if (genderCombo.getValue() == null) {
            showError("Please select gender");
            return false;
        }
        
        if (weightField.getText().trim().isEmpty()) {
            showError("Weight is required");
            return false;
        }
        
        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            if (!ValidationUtil.isValidDonorWeight(weight)) {
                showError("Weight must be at least 50 kg");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid weight");
            return false;
        }
        
        if (cityField.getText().trim().isEmpty()) {
            showError("City is required");
            return false;
        }
        
        if (stateCombo.getValue() == null) {
            showError("Please select state");
            return false;
        }
        
        if (pincodeField.getText().trim().isEmpty()) {
            showError("Pincode is required");
            return false;
        }
        
        if (!ValidationUtil.isValidPincode(pincodeField.getText().trim())) {
            showError("Please enter a valid 6-digit pincode");
            return false;
        }
        
        if (!termsCheckbox.isSelected()) {
            showError("You must agree to the Terms and Conditions");
            return false;
        }
        
        return true;
    }
    
    private Donor.BloodGroup parseBloodGroup(String bloodGroup) {
        return Donor.BloodGroup.valueOf(
            bloodGroup.replace("+", "_POSITIVE").replace("-", "_NEGATIVE")
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