package dao;

import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    
    private final DatabaseConfig dbConfig;
    
    public PatientDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Register new patient
     */
    public int registerPatient(Patient patient) {
        String sql = "INSERT INTO patients (user_id, full_name, blood_group, date_of_birth, gender, " +
                     "address, city, state, pincode, emergency_contact, medical_history) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        System.out.println("--- Registering Patient ---");
        System.out.println("Full Name: " + patient.getFullName());
        System.out.println("Blood Group Enum: " + patient.getBloodGroup());
        System.out.println("Blood Group for DB: " + patient.getBloodGroup().getDisplay());
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, patient.getUserId());
            pstmt.setString(2, patient.getFullName());
            // FIXED: Use getDisplay() to get "A+", "B+", etc. instead of name()
            pstmt.setString(3, patient.getBloodGroup().getDisplay());
            pstmt.setDate(4, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(5, patient.getGender().name());
            pstmt.setString(6, patient.getAddress());
            pstmt.setString(7, patient.getCity());
            pstmt.setString(8, patient.getState());
            pstmt.setString(9, patient.getPincode());
            pstmt.setString(10, patient.getEmergencyContact());
            pstmt.setString(11, patient.getMedicalHistory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int patientId = rs.getInt(1);
                    System.out.println("✓ Patient registered with ID: " + patientId);
                    return patientId;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to register patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get patient by user ID
     */
    public Patient getPatientByUserId(int userId) {
        String sql = "SELECT * FROM patients WHERE user_id = ?";
        
        System.out.println("\n--- PatientDAO.getPatientByUserId() ---");
        System.out.println("Searching for patient with user_id: " + userId);
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✓ Patient record found in database");
                Patient patient = extractPatientFromResultSet(rs);
                System.out.println("  Patient ID: " + patient.getPatientId());
                System.out.println("  Full Name: " + patient.getFullName());
                System.out.println("  Blood Group: " + patient.getBloodGroup().getDisplay());
                System.out.println("--- End getPatientByUserId() ---\n");
                return patient;
            } else {
                System.err.println("✗ No patient record found for user_id: " + userId);
                System.out.println("--- End getPatientByUserId() ---\n");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error in getPatientByUserId(): " + e.getMessage());
            e.printStackTrace();
            System.out.println("--- End getPatientByUserId() ---\n");
        }
        
        return null;
    }
    
    /**
     * Get patient by ID
     */
    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching patient: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update patient information
     */
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET full_name = ?, blood_group = ?, address = ?, " +
                     "city = ?, state = ?, pincode = ?, emergency_contact = ?, " +
                     "medical_history = ?, updated_at = NOW() WHERE patient_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getFullName());
            // FIXED: Use getDisplay() instead of name()
            pstmt.setString(2, patient.getBloodGroup().getDisplay());
            pstmt.setString(3, patient.getAddress());
            pstmt.setString(4, patient.getCity());
            pstmt.setString(5, patient.getState());
            pstmt.setString(6, patient.getPincode());
            pstmt.setString(7, patient.getEmergencyContact());
            pstmt.setString(8, patient.getMedicalHistory());
            pstmt.setInt(9, patient.getPatientId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Patient updated: " + patient.getPatientId());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update patient: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all patients by city
     */
    public List<Patient> getPatientsByCity(String city) {
        String sql = "SELECT * FROM patients WHERE city = ?";
        List<Patient> patients = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching patients by city: " + e.getMessage());
        }
        
        return patients;
    }
    
    /**
     * Extract Patient object from ResultSet
     */
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setFullName(rs.getString("full_name"));
        
        // Parse blood group from database (A+, B+, etc.) to enum (A_POSITIVE, B_POSITIVE, etc.)
        String bloodGroupStr = rs.getString("blood_group")
            .replace("+", "_POSITIVE")
            .replace("-", "_NEGATIVE");
        patient.setBloodGroup(Donor.BloodGroup.valueOf(bloodGroupStr));
        
        patient.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        patient.setGender(Donor.Gender.valueOf(rs.getString("gender")));
        patient.setAddress(rs.getString("address"));
        patient.setCity(rs.getString("city"));
        patient.setState(rs.getString("state"));
        patient.setPincode(rs.getString("pincode"));
        patient.setEmergencyContact(rs.getString("emergency_contact"));
        patient.setMedicalHistory(rs.getString("medical_history"));
        
        return patient;
    }
}