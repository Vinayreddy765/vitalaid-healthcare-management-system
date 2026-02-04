package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request Model - Blood/Plasma/Ventilator request entity
 */
public class Request {
    private int requestId;
    private int patientId;
    private RequestType requestType;
    private Donor.BloodGroup bloodGroup;
    private int quantityMl;
    private Urgency urgency;
    private LocalDateTime requiredBy;
    private Integer hospitalId;
    private RequestStatus status;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum RequestType {
        BLOOD, PLASMA, VENTILATOR
    }
    
    public enum Urgency {
        CRITICAL, URGENT, NORMAL
    }
    
    public enum RequestStatus {
        PENDING, APPROVED, FULFILLED, REJECTED, CANCELLED
    }
    
    // Constructors
    public Request() {}
    
    public Request(int patientId, RequestType requestType, Urgency urgency, LocalDateTime requiredBy) {
        this.patientId = patientId;
        this.requestType = requestType;
        this.urgency = urgency;
        this.requiredBy = requiredBy;
        this.status = RequestStatus.PENDING;
    }
    
    // Getters and Setters
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }
    
    public Donor.BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(Donor.BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public int getQuantityMl() { return quantityMl; }
    public void setQuantityMl(int quantityMl) { this.quantityMl = quantityMl; }
    
    public Urgency getUrgency() { return urgency; }
    public void setUrgency(Urgency urgency) { this.urgency = urgency; }
    
    public LocalDateTime getRequiredBy() { return requiredBy; }
    public void setRequiredBy(LocalDateTime requiredBy) { this.requiredBy = requiredBy; }
    
    public Integer getHospitalId() { return hospitalId; }
    public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + requestId +
                ", type=" + requestType +
                ", urgency=" + urgency +
                ", status=" + status +
                '}';
    }
}