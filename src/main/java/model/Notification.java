/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Vinay Reddy
 */

import java.time.LocalDateTime;


public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Priority priority;
    private boolean isRead;
    private String relatedEntityType;
    private Integer relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    public enum NotificationType {
        REQUEST, MATCH, STOCK_ALERT, APPROVAL, GENERAL
    }
    
    public enum Priority {
        HIGH, MEDIUM, LOW
    }
    
    // Constructors
    public Notification() {}
    
    public Notification(int userId, String title, String message, NotificationType type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = type;
        this.priority = Priority.MEDIUM;
        this.isRead = false;
    }
    
    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    
    public Integer getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Integer relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", type=" + notificationType +
                ", priority=" + priority +
                ", read=" + isRead +
                '}';
    }
}