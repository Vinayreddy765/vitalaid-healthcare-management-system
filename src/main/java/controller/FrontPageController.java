package controller;

import javafx.fxml.FXML;
import view.VitalAidApp;

/**
 * FrontPageController - Handles the initial project screen
 */
public class FrontPageController {
    
    @FXML
    public void initialize() {
        // Initialization logic if needed (e.g., loading data)
    }
    
    /**
     * Action handler for the "Launch Project" button.
     * Switches the scene from the front page to the login screen.
     */
    @FXML
    private void launchProject() {
        VitalAidApp.showLoginScreen();
    }
}