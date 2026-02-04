package dao;

import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HospitalDAO - Data Access Object for Hospital operations
 */
public class HospitalDAO {
    
    private final DatabaseConfig dbConfig;
    
    public HospitalDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Register new hospital
     */
    public int registerHospital(Hospital hospital) {
        String sql = "INSERT INTO hospitals (user_id, hospital_name, registration_number, address, " +
                     "city, state, pincode, latitude, longitude, contact_person, license_number, " +
                     "bed_capacity, has_blood_bank) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, hospital.getUserId());
            pstmt.setString(2, hospital.getHospitalName());
            pstmt.setString(3, hospital.getRegistrationNumber());
            pstmt.setString(4, hospital.getAddress());
            pstmt.setString(5, hospital.getCity());
            pstmt.setString(6, hospital.getState());
            pstmt.setString(7, hospital.getPincode());
            pstmt.setDouble(8, hospital.getLatitude());
            pstmt.setDouble(9, hospital.getLongitude());
            pstmt.setString(10, hospital.getContactPerson());
            pstmt.setString(11, hospital.getLicenseNumber());
            pstmt.setInt(12, hospital.getBedCapacity());
            pstmt.setBoolean(13, hospital.hasBloodBank());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to register hospital: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Get hospital by user ID (Used for login/session loading)
     */
    public Hospital getHospitalByUserId(int userId) {
        String sql = "SELECT * FROM hospitals WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractHospitalFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching hospital by user ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get hospital by hospital ID (Used by services and controllers)
     */
    public Hospital getHospitalById(int hospitalId) {
        String sql = "SELECT * FROM hospitals WHERE hospital_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractHospitalFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching hospital by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all verified hospitals in a city
     */
    public List<Hospital> getHospitalsByCity(String city) {
        String sql = "SELECT * FROM hospitals WHERE city = ? AND is_verified = TRUE";
        List<Hospital> hospitals = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                hospitals.add(extractHospitalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching hospitals by city: " + e.getMessage());
        }
        
        return hospitals;
    }
    
    /**
     * Get all hospitals
     */
    public List<Hospital> getAllHospitals() {
        String sql = "SELECT * FROM hospitals ORDER BY hospital_name";
        List<Hospital> hospitals = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                hospitals.add(extractHospitalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching all hospitals: " + e.getMessage());
        }
        
        return hospitals;
    }
    
    private Hospital extractHospitalFromResultSet(ResultSet rs) throws SQLException {
        Hospital hospital = new Hospital();
        hospital.setHospitalId(rs.getInt("hospital_id"));
        hospital.setUserId(rs.getInt("user_id"));
        hospital.setHospitalName(rs.getString("hospital_name"));
        hospital.setRegistrationNumber(rs.getString("registration_number"));
        hospital.setAddress(rs.getString("address"));
        hospital.setCity(rs.getString("city"));
        hospital.setState(rs.getString("state"));
        hospital.setPincode(rs.getString("pincode"));
        hospital.setLatitude(rs.getDouble("latitude"));
        hospital.setLongitude(rs.getDouble("longitude"));
        hospital.setContactPerson(rs.getString("contact_person"));
        hospital.setLicenseNumber(rs.getString("license_number"));
        hospital.setVerified(rs.getBoolean("is_verified"));
        hospital.setBedCapacity(rs.getInt("bed_capacity"));
        hospital.setHasBloodBank(rs.getBoolean("has_blood_bank"));
        
        // Handle timestamps if necessary (though not explicitly in this model)
        // hospital.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        // hospital.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return hospital;
    }
}