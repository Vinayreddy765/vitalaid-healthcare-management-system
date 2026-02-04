package util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class SMSUtil {
    
    // SMS API configuration
    private static final String SMS_API_KEY = "your_sms_api_key";
    private static final String SMS_API_URL = "https://api.sms-provider.com/send";
    private static final String SENDER_ID = "VITAID";
    
    /**
     * Send SMS notification
     * * @param phoneNumber Recipient phone number
     * @param message SMS message (max 160 characters)
     * @return true if sent successfully
     */
    public static boolean sendSMS(String phoneNumber, String message) {
        // TODO: Implement using SMS Gateway API
        /*
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String jsonBody = String.format(
                "{\"apiKey\":\"%s\",\"senderId\":\"%s\",\"phoneNumber\":\"%s\",\"message\":\"%s\"}",
                SMS_API_KEY, SENDER_ID, phoneNumber, message
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SMS_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            return false;
        }
        */
        
        // Placeholder implementation
        System.out.println("\n📱 SMS SENT:");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("─────────────────────────────\n");
        
        return true;
    }
    
    /**
     * Send urgent donation request SMS
     */
    public static boolean sendDonorRequestSMS(String phone, String bloodGroup, String location) {
        String message = String.format(
            "URGENT: %s blood needed in %s. Login to VitalAid to respond. Lives depend on you! -VitalAid",
            bloodGroup, location
        );
        
        return sendSMS(phone, truncateMessage(message, 160));
    }
    
    /**
     * Send request approval SMS
     */
    public static boolean sendApprovalSMS(String phone, String hospitalName) {
        String message = String.format(
            "Your request approved by %s. Contact them immediately. -VitalAid",
            hospitalName
        );
        
        return sendSMS(phone, truncateMessage(message, 160));
    }
    
    /**
     * Send OTP for verification
     */
    public static boolean sendOTP(String phone, String otp) {
        String message = String.format(
            "Your VitalAid verification code is: %s. Valid for 10 minutes. Do not share. -VitalAid",
            otp
        );
        
        return sendSMS(phone, message);
    }
    
    /**
     * Generate 6-digit OTP
     */
    public static String generateOTP() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    /**
     * Truncate message to specified length
     */
    private static String truncateMessage(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
}

/**
 * DateUtil - Date and time utility functions
 */
class DateUtil {
    
    /**
     * Calculate age from date of birth
     */
    public static int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    /**
     * Check if date is within range
     */
    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
    
    /**
     * Format date for display
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
        return date.format(formatter);
    }
    
    /**
     * Get days until date
     */
    public static long getDaysUntil(LocalDate targetDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
    }
    
    /**
     * Check if date is expired
     */
    public static boolean isExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}

/**
 * StringUtil - String manipulation utilities
 */
class StringUtil {
    
    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Capitalize first letter of each word
     */
    public static String toTitleCase(String str) {
        if (isEmpty(str)) return str;
        
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Mask sensitive data (e.g., phone numbers)
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        
        int visibleDigits = 2;
        String masked = phone.substring(0, visibleDigits) + 
                       "X".repeat(phone.length() - 2 * visibleDigits) +
                       phone.substring(phone.length() - visibleDigits);
        
        return masked;
    }
    
    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        
        return result.toString();
    }
}