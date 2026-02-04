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
public class BloodStock {
    private int stockId;
    private int hospitalId;
    private Donor.BloodGroup bloodGroup;
    private int quantityMl;
    private LocalDateTime lastUpdated;
    private LocalDate expiryDate;
    private int minThreshold;
    
    // Constructors
    public BloodStock() {}
    
    public BloodStock(int hospitalId, Donor.BloodGroup bloodGroup, int quantityMl) {
        this.hospitalId = hospitalId;
        this.bloodGroup = bloodGroup;
        this.quantityMl = quantityMl;
        this.minThreshold = 500; // Default minimum threshold
    }
    
    // Getters and Setters
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }
    
    public Donor.BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(Donor.BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public int getQuantityMl() { return quantityMl; }
    public void setQuantityMl(int quantityMl) { this.quantityMl = quantityMl; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }
    
    /**
     * Check if stock is below minimum threshold
     */
    public boolean isBelowThreshold() {
        return quantityMl < minThreshold;
    }
    
    /**
     * Check if stock is expired or expiring soon (within 7 days)
     */
    public boolean isExpiring() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now().plusDays(7));
    }
    
    @Override
    public String toString() {
        return "BloodStock{" +
                "bloodGroup=" + bloodGroup.getDisplay() +
                ", quantity=" + quantityMl + "ml" +
                ", threshold=" + minThreshold + "ml" +
                '}';
    }
}
