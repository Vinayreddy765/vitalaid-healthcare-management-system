package util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * ValidationUtil - Input validation and data sanitization utilities
 */
public class ValidationUtil {
    
    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[6-9]\\d{9}$" // Indian mobile number format
    );
    
    private static final Pattern PINCODE_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}$" // Indian pincode format
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    /**
     * Validate email address format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate Indian mobile number
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate Indian pincode
     */
    public static boolean isValidPincode(String pincode) {
        return pincode != null && PINCODE_PATTERN.matcher(pincode).matches();
    }
    
    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate password strength
     * Minimum 8 characters, at least one letter and one number
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasLetter && hasDigit;
    }
    
    /**
     * Validate age for blood donation (18-65 years)
     */
    public static boolean isValidDonorAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            return false;
        }
        
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= 18 && age <= 65;
    }
    
    /**
     * Validate weight for blood donation (minimum 50kg)
     */
    public static boolean isValidDonorWeight(double weight) {
        return weight >= 50.0 && weight <= 200.0; // Reasonable range
    }
    
    /**
     * Sanitize string input (prevent SQL injection)
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+]", "");
    }
    
    /**
     * Validate blood quantity (ml)
     */
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0 && quantity <= 10000; // Max 10 liters
    }
    
    /**
     * Validate geographic coordinates
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }
    
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }
    
    /**
     * Format phone number for display
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() != 10) {
            return phone;
        }
        
        return String.format("+91 %s-%s-%s",
            phone.substring(0, 5),
            phone.substring(5, 8),
            phone.substring(8));
    }
    
    /**
     * Get password strength indicator
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return "Weak";
        }
        
        int score = 0;
        
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        
        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }
}