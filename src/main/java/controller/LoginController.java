package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import dao.UserDAO;
import dao.DonorDAO;
import dao.PatientDAO;
import dao.HospitalDAO;
import model.User;
import model.Donor;
import model.Patient;
import model.Hospital;
import view.VitalAidApp;
import javafx.concurrent.Task; // <-- ADD THIS
import javafx.application.Platform; // <-- ADD THIS (Needed if you update UI mid-process, though Task handles it mostly)
// ... (rest of your imports)
/**
 * LoginController - Handles user authentication and registration navigation
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    
    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    
    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Add enter key listener
        passwordField.setOnAction(event -> handleLogin());
        usernameField.setOnAction(event -> passwordField.requestFocus());
    }
    

    
    /**
     * Handle login button click
     */
   @FXML
private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
        showError("Please enter both username and password");
        return;
    }
    
    // 1. Disable UI elements
    loginButton.setDisable(true);
    errorLabel.setVisible(false);
    
    // 2. Create the Background Task
    Task<User> loginTask = new Task<>() {
        @Override
        protected User call() throws Exception {
            // ALL DAO and blocking calls run here in the background thread

            // Authenticate user
            User user = userDAO.authenticate(username, password);

            if (user != null) {
                // Load user-specific data based on type
                switch (user.getUserType()) {
                    case DONOR:
                        Donor donor = donorDAO.getDonorByUserId(user.getUserId());
                        SessionManager.setCurrentDonor(donor);
                        break;
                    case PATIENT:
                        Patient patient = patientDAO.getPatientByUserId(user.getUserId());
                        SessionManager.setCurrentPatient(patient);
                        break;
                    case HOSPITAL:
                        Hospital hospital = hospitalDAO.getHospitalByUserId(user.getUserId());
                        SessionManager.setCurrentHospital(hospital);
                        break;
                    case ADMIN:
                        // Admin doesn't need special object loading
                        break;
                }
            }
            return user; // Return the authenticated User object (or null)
        }
    };
    
    // 3. Define what happens when the Task SUCCEEDS (runs on JavaFX UI Thread)
    loginTask.setOnSucceeded(e -> {
        // Retrieve result from the background task
        User user = loginTask.getValue();
        
        if (user != null) {
            // Set main session manager and navigate
            SessionManager.setCurrentUser(user);
            System.out.println("✓ Login successful: " + username + " (" + user.getUserType() + ")");

            switch (user.getUserType()) {
                case DONOR:
                    VitalAidApp.showDonorDashboard();
                    break;
                case PATIENT:
                    VitalAidApp.showPatientDashboard();
                    break;
                case HOSPITAL:
                    VitalAidApp.showHospitalDashboard();
                    break;
                case ADMIN:
                    VitalAidApp.showAdminDashboard();
                    break;
            }
        } else {
            // Login failed: re-enable button and show error
            showError("Invalid username or password");
            loginButton.setDisable(false);
            passwordField.clear();
            passwordField.requestFocus();
        }
    });

    // 4. Define what happens if the Task FAILS (e.g., a SQL connection error)
    loginTask.setOnFailed(e -> {
        showError("Authentication failed due to system error.");
        loginButton.setDisable(false);
        e.getSource().getException().printStackTrace();
    });

    // 5. Start the Task in a new background thread
    new Thread(loginTask).start(); 
}
    /**
     * Show donor registration dialog
     */
    @FXML
    private void showDonorRegistration() {
        showRegistrationDialog("/fxml/DonorRegistration.fxml", "Donor Registration");
    }
    
    /**
     * Show patient registration dialog
     */
    @FXML
    private void showPatientRegistration() {
        showRegistrationDialog("/fxml/PatentRegistration.fxml", "Patient Registration");
    }
    
    /**
     * Show hospital registration dialog
     */
    @FXML
    private void showHospitalRegistration() {
        showRegistrationDialog("/fxml/HospitalRegistration.fxml", "Hospital Registration");
    }
    
    /**
     * Show registration dialog
     */
    private void showRegistrationDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(VitalAidApp.getPrimaryStage());
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            VitalAidApp.showErrorAlert("Error", "Failed to open registration form: " + e.getMessage());
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
    }
}

/**
 * SessionManager - Manages current user session
 */
