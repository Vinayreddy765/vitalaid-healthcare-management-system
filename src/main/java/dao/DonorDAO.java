package dao;

import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {
    
    private final DatabaseConfig dbConfig;
    
    public DonorDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Register new donor with profile information
     * @param donor Donor object with registration details
     * @return Generated donor ID, or -1 if registration fails
     */
    public int registerDonor(Donor donor) {
        String sql = "INSERT INTO donors (user_id, full_name, blood_group, date_of_birth, gender, " +
                     "address, city, state, pincode, latitude, longitude, weight, is_available) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, donor.getUserId());
            pstmt.setString(2, donor.getFullName());
            
            // FIX: Use getDisplay() which returns the short symbol (e.g., "O+")
            pstmt.setString(3, donor.getBloodGroup().getDisplay()); 
            
            pstmt.setDate(4, Date.valueOf(donor.getDateOfBirth()));
            pstmt.setString(5, donor.getGender().name());
            pstmt.setString(6, donor.getAddress());
            pstmt.setString(7, donor.getCity());
            pstmt.setString(8, donor.getState());
            pstmt.setString(9, donor.getPincode());
            pstmt.setDouble(10, donor.getLatitude());
            pstmt.setDouble(11, donor.getLongitude());
            pstmt.setDouble(12, donor.getWeight());
            pstmt.setBoolean(13, donor.isAvailable());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int donorId = rs.getInt(1);
                    System.out.println("✓ Donor registered with ID: " + donorId);
                    return donorId;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to register donor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get donor by user ID
     */
    public Donor getDonorByUserId(int userId) {
        String sql = "SELECT * FROM donors WHERE user_id = ?";
        
        System.out.println("\n--- DonorDAO.getDonorByUserId() ---");
        System.out.println("Searching for donor with user_id: " + userId);
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            System.out.println("Executing query: " + sql);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✓ Donor record found in database");
                Donor donor = extractDonorFromResultSet(rs);
                System.out.println("  Donor ID: " + donor.getDonorId());
                System.out.println("  Full Name: " + donor.getFullName());
                System.out.println("  Blood Group: " + donor.getBloodGroup().getDisplay());
                System.out.println("  City: " + donor.getCity());
                System.out.println("--- End getDonorByUserId() ---\n");
                return donor;
            } else {
                System.err.println("✗ No donor record found for user_id: " + userId);
                System.out.println("--- End getDonorByUserId() ---\n");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error in getDonorByUserId(): " + e.getMessage());
            e.printStackTrace();
            System.out.println("--- End getDonorByUserId() ---\n");
        }
        
        return null;
    }
    
    /**
     * Update all core profile fields for a donor, including address and location.
     * @param donor The Donor object containing the updated fields.
     * @return true if the update was successful.
     */
    public boolean updateDonorProfile(Donor donor) {
        String sql = "UPDATE donors SET " +
                     "full_name = ?, address = ?, city = ?, state = ?, pincode = ?, " +
                     "latitude = ?, longitude = ?, weight = ?, is_available = ?, " +
                     "medical_conditions = ?, updated_at = NOW() " +
                     "WHERE donor_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, donor.getFullName());
            pstmt.setString(2, donor.getAddress());
            pstmt.setString(3, donor.getCity());
            pstmt.setString(4, donor.getState());
            pstmt.setString(5, donor.getPincode());
            pstmt.setDouble(6, donor.getLatitude());
            pstmt.setDouble(7, donor.getLongitude());
            pstmt.setDouble(8, donor.getWeight());
            pstmt.setBoolean(9, donor.isAvailable());
            pstmt.setString(10, donor.getMedicalConditions());
            pstmt.setInt(11, donor.getDonorId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✓ Donor profile updated for ID: " + donor.getDonorId());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update donor profile: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Find available donors by blood group and city
     * Used for donor matching algorithm
     * @param bloodGroup Required blood group
     * @param city City to search in
     * @return List of available donors
     */
    public List<Donor> findAvailableDonors(Donor.BloodGroup bloodGroup, String city) {
        String sql = "SELECT * FROM donors WHERE blood_group = ? AND city = ? " +
                     "AND is_available = TRUE " +
                     "ORDER BY last_donation_date ASC";
        
        List<Donor> donors = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // NOTE: This uses the incorrect enum.name() and needs to be replaced 
            // by calling getDonorsByBloodGroup directly (which will be corrected next).
            pstmt.setString(1, bloodGroup.name().replace("_", ""));
            pstmt.setString(2, city);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                donors.add(extractDonorFromResultSet(rs));
            }
            
            System.out.println("✓ Found " + donors.size() + " available donors");
            
        } catch (SQLException e) {
            System.err.println("✗ Error finding donors: " + e.getMessage());
        }
        
        return donors;
    }
    
    /**
     * Update donor availability status
     */
    public boolean updateAvailability(int donorId, boolean available) {
        String sql = "UPDATE donors SET is_available = ?, updated_at = NOW() WHERE donor_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, available);
            pstmt.setInt(2, donorId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Availability updated for donor_id: " + donorId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update availability: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update last donation date
     */
    public boolean updateLastDonation(int donorId, java.time.LocalDate donationDate) {
        String sql = "UPDATE donors SET last_donation_date = ?, updated_at = NOW() WHERE donor_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(donationDate));
            pstmt.setInt(2, donorId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update last donation: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get donors by blood group (for statistics and matching)
     */
    public List<Donor> getDonorsByBloodGroup(Donor.BloodGroup bloodGroup) {
        String sql = "SELECT * FROM donors WHERE blood_group = ?";
        List<Donor> donors = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // --- FIX: Use the correct database symbol for the blood group ---
            pstmt.setString(1, bloodGroup.getDisplay()); 
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                donors.add(extractDonorFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching donors by blood group: " + e.getMessage());
        }
        
        return donors;
    }
    
    /**
     * Extract Donor object from ResultSet
     */
    private Donor extractDonorFromResultSet(ResultSet rs) throws SQLException {
        Donor donor = new Donor();
        donor.setDonorId(rs.getInt("donor_id"));
        donor.setUserId(rs.getInt("user_id"));
        donor.setFullName(rs.getString("full_name"));
        
        String bloodGroupStr = rs.getString("blood_group");
        if (bloodGroupStr != null) {
            bloodGroupStr = bloodGroupStr
                .replace("+", "_POSITIVE")
                .replace("-", "_NEGATIVE")
                .toUpperCase();
        }
        
        donor.setBloodGroup(Donor.BloodGroup.valueOf(bloodGroupStr));
        
        donor.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        donor.setGender(Donor.Gender.valueOf(rs.getString("gender")));
        donor.setAddress(rs.getString("address"));
        donor.setCity(rs.getString("city"));
        donor.setState(rs.getString("state"));
        donor.setPincode(rs.getString("pincode"));
        donor.setLatitude(rs.getDouble("latitude"));
        donor.setLongitude(rs.getDouble("longitude"));
        donor.setWeight(rs.getDouble("weight"));
        donor.setAvailable(rs.getBoolean("is_available"));
        donor.setMedicalConditions(rs.getString("medical_conditions"));
        
        Date lastDonation = rs.getDate("last_donation_date");
        if (lastDonation != null) {
            donor.setLastDonationDate(lastDonation.toLocalDate());
        }
        
        return donor;
    }
}