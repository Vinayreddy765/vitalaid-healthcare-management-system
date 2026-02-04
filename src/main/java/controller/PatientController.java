package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import dao.*;
import service.NotificationService;
import service.RequestService; 
import view.VitalAidApp;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

/**
 * PatientController - Controls Patient Dashboard
 */
public class PatientController {
    
    @FXML private Text userName;
    @FXML private Text welcomeText;
    @FXML private Text fullNameText;
    @FXML private Text bloodGroupText;
    @FXML private Text ageText;
    @FXML private Text cityText;
    @FXML private Text emergencyContactText;
    @FXML private Label notificationBadge;
    
    @FXML private TableView<RequestRow> requestsTable;
    @FXML private TableColumn<RequestRow, Integer> requestIdColumn;
    @FXML private TableColumn<RequestRow, String> requestTypeColumn;
    @FXML private TableColumn<RequestRow, String> bloodGroupColumn;
    @FXML private TableColumn<RequestRow, String> quantityColumn;
    @FXML private TableColumn<RequestRow, String> urgencyColumn;
    @FXML private TableColumn<RequestRow, String> statusColumn;
    @FXML private TableColumn<RequestRow, String> dateColumn;
    @FXML private TableColumn<RequestRow, Button> actionColumn;
    
    @FXML private ListView<String> hospitalsList;
    
    private Patient currentPatient;
    private User currentUser;
    private final PatientDAO patientDAO = new PatientDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final NotificationService notificationService = new NotificationService();
    private final RequestService requestService = new RequestService(); 
    
    @FXML
    public void initialize() {
        System.out.println("\n========== PATIENT DASHBOARD INITIALIZATION ==========");
        
        currentUser = SessionManager.getCurrentUser();
        currentPatient = SessionManager.getCurrentPatient();
        
        if (currentUser != null) {
            System.out.println("User ID: " + currentUser.getUserId());
            System.out.println("Username: " + currentUser.getUsername());
        }
        
        if (currentPatient != null) {
            System.out.println("Patient ID: " + currentPatient.getPatientId());
            System.out.println("Patient Name: " + currentPatient.getFullName());
            
            loadPatientData();
            loadRequests();
            loadHospitals();
            loadNotifications();
        } else {
            System.err.println("ERROR: currentPatient is NULL!");
        }
        
        setupTableColumns();
        System.out.println("========== INITIALIZATION COMPLETE ==========\n");
    }
    
    private void loadPatientData() {
        userName.setText(currentPatient.getFullName());
        welcomeText.setText("Welcome, " + currentPatient.getFullName() + "!");
        
        fullNameText.setText(currentPatient.getFullName());
        bloodGroupText.setText(currentPatient.getBloodGroup().getDisplay());
        
        int age = Period.between(currentPatient.getDateOfBirth(), LocalDate.now()).getYears();
        ageText.setText(age + " years");
        
        cityText.setText(currentPatient.getCity());
        emergencyContactText.setText(currentPatient.getEmergencyContact());
    }
    
    private void loadNotifications() {
        int unreadCount = notificationService.getUnreadCount(currentUser.getUserId());
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }
    
    private void loadRequests() {
        List<Request> requests = requestDAO.getRequestsByPatient(currentPatient.getPatientId());
        ObservableList<RequestRow> rows = FXCollections.observableArrayList();
        
        for (Request req : requests) {
            rows.add(new RequestRow(req));
        }
        
        requestsTable.setItems(rows);
        System.out.println("✓ Loaded " + requests.size() + " requests");
    }
    
    /**
     * Loads hospitals from DB + Adds 10 Manual Demo Hospitals
     */
    private void loadHospitals() {
        ObservableList<String> items = FXCollections.observableArrayList();
        
        // 1. Add Hospitals from Database (Real Data)
        List<Hospital> dbHospitals = hospitalDAO.getHospitalsByCity(currentPatient.getCity());
        for (Hospital h : dbHospitals) {
            items.add("🏥 " + h.getHospitalName() + "\n   " + h.getAddress());
        }
        
        // 2. Add Manual Hospitals for Demo (Ensures Sparsha and others always appear in the dashboard list)
        items.add("🏥 Sparsha Hospital\n   #12, Narayana Health City, Bommasandra");
        items.add("🏥 Apollo Hospital\n   154/11, Bannerghatta Road");
        items.add("🏥 Manipal Hospital\n   98, HAL Airport Road");
        items.add("🏥 Fortis Hospital\n   154/9, Bannerghatta Road");
        items.add("🏥 Aster CMI Hospital\n   #43/2, NH 44, Sahakar Nagar");
        items.add("🏥 Narayana Health City\n   258/A, Bommasandra Industrial Area");
        items.add("🏥 Columbia Asia Hospital\n   26/4, Brigade Gateway");
        items.add("🏥 Sakra World Hospital\n   SY No 52/2 & 52/3, Devarabeesanahalli");
        items.add("🏥 M S Ramaiah Memorial Hospital\n   M S Ramaiah Nagar, MSRIT Post");
        items.add("🏥 Victoria Hospital\n   Fort Road, Near City Market");
        
        hospitalsList.setItems(items);
        // NOTE: item.size() reflects total (DB + Manual).
        System.out.println("✓ Loaded " + items.size() + " hospitals (Database + Manual)");
    }
    
    private void setupTableColumns() {
        requestIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getRequestId()));
        requestTypeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        bloodGroupColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBloodGroup()));
        quantityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getQuantity()));
        urgencyColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUrgency()));
        statusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        dateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        actionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getActionButton()));
    }
    
    @FXML
    private void createBloodRequest() {
        createSimpleRequest(Request.RequestType.BLOOD);
    }
    
    @FXML
    private void createPlasmaRequest() {
        createSimpleRequest(Request.RequestType.PLASMA);
    }
    
    @FXML
    private void createVentilatorRequest() {
        createSimpleRequest(Request.RequestType.VENTILATOR);
    }
    
    private void createSimpleRequest(Request.RequestType type) {
        System.out.println("\n--- Creating " + type + " Request ---");
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create " + type + " Request");
        dialog.setHeaderText("Request " + type + " - " + currentPatient.getFullName());
        
        ButtonType submitButtonType = new ButtonType("Submit Request", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<String> urgencyCombo = new ComboBox<>();
        urgencyCombo.getItems().addAll("CRITICAL", "URGENT", "NORMAL");
        urgencyCombo.setValue("NORMAL");
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in ml");
        
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for request");
        reasonArea.setPrefRowCount(3);
        
        DatePicker requiredByPicker = new DatePicker();
        requiredByPicker.setValue(LocalDate.now().plusDays(1));
        
        // --- FIX: Add Manual Hospitals to Dropdown as well ---
        ComboBox<String> hospitalCombo = new ComboBox<>();
        
        // 1. Add DB Hospitals
        List<Hospital> dbHospitals = hospitalDAO.getHospitalsByCity(currentPatient.getCity());
        for (Hospital h : dbHospitals) {
            hospitalCombo.getItems().add(h.getHospitalName());
        }
        
        // 2. Add Manual Names for Selection in the Form
        hospitalCombo.getItems().add("Sparsha Hospital");
        hospitalCombo.getItems().add("Apollo Hospital");
        hospitalCombo.getItems().add("Manipal Hospital");
        hospitalCombo.getItems().add("Fortis Hospital");
        hospitalCombo.getItems().add("Aster CMI Hospital");
        hospitalCombo.getItems().add("Narayana Health City");
        hospitalCombo.getItems().add("Columbia Asia Hospital");
        hospitalCombo.getItems().add("Sakra World Hospital");
        hospitalCombo.getItems().add("M S Ramaiah Memorial Hospital");
        hospitalCombo.getItems().add("Victoria Hospital");
        
        if (!hospitalCombo.getItems().isEmpty()) {
            hospitalCombo.setValue(hospitalCombo.getItems().get(0));
        }
        
        grid.add(new Label("Urgency:"), 0, 0);
        grid.add(urgencyCombo, 1, 0);
        
        if (type != Request.RequestType.VENTILATOR) {
            grid.add(new Label("Quantity (ml):"), 0, 1);
            grid.add(quantityField, 1, 1);
            quantityField.setText("450"); // Default blood donation amount
        }
        
        grid.add(new Label("Required By:"), 0, 2);
        grid.add(requiredByPicker, 1, 2);
        
        grid.add(new Label("Hospital:"), 0, 3);
        grid.add(hospitalCombo, 1, 3);
        
        grid.add(new Label("Reason:"), 0, 4);
        grid.add(reasonArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(() -> urgencyCombo.requestFocus());
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == submitButtonType) {
            try {
                // Validation checks...
                if (type != Request.RequestType.VENTILATOR) {
                    String qtyText = quantityField.getText().trim();
                    if (qtyText.isEmpty()) {
                        VitalAidApp.showErrorAlert("Error", "Please enter quantity");
                        return;
                    }
                    int quantity = Integer.parseInt(qtyText);
                    if (quantity <= 0 || quantity > 2000) {
                        VitalAidApp.showErrorAlert("Error", "Quantity must be between 1 and 2000 ml");
                        return;
                    }
                }
                
                if (reasonArea.getText().trim().isEmpty()) {
                    VitalAidApp.showErrorAlert("Error", "Please provide a reason");
                    return;
                }
                
                if (hospitalCombo.getValue() == null) {
                    VitalAidApp.showErrorAlert("Error", "Please select a hospital");
                    return;
                }
                
                // Create the request object
                Request request = new Request();
                request.setPatientId(currentPatient.getPatientId());
                request.setRequestType(type);
                request.setUrgency(Request.Urgency.valueOf(urgencyCombo.getValue()));
                request.setReason(reasonArea.getText().trim());
                request.setRequiredBy(requiredByPicker.getValue().atStartOfDay());
                
                // Find selected hospital ID
                Hospital selectedHospital = dbHospitals.stream()
                    .filter(h -> h.getHospitalName().equals(hospitalCombo.getValue()))
                    .findFirst()
                    .orElse(null);
                
                if (selectedHospital != null) {
                    request.setHospitalId(selectedHospital.getHospitalId());
                } else {
                    // Manual hospital selection fallback for demo
                    // If they select "Sparsha Hospital" but it's not in DB, attach to first available DB hospital
                    if (!dbHospitals.isEmpty()) {
                        request.setHospitalId(dbHospitals.get(0).getHospitalId());
                        System.out.println("ℹ Using ID of " + dbHospitals.get(0).getHospitalName() + " for manual selection.");
                    }
                }
                
                if (type != Request.RequestType.VENTILATOR) {
                    request.setBloodGroup(currentPatient.getBloodGroup());
                    request.setQuantityMl(Integer.parseInt(quantityField.getText().trim()));
                }
                
                // Use the new RequestService to submit and trigger matching
                System.out.println("Submitting request via RequestService...");
                int requestId = requestService.submitRequest(request);
                
                if (requestId > 0) {
                    System.out.println("✓ Request submitted and matching process triggered successfully. ID: " + requestId);
                    VitalAidApp.showSuccessAlert("Success", 
                        "Your " + type + " request has been submitted successfully!\n" +
                        "Request ID: " + requestId + "\n\n" +
                        "Matching donors and hospitals are being notified.");
                    loadRequests(); // Refresh the table
                } else {
                    System.err.println("✗ Failed to create request");
                    VitalAidApp.showErrorAlert("Error", "Failed to create request. Please try again.");
                }
                
            } catch (NumberFormatException e) {
                VitalAidApp.showErrorAlert("Error", "Please enter a valid quantity number");
            } catch (Exception e) {
                System.err.println("✗ Error creating request: " + e.getMessage());
                e.printStackTrace();
                VitalAidApp.showErrorAlert("Error", "Error creating request: " + e.getMessage());
            }
        } else {
            System.out.println("Request creation cancelled");
        }
    }
    
    @FXML
    private void refreshRequests() {
        loadRequests();
        VitalAidApp.showSuccessAlert("Refreshed", "Requests updated");
    }
    
    @FXML
    private void findHospitals() {
        loadHospitals();
        VitalAidApp.showSuccessAlert("Refreshed", "Hospitals list updated");
    }
    
    @FXML
    private void showNotifications() {
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getUserId());
        
        if (notifications.isEmpty()) {
            VitalAidApp.showSuccessAlert("Notifications", "No new notifications");
            return;
        }
        
        StringBuilder message = new StringBuilder();
        for (Notification notif : notifications) {
            message.append("• ").append(notif.getTitle()).append("\n")
                   .append("  ").append(notif.getMessage()).append("\n\n");
        }
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText(notifications.size() + " new notification(s)");
        alert.setContentText(message.toString());
        alert.showAndWait();
        
        notifications.forEach(n -> notificationService.markAsRead(n.getNotificationId()));
        loadNotifications();
    }
    
    @FXML
    private void handleLogout() {
        if (VitalAidApp.showConfirmDialog("Logout", "Are you sure you want to logout?")) {
            SessionManager.clearSession();
            VitalAidApp.showLoginScreen();
        }
    }
    
    /**
     * Inner class for table rows
     */
    public static class RequestRow {
        private final Request request;
        private final Button actionButton;
        
        public RequestRow(Request request) {
            this.request = request;
            this.actionButton = new Button("View");
            this.actionButton.setOnAction(e -> viewRequest());
        }
        
        private void viewRequest() {
            VitalAidApp.showSuccessAlert("Request Details", 
                "Request ID: " + request.getRequestId() + "\n" +
                "Type: " + request.getRequestType() + "\n" +
                "Status: " + request.getStatus());
        }
        
        public int getRequestId() { return request.getRequestId(); }
        public String getType() { return request.getRequestType().toString(); }
        public String getBloodGroup() { 
            return request.getBloodGroup() != null ? request.getBloodGroup().getDisplay() : "N/A";
        }
        public String getQuantity() { 
            return request.getQuantityMl() > 0 ? request.getQuantityMl() + "ml" : "N/A";
        }
        public String getUrgency() { return request.getUrgency().toString(); }
        public String getStatus() { return request.getStatus().toString(); }
        public String getDate() { return request.getCreatedAt().toString(); }
        public Button getActionButton() { return actionButton; }
    }
}