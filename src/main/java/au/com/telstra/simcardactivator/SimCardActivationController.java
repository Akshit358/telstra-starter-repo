package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SimCardActivationController {
    
    @Autowired
    private SimCardActivationService activationService;
    
    @PostMapping("/activate")
    public ResponseEntity<String> activateSimCard(@RequestBody SimCardActivationRequest request) {
        try {
            System.out.println("Received activation request: " + request);
            
            // Validate request
            if (request.getIccid() == null || request.getIccid().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("ICCID is required");
            }
            
            if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Customer email is required");
            }
            
            // Call the activation service
            boolean success = activationService.activateSimCard(request.getIccid());
            
            // Print the result
            String result = success ? "SUCCESS" : "FAILURE";
            System.out.println("SIM card activation result for ICCID " + request.getIccid() + ": " + result);
            
            return ResponseEntity.ok("Activation " + result + " for ICCID: " + request.getIccid());
            
        } catch (Exception e) {
            System.err.println("Error processing activation request: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing activation request: " + e.getMessage());
        }
    }
}
