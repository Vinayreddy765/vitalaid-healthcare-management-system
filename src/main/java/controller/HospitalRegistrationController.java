package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import model.*;
import dao.*;
import util.ValidationUtil;
import view.VitalAidApp;

/**
 * HospitalRegistrationController - Handles hospital registration
 */
public class HospitalRegistrationController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField hospitalNameField;
    @FXML private TextField registrationNumberField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField contactPersonField;
    @FXML private TextField bedCapacityField;
    @FXML private TextArea addressArea;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> stateCombo;
    @FXML private TextField pincodeField;
    @FXML private CheckBox hasBloodBankCheckbox;
    @FXML private CheckBox hasPlasmaCheckbox;
    @FXML private CheckBox hasVentilatorsCheckbox;
    @FXML private CheckBox has24x7Checkbox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label errorLabel;
    
    private final UserDAO userDAO = new UserDAO();
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    
    @FXML
    public void initialize() {
        stateCombo.setItems(FXCollections.observableArrayList(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
            "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
            "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal"
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
                showError("Username already exists");
                return;
            }
            
            // Create user
            User user = new User(
                usernameField.getText().trim(),
                passwordField.getText(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                User.UserType.HOSPITAL
            );
            
            int userId = userDAO.createUser(user);
            
            if (userId == -1) {
                showError("Failed to create account");
                return;
            }
            
            // Create hospital
            Hospital hospital = new Hospital();
            hospital.setUserId(userId);
            hospital.setHospitalName(hospitalNameField.getText().trim());
            hospital.setRegistrationNumber(registrationNumberField.getText().trim());
            hospital.setLicenseNumber(licenseNumberField.getText().trim());
            hospital.setContactPerson(contactPersonField.getText().trim());
            hospital.setBedCapacity(Integer.parseInt(bedCapacityField.getText().trim()));
            hospital.setAddress(addressArea.getText().trim());
            hospital.setCity(cityField.getText().trim());
            hospital.setState(stateCombo.getValue());
            hospital.setPincode(pincodeField.getText().trim());
            hospital.setLatitude(12.9716);
            hospital.setLongitude(77.5946);
            hospital.setHasBloodBank(hasBloodBankCheckbox.isSelected());
            hospital.setVerified(false); // Requires admin approval
            
            int hospitalId = hospitalDAO.registerHospital(hospital);
            
            if (hospitalId != -1) {
                VitalAidApp.showSuccessAlert("Success", 
                    "Registration submitted successfully!\n\n" +
                    "Your hospital registration is pending admin approval.\n" +
                    "You will be notified via email once approved.\n\n" +
                    "Thank you for joining VitalAid!");
                handleCancel();
            } else {
                showError("Failed to complete registration");
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
        
        if (!ValidationUtil.isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email");
            return false;
        }
        
        if (!ValidationUtil.isValidPhone(phoneField.getText().trim())) {
            showError("Please enter a valid phone number");
            return false;
        }
        
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }
        
        if (hospitalNameField.getText().trim().isEmpty()) {
            showError("Hospital name is required");
            return false;
        }
        
        if (registrationNumberField.getText().trim().isEmpty()) {
            showError("Registration number is required");
            return false;
        }
        
        if (contactPersonField.getText().trim().isEmpty()) {
            showError("Contact person is required");
            return false;
        }
        
        try {
            int capacity = Integer.parseInt(bedCapacityField.getText().trim());
            if (capacity <= 0) {
                showError("Bed capacity must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid bed capacity");
            return false;
        }
        
        if (cityField.getText().trim().isEmpty()) {
            showError("City is required");
            return false;
        }
        
        if (!ValidationUtil.isValidPincode(pincodeField.getText().trim())) {
            showError("Please enter a valid 6-digit pincode");
            return false;
        }
        
        if (!termsCheckbox.isSelected()) {
            showError("You must agree to the terms");
            return false;
        }
        
        return true;
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
