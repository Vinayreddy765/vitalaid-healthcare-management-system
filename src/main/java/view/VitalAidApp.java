package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import config.DatabaseConfig;

/**
 * VitalAidApp - JavaFX Main Application
 * Entry point for the GUI-based VitalAid Management System
 */
public class VitalAidApp extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Test database connection
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        if (!dbConfig.testConnection()) {
            showErrorAlert("Database Connection Failed", 
                "Could not connect to database. Please check configuration.");
            return;
        }
        
        System.out.println("✓ Database connected successfully");
        
        // Load the project front page screen
        showProjectFrontPage(); 
        
        // Configure primary stage
        primaryStage.setTitle("Vital Aid Unified Blood, Plasma And Ventilator Management System");
        // FIX: Ensure resizing is TRUE so the maximize button appears
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    /**
     * NEW: Method to show the initial project front page
     */
    public static void showProjectFrontPage() {
        // FIX: Pass the original, fixed size (1000, 700) to keep the FXML layout visually correct.
        loadScene("/fxml/ProjectFrontPage.fxml", 1000, 700); 
        
        // FIX: Force the stage to open maximized, overriding the initial size.
        primaryStage.setMaximized(true); 
    }

    /**
     * Show login screen
     */
    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                VitalAidApp.class.getResource("/fxml/Login.fxml")
            );
            Parent root = loader.load();
            
            // FIX: Explicitly UN-MAXIMIZE the stage to respect the Login scene's fixed size
            primaryStage.setMaximized(false); 
            primaryStage.setWidth(1000); // Set fixed width based on FXML design
            primaryStage.setHeight(700); // Set fixed height based on FXML design
            primaryStage.centerOnScreen(); // Center the non-maximized window
            
            Scene scene = new Scene(root, 1000, 700); // Scene size is set here
            scene.getStylesheets().add(
                VitalAidApp.class.getResource("/css/style.css").toExternalForm()
            );
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("VitalAid - Login"); // Update title for Login screen
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load login screen: " + e.getMessage());
        }
    }
    
    /**
     * Show donor dashboard
     */
    public static void showDonorDashboard() {
        // Dashboards can be set to a large default size (1200x800) and then maximized
        loadScene("/fxml/DonorDashboard.fxml", 1200, 800); 
        primaryStage.setMaximized(true); // Force maximize
    }
    
    /**
     * Show patient dashboard
     */
    public static void showPatientDashboard() {
        loadScene("/fxml/PatientDashboard.fxml", 1200, 800);
        primaryStage.setMaximized(true);
    }
    
    /**
     * Show hospital dashboard
     */
    public static void showHospitalDashboard() {
        loadScene("/fxml/HospitalDashboard.fxml", 1400, 900);
        primaryStage.setMaximized(true);
    }
    
    /**
     * Show admin dashboard
     */
    public static void showAdminDashboard() {
        loadScene("/fxml/AdminDashboard.fxml", 1400, 900);
        primaryStage.setMaximized(true);
    }
    
    /**
     * Load scene from FXML file
     */
    private static void loadScene(String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(VitalAidApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Use width/height parameters explicitly for the scene size
            Scene scene = new Scene(root, width, height); 
            
            // Only add stylesheet if it exists and is not the Front Page
            if (!fxmlPath.contains("ProjectFrontPage")) {
                 scene.getStylesheets().add(
                    VitalAidApp.class.getResource("/css/style.css").toExternalForm()
                );
            }
            
            primaryStage.setScene(scene);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load screen: " + e.getMessage());
        }
    }
    
    /**
     * Show error alert dialog
     */
    public static void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show success alert dialog
     */
    public static void showSuccessAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().get() == javafx.scene.control.ButtonType.OK;
    }
    
    /**
     * Get primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}