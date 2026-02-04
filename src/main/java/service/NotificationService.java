package service;

import dao.*;
import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class NotificationService {
    
    private final config.DatabaseConfig dbConfig;
    
    public NotificationService() {
        this.dbConfig = config.DatabaseConfig.getInstance();
    }
    
    /**
     * Send notification to donor about donation request
     */
    public boolean sendNotificationToDonor(int userId, String title, String message, int requestId) {
        return createNotification(
            userId, 
            title, 
            message, 
            Notification.NotificationType.MATCH,
            Notification.Priority.HIGH,
            "REQUEST",
            requestId
        );
    }
    
    /**
     * Send stock alert notification to hospital
     */
    public boolean sendStockAlert(int userId, String bloodGroup, int currentStock, int threshold) {
        String title = "Low Stock Alert - " + bloodGroup;
        String message = String.format(
            "Blood stock for %s is low. Current: %dml, Threshold: %dml. Please restock.",
            bloodGroup, currentStock, threshold
        );
        
        return createNotification(
            userId, 
            title, 
            message, 
            Notification.NotificationType.STOCK_ALERT,
            Notification.Priority.HIGH,
            null,
            null
        );
    }
    
    /**
     * Send request approval notification to patient
     */
    public boolean sendApprovalNotification(int userId, String hospitalName, Request.RequestType requestType) {
        String title = "Request Approved";
        String message = String.format(
            "Your %s request has been approved by %s. Please contact them for further details.",
            requestType, hospitalName
        );
        
        return createNotification(
            userId, 
            title, 
            message, 
            Notification.NotificationType.APPROVAL,
            Notification.Priority.MEDIUM,
            null,
            null
        );
    }
    
    /**
     * Create and store notification in database
     */
    private boolean createNotification(int userId, String title, String message, 
                                      Notification.NotificationType type, 
                                      Notification.Priority priority,
                                      String entityType, Integer entityId) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, " +
                     "priority, related_entity_type, related_entity_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = dbConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, message);
            // Use enum name() for persistence
            pstmt.setString(4, type.name()); 
            pstmt.setString(5, priority.name());
            pstmt.setString(6, entityType);
            
            if (entityId != null) {
                pstmt.setInt(7, entityId);
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✓ Notification sent to user " + userId + " (Type: " + type.name() + ")");
                return true;
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Failed to send notification: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE " +
                     "ORDER BY priority DESC, created_at DESC";
        
        List<Notification> notifications = new ArrayList<>();
        
        try (java.sql.Connection conn = dbConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Error fetching notifications: " + e.getMessage());
        }
        
        return notifications;
    }
    
    /**
     * Mark notification as read
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE, read_at = NOW() " +
                     "WHERE notification_id = ?";
        
        try (java.sql.Connection conn = dbConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Failed to mark notification as read: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get notification count for user
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        
        try (java.sql.Connection conn = dbConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("✗ Error getting notification count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Send bulk notifications to multiple users
     */
    public void sendBulkNotifications(List<Integer> userIds, String title, String message, 
                                     Notification.NotificationType type) {
        for (int userId : userIds) {
            createNotification(userId, title, message, type, Notification.Priority.MEDIUM, null, null);
        }
        System.out.println("✓ Sent bulk notifications to " + userIds.size() + " users");
    }
    
    private Notification extractNotificationFromResultSet(java.sql.ResultSet rs) 
            throws java.sql.SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        
        // Ensure String value is uppercased to match Java Enum constants
        notification.setNotificationType(
            Notification.NotificationType.valueOf(rs.getString("notification_type").toUpperCase())
        );
        notification.setPriority(
            Notification.Priority.valueOf(rs.getString("priority").toUpperCase())
        );
        
        notification.setRead(rs.getBoolean("is_read"));
        notification.setRelatedEntityType(rs.getString("related_entity_type"));
        
        int entityId = rs.getInt("related_entity_id");
        if (!rs.wasNull()) {
            notification.setRelatedEntityId(entityId);
        }
        
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
             notification.setCreatedAt(createdAt.toLocalDateTime());
        }
       
        java.sql.Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            notification.setReadAt(readAt.toLocalDateTime());
        }
        
        return notification;
    }
}