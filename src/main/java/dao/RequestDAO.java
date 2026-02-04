package dao;

import config.DatabaseConfig;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {
    
    private final DatabaseConfig dbConfig;
    
    public RequestDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create new request
     */
    public int createRequest(Request request) {
        String sql = "INSERT INTO requests (patient_id, request_type, blood_group, quantity_ml, " +
                     "urgency, required_by, hospital_id, reason, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, request.getPatientId());
            pstmt.setString(2, request.getRequestType().name());
            
            // Properly convert enum blood group to short symbol (A+, O-, etc.)
            if (request.getBloodGroup() != null) {
                pstmt.setString(3, mapBloodGroupSymbol(request.getBloodGroup()));
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            
            pstmt.setInt(4, request.getQuantityMl());
            pstmt.setString(5, request.getUrgency().name());
            pstmt.setTimestamp(6, Timestamp.valueOf(request.getRequiredBy()));
            
            if (request.getHospitalId() != null) {
                pstmt.setInt(7, request.getHospitalId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setString(8, request.getReason());
            pstmt.setString(9, request.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int requestId = rs.getInt(1);
                        System.out.println("✓ Request created with ID: " + requestId);
                        return requestId;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to create request: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get request by ID
     */
    public Request getRequestById(int requestId) {
        String sql = "SELECT * FROM requests WHERE request_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractRequestFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching request by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get requests by patient ID
     */
    public List<Request> getRequestsByPatient(int patientId) {
        String sql = "SELECT * FROM requests WHERE patient_id = ? ORDER BY created_at DESC";
        List<Request> requests = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching patient requests: " + e.getMessage());
        }
        
        return requests;
    }
    
    /**
     * Get pending requests by urgency
     */
    public List<Request> getPendingRequests() {
        String sql = "SELECT * FROM requests WHERE status = 'PENDING' " +
                     "ORDER BY FIELD(urgency, 'CRITICAL', 'URGENT', 'NORMAL'), created_at ASC";
        List<Request> requests = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching pending requests: " + e.getMessage());
        }
        
        return requests;
    }
    
    /**
     * Update request status
     */
    public boolean updateRequestStatus(int requestId, Request.RequestStatus status) {
        String sql = "UPDATE requests SET status = ?, updated_at = NOW() WHERE request_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, requestId);
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✓ Request status updated to: " + status);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to update request status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Assign hospital to request
     */
    public boolean assignHospitalToRequest(int requestId, int hospitalId) {
        String sql = "UPDATE requests SET hospital_id = ?, status = 'APPROVED', updated_at = NOW() " +
                     "WHERE request_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            pstmt.setInt(2, requestId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to assign hospital: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get critical requests (for alerts)
     */
    public List<Request> getCriticalRequests() {
        String sql = "SELECT * FROM requests WHERE urgency = 'CRITICAL' AND status = 'PENDING' " +
                     "ORDER BY required_by ASC";
        List<Request> requests = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error fetching critical requests: " + e.getMessage());
        }
        
        return requests;
    }
    
    /**
     * Extract Request object from ResultSet
     */
    private Request extractRequestFromResultSet(ResultSet rs) throws SQLException {
        Request request = new Request();
        request.setRequestId(rs.getInt("request_id"));
        request.setPatientId(rs.getInt("patient_id"));
        request.setRequestType(Request.RequestType.valueOf(rs.getString("request_type")));
        
        String bloodGroupStr = rs.getString("blood_group");
        if (bloodGroupStr != null) {
            request.setBloodGroup(Donor.BloodGroup.valueOf(
                bloodGroupStr.replace("+", "_POSITIVE").replace("-", "_NEGATIVE")));
        }
        
        request.setQuantityMl(rs.getInt("quantity_ml"));
        request.setUrgency(Request.Urgency.valueOf(rs.getString("urgency")));
        
        Timestamp requiredBy = rs.getTimestamp("required_by");
        if (requiredBy != null) {
             request.setRequiredBy(requiredBy.toLocalDateTime());
        }
        
        int hospitalId = rs.getInt("hospital_id");
        if (!rs.wasNull()) {
            request.setHospitalId(hospitalId);
        }
        
        request.setStatus(Request.RequestStatus.valueOf(rs.getString("status")));
        request.setReason(rs.getString("reason"));
        request.setNotes(rs.getString("notes"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            request.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return request;
    }
    
    /**
     * Converts enum blood group (e.g., A_POSITIVE) to symbol (A+, B-, etc.)
     */
    private String mapBloodGroupSymbol(Donor.BloodGroup bg) {
        switch (bg) {
            case A_POSITIVE:  return "A+";
            case A_NEGATIVE:  return "A-";
            case B_POSITIVE:  return "B+";
            case B_NEGATIVE:  return "B-";
            case AB_POSITIVE: return "AB+";
            case AB_NEGATIVE: return "AB-";
            case O_POSITIVE:  return "O+";
            case O_NEGATIVE:  return "O-";
            default: return null;
        }
    }
}