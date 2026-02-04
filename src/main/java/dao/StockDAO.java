package dao;

import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {
    
    private final DatabaseConfig dbConfig;
    
    public StockDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    // Helper method to map enum (A_POSITIVE) to database symbol (A+)
    private String mapBloodGroupSymbol(Donor.BloodGroup bg) {
        return bg.getDisplay(); // Assuming Donor.BloodGroup has a getDisplay() method that returns "A+", "O-", etc.
    }
    
    // Helper method to extract BloodStock from ResultSet
    private BloodStock extractBloodStockFromResultSet(ResultSet rs) throws SQLException {
        BloodStock stock = new BloodStock();
        stock.setStockId(rs.getInt("stock_id"));
        stock.setHospitalId(rs.getInt("hospital_id"));
        
        String bloodGroupStr = rs.getString("blood_group").replace("+", "_POSITIVE").replace("-", "_NEGATIVE");
        stock.setBloodGroup(Donor.BloodGroup.valueOf(bloodGroupStr));
        
        stock.setQuantityMl(rs.getInt("quantity_ml"));
        stock.setMinThreshold(rs.getInt("min_threshold"));
        
        Timestamp lastUpdatedTs = rs.getTimestamp("last_updated");
        if (lastUpdatedTs != null) {
            stock.setLastUpdated(lastUpdatedTs.toLocalDateTime());
        }
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            stock.setExpiryDate(expiryDate.toLocalDate());
        }
        
        return stock;
    }
    
    // Helper method to extract PlasmaStock from ResultSet
    private PlasmaStock extractPlasmaStockFromResultSet(ResultSet rs) throws SQLException {
        PlasmaStock stock = new PlasmaStock();
        stock.setPlasmaId(rs.getInt("plasma_id"));
        stock.setHospitalId(rs.getInt("hospital_id"));
        
        String bloodGroupStr = rs.getString("blood_group").replace("+", "_POSITIVE").replace("-", "_NEGATIVE");
        stock.setBloodGroup(Donor.BloodGroup.valueOf(bloodGroupStr));
        
        stock.setQuantityMl(rs.getInt("quantity_ml"));
        stock.setMinThreshold(rs.getInt("min_threshold"));
        
        Timestamp lastUpdatedTs = rs.getTimestamp("last_updated");
        if (lastUpdatedTs != null) {
            stock.setLastUpdated(lastUpdatedTs.toLocalDateTime());
        }
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            stock.setExpiryDate(expiryDate.toLocalDate());
        }
        
        return stock;
    }

    /**
     * Get blood stock for a hospital and blood group
     */
    public BloodStock getBloodStock(int hospitalId, Donor.BloodGroup bloodGroup) {
        String sql = "SELECT * FROM blood_stock WHERE hospital_id = ? AND blood_group = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            pstmt.setString(2, mapBloodGroupSymbol(bloodGroup));
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractBloodStockFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching blood stock: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all blood stock for a hospital
     */
    public List<BloodStock> getAllBloodStock(int hospitalId) {
        String sql = "SELECT * FROM blood_stock WHERE hospital_id = ? ORDER BY blood_group";
        List<BloodStock> stocks = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                stocks.add(extractBloodStockFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching blood stocks: " + e.getMessage());
        }
        
        return stocks;
    }
    
    /**
     * FIX: Implemented UPSERT logic for Blood Stock.
     * Attempts to UPDATE, and if no rows affected, INSERTs a new record.
     */
    public boolean updateBloodStock(int hospitalId, Donor.BloodGroup bloodGroup, int quantityChange) {
        String bloodGroupSymbol = mapBloodGroupSymbol(bloodGroup);

        // 1. Attempt to UPDATE existing stock
        String updateSql = "UPDATE blood_stock SET quantity_ml = quantity_ml + ?, " +
                           "last_updated = NOW() WHERE hospital_id = ? AND blood_group = ?";
        
        // 2. INSERT new record if UPDATE fails (quantityChange is the initial quantity)
        // Set expiry date to 30 days from now, and min_threshold to default 500
        String insertSql = "INSERT INTO blood_stock (hospital_id, blood_group, quantity_ml, expiry_date, min_threshold) " +
                           "VALUES (?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500)";

        try (Connection conn = dbConfig.getConnection()) {
            
            // Try Update
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, quantityChange);
                updatePstmt.setInt(2, hospitalId);
                updatePstmt.setString(3, bloodGroupSymbol);
                
                int rows = updatePstmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✓ Blood stock UPDATED successfully for " + bloodGroupSymbol);
                    return true;
                }
            }
            
            // If Update failed (0 rows affected), try Insert
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, hospitalId);
                insertPstmt.setString(2, bloodGroupSymbol);
                insertPstmt.setInt(3, quantityChange);
                
                int rows = insertPstmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✓ Blood stock INSERTED successfully for " + bloodGroupSymbol);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Failed to UPSERT blood stock for " + bloodGroupSymbol + ": " + e.getMessage());
            // Ignore common race condition errors like "Duplicate entry" that result from concurrent updates/inserts
        }
        
        return false;
    }
    
    /**
     * Get low stock alerts (below threshold)
     */
    public List<BloodStock> getLowStockAlerts(int hospitalId) {
        String sql = "SELECT * FROM blood_stock WHERE hospital_id = ? AND quantity_ml < min_threshold";
        List<BloodStock> lowStocks = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                lowStocks.add(extractBloodStockFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching low stock alerts: " + e.getMessage());
        }
        
        return lowStocks;
    }
    
    /**
     * Get all plasma stock for a hospital
     */
    public List<PlasmaStock> getAllPlasmaStock(int hospitalId) {
        String sql = "SELECT * FROM plasma_stock WHERE hospital_id = ? ORDER BY blood_group";
        List<PlasmaStock> stocks = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                stocks.add(extractPlasmaStockFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching plasma stocks: " + e.getMessage());
        }
        
        return stocks;
    }
    
    /**
     * FIX: Implemented UPSERT logic for Plasma Stock.
     * Attempts to UPDATE, and if no rows affected, INSERTs a new record.
     */
    public boolean updatePlasmaStock(int hospitalId, Donor.BloodGroup bloodGroup, int quantityChange) {
        String bloodGroupSymbol = mapBloodGroupSymbol(bloodGroup);
        
        // 1. Attempt to UPDATE existing stock
        String updateSql = "UPDATE plasma_stock SET quantity_ml = quantity_ml + ?, " +
                           "last_updated = NOW() WHERE hospital_id = ? AND blood_group = ?";
        
        // 2. INSERT new record if UPDATE fails (quantityChange is the initial quantity)
        // Set expiry date to 60 days from now, and min_threshold to default 200
        String insertSql = "INSERT INTO plasma_stock (hospital_id, blood_group, quantity_ml, expiry_date, min_threshold) " +
                           "VALUES (?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 200)";

        try (Connection conn = dbConfig.getConnection()) {
            
            // Try Update
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, quantityChange);
                updatePstmt.setInt(2, hospitalId);
                updatePstmt.setString(3, bloodGroupSymbol);
                
                int rows = updatePstmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✓ Plasma stock UPDATED successfully for " + bloodGroupSymbol);
                    return true;
                }
            }
            
            // If Update failed (0 rows affected), try Insert
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, hospitalId);
                insertPstmt.setString(2, bloodGroupSymbol);
                insertPstmt.setInt(3, quantityChange);
                
                int rows = insertPstmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("✓ Plasma stock INSERTED successfully for " + bloodGroupSymbol);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Failed to UPSERT plasma stock for " + bloodGroupSymbol + ": " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Find hospitals with available blood stock
     */
    public List<Hospital> findHospitalsWithStock(Donor.BloodGroup bloodGroup, String city, int requiredQuantity) {
        String sql = "SELECT h.* FROM hospitals h " +
                     "JOIN blood_stock bs ON h.hospital_id = bs.hospital_id " +
                     "WHERE bs.blood_group = ? AND h.city = ? AND bs.quantity_ml >= ? " +
                     "AND h.is_verified = TRUE " +
                     "ORDER BY bs.quantity_ml DESC";
        
        List<Hospital> hospitals = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, mapBloodGroupSymbol(bloodGroup));
            pstmt.setString(2, city);
            pstmt.setInt(3, requiredQuantity);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Hospital hospital = new Hospital();
                hospital.setHospitalId(rs.getInt("hospital_id"));
                hospital.setHospitalName(rs.getString("hospital_name"));
                hospital.setAddress(rs.getString("address"));
                hospital.setCity(rs.getString("city"));
                hospital.setContactPerson(rs.getString("contact_person"));
                hospital.setLatitude(rs.getDouble("latitude"));
                hospital.setLongitude(rs.getDouble("longitude"));
                hospitals.add(hospital);
            }
            
            System.out.println("✓ Found " + hospitals.size() + " hospitals with stock");
            
        } catch (SQLException e) {
            System.err.println("✗ Error finding hospitals with stock: " + e.getMessage());
        }
        
        return hospitals;
    }
}