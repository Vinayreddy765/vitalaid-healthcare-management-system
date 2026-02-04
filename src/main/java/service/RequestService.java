package service;

import dao.RequestDAO;
import model.Request;
import java.util.List;

/**
 * RequestService - Business logic layer for managing patient requests.
 * Handles creation and subsequent triggering of donor matching.
 */
public class RequestService {
    
    private final RequestDAO requestDAO;
    private final DonorMatchingService matchingService;
    
    public RequestService() {
        this.requestDAO = new RequestDAO();
        this.matchingService = new DonorMatchingService();
    }
    
    /**
     * Create a new request and trigger the donor matching process if applicable.
     * * @param request The Request object submitted by the patient.
     * @return The generated requestId, or -1 on failure.
     */
    public int submitRequest(Request request) {
        
        // 1. Persist the request to the database
        int requestId = requestDAO.createRequest(request);
        
        if (requestId > 0) {
            request.setRequestId(requestId); // Set the generated ID back to the object
            
            // 2. Check if donor matching is needed
            if (request.getRequestType() == Request.RequestType.BLOOD || 
                request.getRequestType() == Request.RequestType.PLASMA) {
                
                System.out.println("✓ Request is Blood/Plasma. Triggering Donor Matching...");
                
                // 3. Trigger the matching algorithm and notifications (Email, SMS, In-App)
                List<DonorMatchingService.DonorMatch> matches = matchingService.findMatchingDonors(request);
                
                System.out.println("✓ Donor matching complete. Found " + matches.size() + " matches.");
            } else {
                System.out.println("✓ Request is Ventilator. No donor matching required.");
            }
        } else {
            System.err.println("✗ Failed to persist request to database.");
        }
        
        return requestId;
    }
    
}