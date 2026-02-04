package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import dao.*;
import service.NotificationService;
import view.VitalAidApp;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDateTime;

/**
 * HospitalController - Controls Hospital Dashboard
 */
public class HospitalController {
    
    // Header elements
    @FXML private Text hospitalName;
    @FXML private Text welcomeText;
    @FXML private Button notificationButton;
    @FXML private Label notificationBadge;
    
    // Statistics
    @FXML private Text totalBloodText;
    @FXML private Text totalPlasmaText;
    @FXML private Text availableVentilatorsText;
    @FXML private Text pendingRequestsText;
    
    // Blood Stock Tab
    @FXML private TableView<BloodStockRow> bloodStockTable;
    @FXML private TableColumn<BloodStockRow, String> bloodGroupColumn;
    @FXML private TableColumn<BloodStockRow, String> bloodQuantityColumn;
    @FXML private TableColumn<BloodStockRow, String> bloodThresholdColumn;
    @FXML private TableColumn<BloodStockRow, String> bloodStatusColumn;
    @FXML private TableColumn<BloodStockRow, String> bloodExpiryColumn;
    @FXML private TableColumn<BloodStockRow, String> bloodLastUpdatedColumn;
    @FXML private TableColumn<BloodStockRow, Button> bloodActionColumn;
    @FXML private VBox lowStockAlertBox;
    @FXML private ListView<String> lowStockList;
    
    // Plasma Stock Tab
    @FXML private TableView<PlasmaStockRow> plasmaStockTable;
    @FXML private TableColumn<PlasmaStockRow, String> plasmaGroupColumn;
    @FXML private TableColumn<PlasmaStockRow, String> plasmaQuantityColumn;
    @FXML private TableColumn<PlasmaStockRow, String> plasmaThresholdColumn;
    @FXML private TableColumn<PlasmaStockRow, String> plasmaStatusColumn;
    @FXML private TableColumn<PlasmaStockRow, String> plasmaExpiryColumn;
    @FXML private TableColumn<PlasmaStockRow, Button> plasmaActionColumn;
    
    // Ventilators Tab
    @FXML private TableView<VentilatorRow> ventilatorsTable;
    @FXML private TableColumn<VentilatorRow, String> ventSerialColumn;
    @FXML private TableColumn<VentilatorRow, String> ventTypeColumn;
    @FXML private TableColumn<VentilatorRow, String> ventModelColumn;
    @FXML private TableColumn<VentilatorRow, String> ventStatusColumn;
    @FXML private TableColumn<VentilatorRow, String> ventLocationColumn;
    @FXML private TableColumn<VentilatorRow, String> ventMaintenanceColumn;
    @FXML private TableColumn<VentilatorRow, Button> ventActionColumn;
    
    // Requests Tab
    @FXML private ComboBox<String> requestFilterCombo;
    @FXML private TableView<RequestRow> requestsTable;
    @FXML private TableColumn<RequestRow, Integer> reqIdColumn;
    @FXML private TableColumn<RequestRow, String> reqTypeColumn;
    @FXML private TableColumn<RequestRow, String> reqPatientColumn;
    @FXML private TableColumn<RequestRow, String> reqBloodGroupColumn;
    @FXML private TableColumn<RequestRow, String> reqQuantityColumn;
    @FXML private TableColumn<RequestRow, String> reqUrgencyColumn;
    @FXML private TableColumn<RequestRow, String> reqDateColumn;
    @FXML private TableColumn<RequestRow, Button> reqActionColumn;
    
    private Hospital currentHospital;
    private User currentUser;
    
    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final VentilatorDAO ventilatorDAO = new VentilatorDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final NotificationService notificationService = new NotificationService();
    private final PatientDAO patientDAO = new PatientDAO(); // Added for request approval logic
    
    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        currentHospital = SessionManager.getCurrentHospital();
        
        if (currentHospital != null) {
            loadHospitalData();
            loadStatistics();
            loadBloodStock();
            loadPlasmaStock();
            loadVentilators();
            loadRequests();
            loadNotifications();
        }
        
        setupTables();
        setupRequestFilter();
    }
    
    /**
     * Load hospital data
     */
    private void loadHospitalData() {
        hospitalName.setText(currentHospital.getHospitalName());
        welcomeText.setText("Welcome to " + currentHospital.getHospitalName() + " Dashboard!");
    }
    
    /**
     * Load statistics
     */
    private void loadStatistics() {
        // Calculate total blood units
        List<BloodStock> bloodStocks = stockDAO.getAllBloodStock(currentHospital.getHospitalId());
        int totalBlood = bloodStocks.stream().mapToInt(BloodStock::getQuantityMl).sum();
        totalBloodText.setText(totalBlood + " ml");
        
        // Calculate total plasma units
        List<PlasmaStock> plasmaStocks = stockDAO.getAllPlasmaStock(currentHospital.getHospitalId());
        int totalPlasma = plasmaStocks.stream().mapToInt(PlasmaStock::getQuantityMl).sum();
        totalPlasmaText.setText(totalPlasma + " ml");
        
        // Get ventilator statistics
        List<Ventilator> ventilators = ventilatorDAO.getVentilatorsByHospital(currentHospital.getHospitalId());
        long available = ventilators.stream()
            .filter(v -> v.getStatus() == Ventilator.VentilatorStatus.AVAILABLE)
            .count();
        availableVentilatorsText.setText(available + "/" + ventilators.size());
        
        // Get pending requests count
        // NOTE: This currently loads ALL pending requests in the system. 
        // In a real system, it would likely load only requests assigned to or relevant to this hospital.
        List<Request> requests = requestDAO.getPendingRequests();
        pendingRequestsText.setText(String.valueOf(requests.size()));
        
        // Check for low stock
        List<BloodStock> lowStocks = stockDAO.getLowStockAlerts(currentHospital.getHospitalId());
        if (!lowStocks.isEmpty()) {
            lowStockAlertBox.setVisible(true);
            ObservableList<String> alerts = FXCollections.observableArrayList();
            for (BloodStock stock : lowStocks) {
                alerts.add("⚠️ " + stock.getBloodGroup().getDisplay() + 
                          ": " + stock.getQuantityMl() + "ml (need " + stock.getMinThreshold() + "ml)");
                
                // OPTIONAL: Send low stock email/notification here if necessary
                // notificationService.sendStockAlert(currentUser.getUserId(), stock.getBloodGroup().getDisplay(), stock.getQuantityMl(), stock.getMinThreshold());
            }
            lowStockList.setItems(alerts);
        } else {
            lowStockAlertBox.setVisible(false);
        }
    }
    
    /**
     * Load blood stock table
     */
    private void loadBloodStock() {
        // Displays Blood Stock details: Group, Quantity, Threshold, Status, Expiry
        List<BloodStock> stocks = stockDAO.getAllBloodStock(currentHospital.getHospitalId());
        ObservableList<BloodStockRow> rows = FXCollections.observableArrayList();
        
        for (BloodStock stock : stocks) {
            rows.add(new BloodStockRow(stock));
        }
        
        bloodStockTable.setItems(rows);
    }
    
    /**
     * Load plasma stock table
     */
    private void loadPlasmaStock() {
        // Displays Plasma Stock details: Group, Quantity, Threshold, Status, Expiry
        List<PlasmaStock> stocks = stockDAO.getAllPlasmaStock(currentHospital.getHospitalId());
        ObservableList<PlasmaStockRow> rows = FXCollections.observableArrayList();
        
        for (PlasmaStock stock : stocks) {
            rows.add(new PlasmaStockRow(stock));
        }
        
        plasmaStockTable.setItems(rows);
    }
    
    /**
     * Load ventilators table
     */
    private void loadVentilators() {
        // Displays Ventilator details: Serial, Type, Model, Status, Location, Maintenance
        List<Ventilator> ventilators = ventilatorDAO.getVentilatorsByHospital(currentHospital.getHospitalId());
        ObservableList<VentilatorRow> rows = FXCollections.observableArrayList();
        
        for (Ventilator vent : ventilators) {
            rows.add(new VentilatorRow(vent));
        }
        
        ventilatorsTable.setItems(rows);
    }
    
    /**
     * Load requests table
     */
    private void loadRequests() {
        // Displays Patient Requests details: ID, Type, Patient, Group, Quantity, Urgency, Date
        List<Request> requests = requestDAO.getPendingRequests();
        ObservableList<RequestRow> rows = FXCollections.observableArrayList();
        
        for (Request req : requests) {
            rows.add(new RequestRow(req));
        }
        
        requestsTable.setItems(rows);
    }
    
    /**
     * Load notifications
     */
    private void loadNotifications() {
        int unreadCount = notificationService.getUnreadCount(currentUser.getUserId());
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        }
    }
    
    /**
     * Setup table columns
     */
    private void setupTables() {
        // Blood Stock Table
        bloodGroupColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBloodGroup()));
        bloodQuantityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getQuantity()));
        bloodThresholdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getThreshold()));
        bloodStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        bloodExpiryColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getExpiry()));
        bloodLastUpdatedColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getLastUpdated()));
        bloodActionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getActionButton()));
        
        // Plasma Stock Table (Ensure all columns are set up)
        plasmaGroupColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBloodGroup()));
        plasmaQuantityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getQuantity()));
        plasmaThresholdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getThreshold()));
        plasmaStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        plasmaExpiryColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getExpiry()));
        plasmaActionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getActionButton()));

        // Ventilators Table (Ensure all columns are set up)
        ventSerialColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSerialNumber()));
        ventTypeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        ventModelColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getModel()));
        ventStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        ventLocationColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getLocation()));
        ventMaintenanceColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getMaintenance()));
        ventActionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getActionButton()));

        // Requests Table (Ensure all columns are set up)
        reqIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        reqTypeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        reqPatientColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPatient()));
        reqBloodGroupColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBloodGroup()));
        reqQuantityColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getQuantity()));
        reqUrgencyColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUrgency()));
        reqDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        reqActionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getActionButton()));
    }
    
    /**
     * Setup request filter
     */
    private void setupRequestFilter() {
        requestFilterCombo.setItems(FXCollections.observableArrayList(
            "All Requests", "Critical Only", "Urgent Only", "Normal Only"
        ));
        requestFilterCombo.setValue("All Requests");
    }
    
    // ==================== ACTION HANDLERS ====================
    
    /**
     * Handles the logic when a hospital admin clicks "Approve" on a request.
     * This method is called from the RequestRow's action button.
     */
    private void handleApproveRequest(Request request) {
        // 1. Show a confirmation dialog
        boolean confirmed = VitalAidApp.showConfirmDialog("Approve Request", 
            "Are you sure you want to approve this request?\n\n" +
            "Type: " + request.getRequestType() + "\n" +
            "Patient ID: " + request.getPatientId()
        );
        
        if (confirmed) {
            try {
                // 2. Update the request status in the database
                if (requestDAO.updateRequestStatus(request.getRequestId(), Request.RequestStatus.APPROVED)) {
                    
                    // 3. Send a notification to the patient
                    // We need to get the patient's user_id to notify them
                    Patient patient = patientDAO.getPatientById(request.getPatientId());
                    
                    if (patient != null) {
                        notificationService.sendApprovalNotification(
                            patient.getUserId(), 
                            currentHospital.getHospitalName(), 
                            request.getRequestType()
                        );
                    }
                    
                    VitalAidApp.showSuccessAlert("Request Approved", 
                        "The request has been marked as approved and the patient notified.");
                    
                    // 4. Refresh the data on screen
                    loadRequests();
                    loadStatistics();
                    
                } else {
                    VitalAidApp.showErrorAlert("Error", "Failed to update the request status in the database.");
                }
            } catch (Exception e) {
                VitalAidApp.showErrorAlert("Error", "An error occurred while approving the request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * FIX: Implemented "Add Blood Stock" functionality
     * This opens a dialog to add quantity to existing blood stock.
     */
    @FXML
    private void addBloodStock() {
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Blood Stock");
        dialog.setHeaderText("Update blood stock quantity for " + currentHospital.getHospitalName());

        ButtonType submitButtonType = new ButtonType("Add Stock", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Donor.BloodGroup> bloodGroupCombo = new ComboBox<>();
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Donor.BloodGroup.values()));
        bloodGroupCombo.setValue(Donor.BloodGroup.A_POSITIVE);
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in ml");
        quantityField.setText("450");

        grid.add(new Label("Blood Group:"), 0, 0);
        grid.add(bloodGroupCombo, 1, 0);
        grid.add(new Label("Quantity (ml):"), 0, 1);
        grid.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> bloodGroupCombo.requestFocus());

        // Process the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitButtonType) {
            try {
                Donor.BloodGroup group = bloodGroupCombo.getValue();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (quantity <= 0) {
                    VitalAidApp.showErrorAlert("Error", "Quantity must be a positive number.");
                    return;
                }

                // Call the DAO to update the stock
                if (stockDAO.updateBloodStock(currentHospital.getHospitalId(), group, quantity)) {
                    VitalAidApp.showSuccessAlert("Success", "Blood stock updated successfully.");
                    loadBloodStock(); // Refresh the table
                    loadStatistics(); // Refresh the stats
                } else {
                    VitalAidApp.showErrorAlert("Error", "Failed to update blood stock. (Stock might not exist for this group)");
                }

            } catch (NumberFormatException e) {
                VitalAidApp.showErrorAlert("Error", "Please enter a valid number for quantity.");
            }
        }
    }

    /**
     * FIX: Implemented "Add Plasma Stock" functionality
     */
    @FXML
    private void addPlasmaStock() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Plasma Stock");
        dialog.setHeaderText("Update plasma stock quantity for " + currentHospital.getHospitalName());

        ButtonType submitButtonType = new ButtonType("Add Stock", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Donor.BloodGroup> bloodGroupCombo = new ComboBox<>();
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Donor.BloodGroup.values()));
        bloodGroupCombo.setValue(Donor.BloodGroup.A_POSITIVE);
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in ml");
        quantityField.setText("200");

        grid.add(new Label("Blood Group:"), 0, 0);
        grid.add(bloodGroupCombo, 1, 0);
        grid.add(new Label("Quantity (ml):"), 0, 1);
        grid.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> bloodGroupCombo.requestFocus());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitButtonType) {
            try {
                Donor.BloodGroup group = bloodGroupCombo.getValue();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (quantity <= 0) {
                    VitalAidApp.showErrorAlert("Error", "Quantity must be a positive number.");
                    return;
                }

                if (stockDAO.updatePlasmaStock(currentHospital.getHospitalId(), group, quantity)) {
                    VitalAidApp.showSuccessAlert("Success", "Plasma stock updated successfully.");
                    loadPlasmaStock(); // Refresh
                    loadStatistics(); // Refresh
                } else {
                    VitalAidApp.showErrorAlert("Error", "Failed to update plasma stock. (Stock might not exist for this group)");
                }

            } catch (NumberFormatException e) {
                VitalAidApp.showErrorAlert("Error", "Please enter a valid number for quantity.");
            }
        }
    }
    
    /**
     * FIX: Implemented "Add Ventilator" functionality
     */
    @FXML
    private void addVentilator() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Ventilator");
        dialog.setHeaderText("Enter details for the new ventilator unit.");

        ButtonType submitButtonType = new ButtonType("Add Unit", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Ventilator.VentilatorType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(Ventilator.VentilatorType.values()));
        typeCombo.setValue(Ventilator.VentilatorType.INVASIVE);
        
        TextField modelField = new TextField();
        modelField.setPromptText("e.g., Medtronic PB980");
        
        TextField serialField = new TextField();
        serialField.setPromptText("Unique Serial Number");
        
        TextField locationField = new TextField();
        locationField.setPromptText("e.g., ICU - Ward A");

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Model:"), 0, 1);
        grid.add(modelField, 1, 1);
        grid.add(new Label("Serial Number:"), 0, 2);
        grid.add(serialField, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(locationField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> typeCombo.requestFocus());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitButtonType) {
            String serial = serialField.getText().trim();
            if (serial.isEmpty()) {
                VitalAidApp.showErrorAlert("Error", "Serial Number is required.");
                return;
            }

            Ventilator vent = new Ventilator();
            vent.setHospitalId(currentHospital.getHospitalId());
            vent.setVentilatorType(typeCombo.getValue());
            vent.setModelName(modelField.getText().trim());
            vent.setSerialNumber(serial);
            vent.setLocationInHospital(locationField.getText().trim());
            vent.setStatus(Ventilator.VentilatorStatus.AVAILABLE); // Default to AVAILABLE

            if (ventilatorDAO.addVentilator(vent) > 0) {
                VitalAidApp.showSuccessAlert("Success", "New ventilator added successfully.");
                loadVentilators(); // Refresh
                loadStatistics(); // Refresh
            } else {
                VitalAidApp.showErrorAlert("Error", "Failed to add ventilator. Serial number might already exist or a database error occurred.");
            }
        }
    }
    
    @FXML
    private void updateBloodStock() {
        VitalAidApp.showSuccessAlert("Coming Soon", "Update blood stock feature coming soon!");
    }
    
    @FXML
    private void refreshBloodStock() {
        loadBloodStock();
        loadStatistics();
        VitalAidApp.showSuccessAlert("Refreshed", "Blood stock updated");
    }
    
    @FXML
    private void updatePlasmaStock() {
        VitalAidApp.showSuccessAlert("Coming Soon", "Update plasma stock feature coming soon!");
    }
    
    @FXML
    private void updateVentilatorStatus() {
        VitalAidApp.showSuccessAlert("Coming Soon", "Update ventilator status feature coming soon!");
    }
    
    @FXML
    private void refreshRequests() {
        loadRequests();
        VitalAidApp.showSuccessAlert("Refreshed", "Requests updated");
    }
    
    @FXML
    private void generateInventoryReport() {
        VitalAidApp.showSuccessAlert("Report", "Inventory report generated!");
    }
    
    @FXML
    private void generateRequestReport() {
        VitalAidApp.showSuccessAlert("Report", "Request summary generated!");
    }
    
    @FXML
    private void generateMonthlyReport() {
        VitalAidApp.showSuccessAlert("Report", "Monthly statistics generated!");
    }
    
    @FXML
    private void exportData() {
        VitalAidApp.showSuccessAlert("Export", "Data exported successfully!");
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
    
    // ==================== TABLE ROW CLASSES ====================
    
    public static class BloodStockRow {
        private final BloodStock stock;
        private final Button actionButton;
        
        public BloodStockRow(BloodStock stock) {
            this.stock = stock;
            this.actionButton = new Button("Update");
            this.actionButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        }
        
        public String getBloodGroup() { return stock.getBloodGroup().getDisplay(); }
        public String getQuantity() { return stock.getQuantityMl() + " ml"; }
        public String getThreshold() { return stock.getMinThreshold() + " ml"; }
        public String getStatus() { 
            return stock.isBelowThreshold() ? "⚠️ LOW" : "✓ OK"; 
        }
        public String getExpiry() { 
            return stock.getExpiryDate() != null ? stock.getExpiryDate().toString() : "N/A"; 
        }
        public String getLastUpdated() { 
            // NOTE: stock.getLastUpdated() returns LocalDateTime, which toString() is fine for display
            return stock.getLastUpdated() != null ? stock.getLastUpdated().toString() : "N/A"; 
        }
        public Button getActionButton() { return actionButton; }
    }
    
    public static class PlasmaStockRow {
        private final PlasmaStock stock;
        private final Button actionButton;
        
        public PlasmaStockRow(PlasmaStock stock) {
            this.stock = stock;
            this.actionButton = new Button("Update");
            this.actionButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        }
        
        public String getBloodGroup() { return stock.getBloodGroup().getDisplay(); }
        public String getQuantity() { return stock.getQuantityMl() + " ml"; }
        public String getThreshold() { return stock.getMinThreshold() + " ml"; }
        public String getStatus() { 
            return stock.isBelowThreshold() ? "⚠️ LOW" : "✓ OK"; 
        }
        public String getExpiry() { 
            return stock.getExpiryDate() != null ? stock.getExpiryDate().toString() : "N/A"; 
        }
        public Button getActionButton() { return actionButton; }
    }
    
    public static class VentilatorRow {
        private final Ventilator ventilator;
        private final Button actionButton;
        
        public VentilatorRow(Ventilator ventilator) {
            this.ventilator = ventilator;
            this.actionButton = new Button("Update Status");
            this.actionButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        }
        
        public String getSerialNumber() { return ventilator.getSerialNumber(); }
        public String getType() { return ventilator.getVentilatorType().toString(); }
        public String getModel() { return ventilator.getModelName(); }
        public String getStatus() { return ventilator.getStatus().toString(); }
        public String getLocation() { return ventilator.getLocationInHospital(); }
        public String getMaintenance() { 
            return ventilator.getNextMaintenanceDate() != null ? 
                ventilator.getNextMaintenanceDate().toString() : "N/A"; 
        }
        public Button getActionButton() { return actionButton; }
    }
    
    public class RequestRow {
        private final Request request;
        private final Button actionButton;
        private final PatientDAO patientDAO = new PatientDAO(); // Need this to fetch patient name

        public RequestRow(Request request) {
            this.request = request;
            this.actionButton = new Button("Approve");
            this.actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            // --- FIX: Added action handler ---
            this.actionButton.setOnAction(e -> HospitalController.this.handleApproveRequest(this.request));
        }
        
        public int getId() { return request.getRequestId(); }
        public String getType() { return request.getRequestType().toString(); }

        // --- FIX: Added missing getters back ---
        public String getPatient() {
            // Fetch patient name for better display
            Patient patient = patientDAO.getPatientById(request.getPatientId());
            return patient != null ? patient.getFullName() : "Patient #" + request.getPatientId();
        }

        public String getBloodGroup() {
            return request.getBloodGroup() != null ? request.getBloodGroup().getDisplay() : "N/A";
        }

        public String getQuantity() {
            return request.getQuantityMl() > 0 ? request.getQuantityMl() + " ml" : "N/A";
        }

        public String getUrgency() {
            return request.getUrgency().toString();
        }
        // --- End Fix ---

        public String getDate() {
            // Handle potential null createdAt
            return request.getCreatedAt() != null ? request.getCreatedAt().toString() : "N/A";
        }

        public Button getActionButton() { return actionButton; }
    }
}