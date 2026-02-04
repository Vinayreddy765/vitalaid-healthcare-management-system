package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import dao.*;
import service.NotificationService;
import service.DonorMatchingService;
import view.VitalAidApp;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority; 

/**
 * DonorController - Controls Donor Dashboard
 */
public class DonorController {
    
    @FXML private Text userName;
    @FXML private Text welcomeText;
    @FXML private Text bloodGroupText;
    @FXML private Text bloodGroupProfileText;
    @FXML private Text totalDonationsText;
    @FXML private Text lastDonationText;
    @FXML private Text availabilityText;
    @FXML private ToggleButton availabilityToggle;
    @FXML private ListView<NotificationDisplay> requestsList;
    @FXML private ListView<String> hospitalsList;
    @FXML private TableView<DonationRecord> historyTable;
    @FXML private TableColumn<DonationRecord, String> dateColumn;
    @FXML private TableColumn<DonationRecord, String> typeColumn;
    @FXML private TableColumn<DonationRecord, String> quantityColumn;
    @FXML private TableColumn<DonationRecord, String> hospitalColumn;
    
    // Profile fields
    @FXML private Text fullNameText;
    @FXML private Text ageText;
    @FXML private Text cityText;
    @FXML private Text weightText;
    @FXML private Text phoneText;
    @FXML private Label notificationBadge;
    
    private Donor currentDonor;
    private User currentUser;
    private final DonorDAO donorDAO = new DonorDAO();
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final NotificationService notificationService = new NotificationService();
    private final DonorMatchingService donorMatchingService = new DonorMatchingService();
    
    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        currentDonor = SessionManager.getCurrentDonor();
        
        if (currentDonor != null) {
            loadDonorData();
            loadNotifications();
            loadDonationHistory();
            loadNearbyHospitals();
            loadMatchingRequests();
        }
        
        setupTableColumns();
    }
    
    private void loadDonorData() {
        userName.setText(currentDonor.getFullName());
        welcomeText.setText("Welcome back, " + currentDonor.getFullName() + "!");
        
        bloodGroupText.setText(currentDonor.getBloodGroup().getDisplay());
        bloodGroupProfileText.setText(currentDonor.getBloodGroup().getDisplay());
        
        fullNameText.setText(currentDonor.getFullName());
        
        int age = Period.between(currentDonor.getDateOfBirth(), LocalDate.now()).getYears();
        ageText.setText(age + " years");
        
        cityText.setText(currentDonor.getCity());
        weightText.setText(String.format("%.1f kg", currentDonor.getWeight()));
        phoneText.setText(currentUser.getPhone());
        
        availabilityToggle.setSelected(currentDonor.isAvailable());
        updateAvailabilityText();
        
        if (currentDonor.getLastDonationDate() != null) {
            lastDonationText.setText(currentDonor.getLastDonationDate().toString());
        } else {
            lastDonationText.setText("Never donated");
        }
        
        totalDonationsText.setText("0");
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
    
    private void loadDonationHistory() {
        ObservableList<DonationRecord> history = FXCollections.observableArrayList();
        historyTable.setItems(history);
    }
    
    private void loadNearbyHospitals() {
        List<Hospital> hospitals = hospitalDAO.getHospitalsByCity(currentDonor.getCity());
        
        ObservableList<String> hospitalItems = FXCollections.observableArrayList();
        
        for (Hospital hospital : hospitals) {
            hospitalItems.add("🏥 " + hospital.getHospitalName() + "\n   " + hospital.getAddress());
        }
        
        hospitalItems.add("🏥 Aster CMI Hospital\n   NH 44, Hebbal, Bengaluru");
        hospitalItems.add("🏥 Narayana Health City\n   258/A, Bommasandra, Bengaluru");
        
        hospitalsList.setItems(hospitalItems);
    }
    
    private void loadMatchingRequests() {
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getUserId());
        
        ObservableList<NotificationDisplay> requests = notifications.stream()
            .filter(n -> n.getNotificationType() == Notification.NotificationType.MATCH)
            .map(n -> new NotificationDisplay(n, this)) 
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
        requestsList.setItems(requests);
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        typeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        quantityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getQuantity()));
        hospitalColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getHospital()));
            
        requestsList.setCellFactory(lv -> new ListCell<NotificationDisplay>() {
            private final HBox graphicBox = new HBox(10);
            private final Label messageLabel = new Label();
            private final Button acceptButton = new Button("Accept");
            private final Button rejectButton = new Button("Reject");
            
            {
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(messageLabel, Priority.ALWAYS);
                
                acceptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                rejectButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                
                graphicBox.getChildren().addAll(messageLabel, acceptButton, rejectButton);
                graphicBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }
            
            @Override
            protected void updateItem(NotificationDisplay item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    messageLabel.setText(item.getDisplayMessage());
                    
                    acceptButton.setOnAction(e -> item.handleAccept());
                    rejectButton.setOnAction(e -> item.handleReject());
                    
                    setGraphic(graphicBox);
                    setStyle(item.getPriorityColor());
                }
            }
        });
    }
    
    @FXML
    private void toggleAvailability() {
        boolean newStatus = availabilityToggle.isSelected();
        
        if (donorDAO.updateAvailability(currentDonor.getDonorId(), newStatus)) {
            currentDonor.setAvailable(newStatus);
            updateAvailabilityText();
            VitalAidApp.showSuccessAlert("Success", 
                "Availability status updated to: " + (newStatus ? "Available" : "Not Available"));
        } else {
            availabilityToggle.setSelected(!newStatus);
            VitalAidApp.showErrorAlert("Error", "Failed to update availability status");
        }
    }
    
    private void updateAvailabilityText() {
        if (currentDonor.isAvailable()) {
            availabilityText.setText("✓ Available");
            availabilityText.setStyle("-fx-fill: #4CAF50;");
        } else {
            availabilityText.setText("✗ Not Available");
            availabilityText.setStyle("-fx-fill: #F44336;");
        }
    }
    
    public void acceptDonationRequest(Notification notification) {
        if (notification.getRelatedEntityId() == null) {
            VitalAidApp.showErrorAlert("Error", "Request ID is missing.");
            return;
        }

        int requestId = notification.getRelatedEntityId();
        // FIX: Pass the Donor's primary key ID to update the donor_matches table
        int donorId = currentDonor.getDonorId();
        
        if (donorMatchingService.recordDonorResponse(requestId, donorId, "ACCEPTED")) {
            
            notificationService.markAsRead(notification.getNotificationId());
            
            VitalAidApp.showSuccessAlert("Success!", 
                "Thank you for accepting! The patient's hospital has been notified and will contact you shortly.\nRequest ID: " + requestId);
            
            loadNotifications();
            loadMatchingRequests();
        } else {
            VitalAidApp.showErrorAlert("Error", "Failed to register acceptance. Please try again.");
        }
    }
    
    public void rejectDonationRequest(Notification notification) {
         if (notification.getRelatedEntityId() == null) {
            VitalAidApp.showErrorAlert("Error", "Request ID is missing.");
            return;
        }

        int requestId = notification.getRelatedEntityId();
        // FIX: Pass the Donor's primary key ID
        int donorId = currentDonor.getDonorId();
        
        if (donorMatchingService.recordDonorResponse(requestId, donorId, "REJECTED")) {
             notificationService.markAsRead(notification.getNotificationId());
             loadNotifications();
             loadMatchingRequests();
             VitalAidApp.showSuccessAlert("Request Rejected", "The request has been marked as rejected.");
        } else {
             VitalAidApp.showErrorAlert("Error", "Failed to register rejection. Please try again.");
        }
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
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("You have " + notifications.size() + " new notification(s)");
        alert.setContentText(message.toString());
        
        TextArea textArea = new TextArea(message.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);
        
        alert.showAndWait();
        
        for (Notification notif : notifications) {
            notificationService.markAsRead(notif.getNotificationId());
        }
        
        loadNotifications();
        loadMatchingRequests();
    }
    
    @FXML
    private void viewAllRequests() {
        VitalAidApp.showSuccessAlert("Coming Soon", 
            "Full requests view will be available in the next update");
    }
    
    @FXML
    private void refreshRequests() {
        loadMatchingRequests();
        VitalAidApp.showSuccessAlert("Refreshed", "Donation requests updated");
    }
    
    @FXML
    private void downloadReport() {
        VitalAidApp.showSuccessAlert("Coming Soon", 
            "Report download feature will be available in the next update");
    }
    
    @FXML
    private void editProfile() {
        VitalAidApp.showSuccessAlert("Coming Soon", 
            "Profile editing will be available in the next update");
    }
    
    @FXML
    private void findHospitals() {
        loadNearbyHospitals();
        VitalAidApp.showSuccessAlert("Refreshed", "Hospitals list updated");
    }
    
    @FXML
    private void showProfile() {
        VitalAidApp.showSuccessAlert("Profile", "Profile view coming soon");
    }
    
    @FXML
    private void showSettings() {
        VitalAidApp.showSuccessAlert("Settings", "Settings page coming soon");
    }
    
    @FXML
    private void handleLogout() {
        if (VitalAidApp.showConfirmDialog("Logout", "Are you sure you want to logout?")) {
            SessionManager.clearSession();
            VitalAidApp.showLoginScreen();
        }
    }
    
    public static class DonationRecord {
        private final String date;
        private final String type;
        private final String quantity;
        private final String hospital;
        
        public DonationRecord(String date, String type, String quantity, String hospital) {
            this.date = date;
            this.type = type;
            this.quantity = quantity;
            this.hospital = hospital;
        }
        
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getQuantity() { return quantity; }
        public String getHospital() { return hospital; }
    }
    
    public static class NotificationDisplay {
        private final Notification notification;
        private final DonorController controller;
        
        public NotificationDisplay(Notification notification, DonorController controller) {
            this.notification = notification;
            this.controller = controller;
        }
        
        public String getDisplayMessage() {
            return notification.getTitle() + "\n" + 
                   "   " + notification.getMessage();
        }
        
        public String getPriorityColor() {
            switch (notification.getPriority()) {
                case HIGH:
                    return "-fx-background-color: #fce4ec; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-font-weight: bold;";
                case MEDIUM:
                    return "-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-width: 1px;";
                case LOW:
                default:
                    return "";
            }
        }
        
        public Notification getNotification() {
            return notification;
        }
        
        public void handleAccept() {
            controller.acceptDonationRequest(notification);
        }
        
        public void handleReject() {
             controller.rejectDonationRequest(notification);
        }
    }
}

class ToggleSwitch extends ToggleButton {
    public ToggleSwitch() {
        super();
        getStyleClass().add("toggle-switch");
        
        selectedProperty().addListener((obs, oldVal, newVal) -> {
            setText(newVal ? "ON" : "OFF");
        });
        
        setText("OFF");
    }
}