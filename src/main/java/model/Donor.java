package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
public class Donor {
    private int donorId;
    private int userId;
    private String fullName;
    private BloodGroup bloodGroup;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private double latitude;
    private double longitude;
    private double weight;
    private LocalDate lastDonationDate;
    private boolean isAvailable;
    private String medicalConditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum BloodGroup {
        A_POSITIVE("A+"), A_NEGATIVE("A-"),
        B_POSITIVE("B+"), B_NEGATIVE("B-"),
        AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"),
        O_POSITIVE("O+"), O_NEGATIVE("O-");
        
        private final String display;
        BloodGroup(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
     // Constructors
    public Donor() {}
    
    public Donor(int userId, String fullName, BloodGroup bloodGroup, LocalDate dateOfBirth, Gender gender) {
        this.userId = userId;
        this.fullName = fullName;
        this.bloodGroup = bloodGroup;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.isAvailable = true;
    }
    
    // Getters and Setters
    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Check if donor is eligible to donate based on last donation date
     * Blood donation eligibility: 90 days (3 months)
     * Plasma donation eligibility: 14 days (2 weeks)
     */
    public boolean isEligibleForBloodDonation() {
        if (lastDonationDate == null) return true;
        return LocalDate.now().isAfter(lastDonationDate.plusDays(90));
    }
    
    public boolean isEligibleForPlasmaDonation() {
        if (lastDonationDate == null) return true;
        return LocalDate.now().isAfter(lastDonationDate.plusDays(14));
    }
    
    @Override
    public String toString() {
        return "Donor{" +
                "donorId=" + donorId +
                ", fullName='" + fullName + '\'' +
                ", bloodGroup=" + bloodGroup.getDisplay() +
                ", city='" + city + '\'' +
                '}';
    }
}