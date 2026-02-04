/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Vinay Reddy
 */
public class Ventilator {
    private int ventilatorId;
    private int hospitalId;
    private VentilatorType ventilatorType;
    private String modelName;
    private String serialNumber;
    private VentilatorStatus status;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private String locationInHospital;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum VentilatorType {
        INVASIVE, NON_INVASIVE, TRANSPORT
    }
    
    public enum VentilatorStatus {
        AVAILABLE, IN_USE, MAINTENANCE, DAMAGED
    }
    
    // Constructors
    public Ventilator() {}
    
    public Ventilator(int hospitalId, VentilatorType type, String serialNumber) {
        this.hospitalId = hospitalId;
        this.ventilatorType = type;
        this.serialNumber = serialNumber;
        this.status = VentilatorStatus.AVAILABLE;
    }
    
    // Getters and Setters
    public int getVentilatorId() { return ventilatorId; }
    public void setVentilatorId(int ventilatorId) { this.ventilatorId = ventilatorId; }
    
    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }
    
    public VentilatorType getVentilatorType() { return ventilatorType; }
    public void setVentilatorType(VentilatorType ventilatorType) { this.ventilatorType = ventilatorType; }
    
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public VentilatorStatus getStatus() { return status; }
    public void setStatus(VentilatorStatus status) { this.status = status; }
    
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
    
    public String getLocationInHospital() { return locationInHospital; }
    public void setLocationInHospital(String locationInHospital) { this.locationInHospital = locationInHospital; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Check if ventilator needs maintenance
     */
    public boolean needsMaintenance() {
        if (nextMaintenanceDate == null) return false;
        return LocalDate.now().isAfter(nextMaintenanceDate.minusDays(7));
    }
    
    @Override
    public String toString() {
        return "Ventilator{" +
                "serialNumber='" + serialNumber + '\'' +
                ", type=" + ventilatorType +
                ", status=" + status +
                '}';
    }
}