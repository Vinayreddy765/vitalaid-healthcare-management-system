package controller;

import model.*;

/**
 * SessionManager - Singleton class for managing user session
 * Stores current logged-in user and their associated profile data
 * Thread-safe implementation for multi-threaded environments
 * 
 * @author VitalAid Team
 * @version 1.0
 */
public class SessionManager {
    
    // Singleton instance
    private static SessionManager instance;
    
    // Session data
    private static User currentUser;
    private static Donor currentDonor;
    private static Patient currentPatient;
    private static Hospital currentHospital;
    
    // Session metadata
    private static long sessionStartTime;
    private static String sessionId;
    
    /**
     * Private constructor to prevent instantiation
     */
    private SessionManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Initialize new session
     */
    public static synchronized void initializeSession(User user) {
        currentUser = user;
        sessionStartTime = System.currentTimeMillis();
        sessionId = generateSessionId();
        
        System.out.println("✓ Session initialized for: " + user.getUsername());
        System.out.println("  Session ID: " + sessionId);
        System.out.println("  User Type: " + user.getUserType());
    }
    
    /**
     * Generate unique session ID
     */
    private static String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Get current logged-in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get current donor (if user is donor)
     */
    public static Donor getCurrentDonor() {
        return currentDonor;
    }
    
    /**
     * Get current patient (if user is patient)
     */
    public static Patient getCurrentPatient() {
        return currentPatient;
    }
    
    /**
     * Get current hospital (if user is hospital)
     */
    public static Hospital getCurrentHospital() {
        return currentHospital;
    }
    
    /**
     * Get session start time
     */
    public static long getSessionStartTime() {
        return sessionStartTime;
    }
    
    /**
     * Get session ID
     */
    public static String getSessionId() {
        return sessionId;
    }
    
    /**
     * Get session duration in minutes
     */
    public static long getSessionDuration() {
        if (sessionStartTime == 0) return 0;
        return (System.currentTimeMillis() - sessionStartTime) / (1000 * 60);
    }
    
    // ==================== SETTERS ====================
    
    /**
     * Set current user
     */
    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            initializeSession(user);
        }
    }
    
    /**
     * Set current donor
     */
    public static synchronized void setCurrentDonor(Donor donor) {
        currentDonor = donor;
        if (donor != null) {
            System.out.println("✓ Donor profile loaded: " + donor.getFullName());
        }
    }
    
    /**
     * Set current patient
     */
    public static synchronized void setCurrentPatient(Patient patient) {
        currentPatient = patient;
        if (patient != null) {
            System.out.println("✓ Patient profile loaded: " + patient.getFullName());
        }
    }
    
    /**
     * Set current hospital
     */
    public static synchronized void setCurrentHospital(Hospital hospital) {
        currentHospital = hospital;
        if (hospital != null) {
            System.out.println("✓ Hospital profile loaded: " + hospital.getHospitalName());
        }
    }
    
    // ==================== SESSION MANAGEMENT ====================
    
    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user is donor
     */
    public static boolean isDonor() {
        return currentUser != null && 
               currentUser.getUserType() == User.UserType.DONOR &&
               currentDonor != null;
    }
    
    /**
     * Check if current user is patient
     */
    public static boolean isPatient() {
        return currentUser != null && 
               currentUser.getUserType() == User.UserType.PATIENT &&
               currentPatient != null;
    }
    
    /**
     * Check if current user is hospital
     */
    public static boolean isHospital() {
        return currentUser != null && 
               currentUser.getUserType() == User.UserType.HOSPITAL &&
               currentHospital != null;
    }
    
    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && 
               currentUser.getUserType() == User.UserType.ADMIN;
    }
    
    /**
     * Get current user type as string
     */
    public static String getCurrentUserType() {
        if (currentUser == null) return "NONE";
        return currentUser.getUserType().toString();
    }
    
    /**
     * Get current user ID
     */
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    /**
     * Get current user name
     */
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
    
    /**
     * Get current user email
     */
    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
    
    /**
     * Get current user phone
     */
    public static String getCurrentUserPhone() {
        return currentUser != null ? currentUser.getPhone() : "";
    }
    
    // ==================== SESSION ACTIONS ====================
    
    /**
     * Clear all session data (logout)
     */
    public static synchronized void clearSession() {
        if (currentUser != null) {
            System.out.println("✓ Session cleared for: " + currentUser.getUsername());
            System.out.println("  Session duration: " + getSessionDuration() + " minutes");
        }
        
        currentUser = null;
        currentDonor = null;
        currentPatient = null;
        currentHospital = null;
        sessionStartTime = 0;
        sessionId = null;
    }
    
    /**
     * Refresh user data
     */
    public static void refreshUserData(User updatedUser) {
        if (currentUser != null && currentUser.getUserId() == updatedUser.getUserId()) {
            currentUser = updatedUser;
            System.out.println("✓ User data refreshed");
        }
    }
    
    /**
     * Refresh donor data
     */
    public static void refreshDonorData(Donor updatedDonor) {
        if (currentDonor != null && currentDonor.getDonorId() == updatedDonor.getDonorId()) {
            currentDonor = updatedDonor;
            System.out.println("✓ Donor data refreshed");
        }
    }
    
    /**
     * Refresh patient data
     */
    public static void refreshPatientData(Patient updatedPatient) {
        if (currentPatient != null && currentPatient.getPatientId() == updatedPatient.getPatientId()) {
            currentPatient = updatedPatient;
            System.out.println("✓ Patient data refreshed");
        }
    }
    
    /**
     * Refresh hospital data
     */
    public static void refreshHospitalData(Hospital updatedHospital) {
        if (currentHospital != null && currentHospital.getHospitalId() == updatedHospital.getHospitalId()) {
            currentHospital = updatedHospital;
            System.out.println("✓ Hospital data refreshed");
        }
    }
    
    // ==================== SESSION VALIDATION ====================
    
    /**
     * Check if session is valid
     */
    public static boolean isSessionValid() {
        return isLoggedIn() && sessionId != null;
    }
    
    /**
     * Check if session has expired (optional timeout implementation)
     */
    public static boolean isSessionExpired(long timeoutMinutes) {
        if (!isLoggedIn()) return true;
        return getSessionDuration() > timeoutMinutes;
    }
    
    /**
     * Get session info as string
     */
    public static String getSessionInfo() {
        if (!isLoggedIn()) {
            return "No active session";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Session Information:\n");
        info.append("  Session ID: ").append(sessionId).append("\n");
        info.append("  Username: ").append(currentUser.getUsername()).append("\n");
        info.append("  User Type: ").append(currentUser.getUserType()).append("\n");
        info.append("  Duration: ").append(getSessionDuration()).append(" minutes\n");
        info.append("  Email: ").append(currentUser.getEmail()).append("\n");
        
        if (isDonor()) {
            info.append("  Donor Name: ").append(currentDonor.getFullName()).append("\n");
            info.append("  Blood Group: ").append(currentDonor.getBloodGroup().getDisplay()).append("\n");
        } else if (isPatient()) {
            info.append("  Patient Name: ").append(currentPatient.getFullName()).append("\n");
            info.append("  Blood Group: ").append(currentPatient.getBloodGroup().getDisplay()).append("\n");
        } else if (isHospital()) {
            info.append("  Hospital: ").append(currentHospital.getHospitalName()).append("\n");
            info.append("  City: ").append(currentHospital.getCity()).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * Print session info to console (for debugging)
     */
    public static void printSessionInfo() {
        System.out.println("\n" + getSessionInfo());
    }
}
