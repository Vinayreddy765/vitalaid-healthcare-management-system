/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vinay Reddy
 */
import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class VentilatorDAO {
    
    private final DatabaseConfig dbConfig;
    
    public VentilatorDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Add new ventilator
     */
    public int addVentilator(Ventilator ventilator) {
        String sql = "INSERT INTO ventilators (hospital_id, ventilator_type, model_name, " +
                     "serial_number, status, location_in_hospital) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, ventilator.getHospitalId());
            pstmt.setString(2, ventilator.getVentilatorType().name());
            pstmt.setString(3, ventilator.getModelName());
            pstmt.setString(4, ventilator.getSerialNumber());
            pstmt.setString(5, ventilator.getStatus().name());
            pstmt.setString(6, ventilator.getLocationInHospital());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to add ventilator: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Get all ventilators for a hospital
     */
    public List<Ventilator> getVentilatorsByHospital(int hospitalId) {
        String sql = "SELECT * FROM ventilators WHERE hospital_id = ? ORDER BY status, serial_number";
        List<Ventilator> ventilators = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ventilators.add(extractVentilatorFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching ventilators: " + e.getMessage());
        }
        
        return ventilators;
    }
    
    /**
     * Get available ventilators
     */
    public List<Ventilator> getAvailableVentilators(int hospitalId) {
        String sql = "SELECT * FROM ventilators WHERE hospital_id = ? AND status = 'AVAILABLE'";
        List<Ventilator> ventilators = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ventilators.add(extractVentilatorFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching available ventilators: " + e.getMessage());
        }
        
        return ventilators;
    }
    
    /**
     * Update ventilator status
     */
    public boolean updateVentilatorStatus(int ventilatorId, Ventilator.VentilatorStatus status) {
        String sql = "UPDATE ventilators SET status = ?, updated_at = NOW() WHERE ventilator_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, ventilatorId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update ventilator status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Find hospitals with available ventilators
     */
    public List<Hospital> findHospitalsWithVentilators(String city) {
        String sql = "SELECT DISTINCT h.* FROM hospitals h " +
                     "JOIN ventilators v ON h.hospital_id = v.hospital_id " +
                     "WHERE h.city = ? AND v.status = 'AVAILABLE' AND h.is_verified = TRUE";
        
        List<Hospital> hospitals = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Hospital hospital = new Hospital();
                hospital.setHospitalId(rs.getInt("hospital_id"));
                hospital.setHospitalName(rs.getString("hospital_name"));
                hospital.setAddress(rs.getString("address"));
                hospital.setCity(rs.getString("city"));
                hospital.setContactPerson(rs.getString("contact_person"));
                hospitals.add(hospital);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error finding hospitals with ventilators: " + e.getMessage());
        }
        
        return hospitals;
    }
    
    private Ventilator extractVentilatorFromResultSet(ResultSet rs) throws SQLException {
        Ventilator ventilator = new Ventilator();
        ventilator.setVentilatorId(rs.getInt("ventilator_id"));
        ventilator.setHospitalId(rs.getInt("hospital_id"));
        ventilator.setVentilatorType(Ventilator.VentilatorType.valueOf(rs.getString("ventilator_type")));
        ventilator.setModelName(rs.getString("model_name"));
        ventilator.setSerialNumber(rs.getString("serial_number"));
        ventilator.setStatus(Ventilator.VentilatorStatus.valueOf(rs.getString("status")));
        ventilator.setLocationInHospital(rs.getString("location_in_hospital"));
        
        Date lastMaintenance = rs.getDate("last_maintenance_date");
        if (lastMaintenance != null) {
            ventilator.setLastMaintenanceDate(lastMaintenance.toLocalDate());
        }
        
        Date nextMaintenance = rs.getDate("next_maintenance_date");
        if (nextMaintenance != null) {
            ventilator.setNextMaintenanceDate(nextMaintenance.toLocalDate());
        }
        
        return ventilator;
    }
}