package service;

import dao.*;
import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    
    private final config.DatabaseConfig dbConfig;
    
    public ReportService() {
        this.dbConfig = config.DatabaseConfig.getInstance();
    }
    
    /**
     * Generate inventory report for a hospital
     */
    public Map<String, Object> generateInventoryReport(int hospitalId) {
        Map<String, Object> report = new HashMap<>();
        
        try (java.sql.Connection conn = dbConfig.getConnection()) {
            
            // Blood stock summary
            String bloodSql = "SELECT blood_group, quantity_ml, min_threshold FROM blood_stock " +
                            "WHERE hospital_id = ?";
            
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(bloodSql)) {
                pstmt.setInt(1, hospitalId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                
                List<Map<String, Object>> bloodStock = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("bloodGroup", rs.getString("blood_group"));
                    stock.put("quantity", rs.getInt("quantity_ml"));
                    stock.put("threshold", rs.getInt("min_threshold"));
                    stock.put("status", rs.getInt("quantity_ml") < rs.getInt("min_threshold") 
                        ? "LOW" : "OK");
                    bloodStock.add(stock);
                }
                report.put("bloodStock", bloodStock);
            }
            
            // Ventilator summary
            String ventSql = "SELECT status, COUNT(*) as count FROM ventilators " +
                           "WHERE hospital_id = ? GROUP BY status";
            
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(ventSql)) {
                pstmt.setInt(1, hospitalId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                
                Map<String, Integer> ventilators = new HashMap<>();
                while (rs.next()) {
                    ventilators.put(rs.getString("status"), rs.getInt("count"));
                }
                report.put("ventilators", ventilators);
            }
            
            report.put("generated_at", java.time.LocalDateTime.now());
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Error generating inventory report: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * Generate donor statistics report
     */
    public Map<String, Object> generateDonorStatistics(String city) {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = "SELECT blood_group, " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN is_available = TRUE THEN 1 ELSE 0 END) as available " +
                    "FROM donors WHERE city = ? GROUP BY blood_group";
        
        try (java.sql.Connection conn = dbConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, city);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> donorStats = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("bloodGroup", rs.getString("blood_group"));
                stat.put("total", rs.getInt("total"));
                stat.put("available", rs.getInt("available"));
                donorStats.add(stat);
            }
            
            stats.put("donorsByBloodGroup", donorStats);
            stats.put("city", city);
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Error generating donor statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Export report to PDF format (placeholder - requires iText or similar library)
     */
    public boolean exportToPDF(Map<String, Object> report, String filename) {
        // TODO: Implement PDF export using iText library
        System.out.println("PDF export functionality - to be implemented with iText");
        return true;
    }
    
    /**
     * Export report to Excel format (placeholder - requires Apache POI)
     */
    public boolean exportToExcel(Map<String, Object> report, String filename) {
        // TODO: Implement Excel export using Apache POI
        System.out.println("Excel export functionality - to be implemented with Apache POI");
        return true;
    }
}