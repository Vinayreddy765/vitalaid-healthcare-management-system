package util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class EmailUtil {
    
    // Email configuration (configure these values)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "vitalaid@example.com";
    private static final String EMAIL_PASSWORD = "your_app_password";
    
    /**
     * Send email notification
     * * @param toEmail Recipient email
     * @param subject Email subject
     * @param body Email body
     * @return true if sent successfully
     */
    public static boolean sendEmail(String toEmail, String subject, String body) {
        // TODO: Implement using JavaMail API
        /*
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
        */
        
        // Placeholder implementation
        System.out.println("\n📧 EMAIL SENT:");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("─────────────────────────────\n");
        
        return true;
    }
    
    /**
     * Send donor notification email
     */
    public static boolean sendDonorNotification(String email, String donorName, 
                                               String bloodGroup, String urgency) {
        String subject = "🩸 Urgent Blood Donation Request";
        String body = String.format(
            "Dear %s,\n\n" +
            "A patient in your area urgently needs %s blood.\n" +
            "Urgency Level: %s\n\n" +
            "Your donation can save a life!\n" +
            "Please login to VitalAid to respond.\n\n" +
            "Thank you for being a lifesaver!\n\n" +
            "Best regards,\n" +
            "VitalAid Team",
            donorName, bloodGroup, urgency
        );
        
        return sendEmail(email, subject, body);
    }
    
    /**
     * Send request approval email to patient
     */
    public static boolean sendApprovalEmail(String email, String patientName, 
                                           String hospitalName) {
        String subject = "✅ Your Request Has Been Approved";
        String body = String.format(
            "Dear %s,\n\n" +
            "Good news! Your blood/plasma request has been approved by %s.\n\n" +
            "Please contact the hospital at your earliest convenience.\n\n" +
            "Hospital Contact: [Phone Number]\n" +
            "Address: [Hospital Address]\n\n" +
            "Best regards,\n" +
            "VitalAid Team",
            patientName, hospitalName
        );
        
        return sendEmail(email, subject, body);
    }
    
    /**
     * Send low stock alert email to hospital
     */
    public static boolean sendStockAlertEmail(String email, String hospitalName, 
                                             String bloodGroup, int currentStock) {
        String subject = "⚠️ Low Stock Alert - " + bloodGroup;
        String body = String.format(
            "Dear %s Administrator,\n\n" +
            "ALERT: Your blood stock for %s is critically low.\n\n" +
            "Current Stock: %d ml\n" +
            "Recommended Action: Immediate restocking required\n\n" +
            "Please coordinate with blood banks or initiate donation drives.\n\n" +
            "VitalAid System",
            hospitalName, bloodGroup, currentStock
        );
        
        return sendEmail(email, subject, body);
    }
}