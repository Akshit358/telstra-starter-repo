package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SimCardActivationController {
    
    private static final Logger logger = LoggerFactory.getLogger(SimCardActivationController.class);
    
    @Autowired
    private SimCardActivationService activationService;
    
    @PostMapping("/activate")
    public ResponseEntity<String> activateSimCard(@RequestBody SimCardActivationRequest request) {
        try {
            logger.info("Received activation request: {}", request);
            
            // Validate request
            if (request.getIccid() == null || request.getIccid().trim().isEmpty()) {
                logger.warn("Invalid request: ICCID is required");
                return ResponseEntity.badRequest().body("ICCID is required");
            }
            
            if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
                logger.warn("Invalid request: Customer email is required");
                return ResponseEntity.badRequest().body("Customer email is required");
            }
            
            // Call the activation service
            boolean success = activationService.activateSimCard(request.getIccid(), request.getCustomerEmail());
            
            // Return the result
            String result = success ? "SUCCESS" : "FAILURE";
            logger.info("SIM card activation result for ICCID {}: {}", request.getIccid(), result);
            
            return ResponseEntity.ok("Activation " + result + " for ICCID: " + request.getIccid());
            
        } catch (Exception e) {
            logger.error("Error processing activation request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing activation request: " + e.getMessage());
        }
    }
    
    @GetMapping("/activations")
    public ResponseEntity<List<SimCardActivationRecord>> getAllActivations() {
        try {
            logger.info("Retrieving all activation records");
            List<SimCardActivationRecord> records = activationService.getAllActivationRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving activation records: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/activations/{iccid}")
    public ResponseEntity<SimCardActivationRecord> getActivationByIccid(@PathVariable String iccid) {
        try {
            logger.info("Retrieving activation record for ICCID: {}", iccid);
            Optional<SimCardActivationRecord> record = activationService.getActivationRecordByIccid(iccid);
            
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("No activation record found for ICCID: {}", iccid);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving activation record for ICCID {}: {}", iccid, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/activations/customer/{customerEmail}")
    public ResponseEntity<List<SimCardActivationRecord>> getActivationsByCustomer(@PathVariable String customerEmail) {
        try {
            logger.info("Retrieving activation records for customer: {}", customerEmail);
            List<SimCardActivationRecord> records = activationService.getActivationRecordsByCustomerEmail(customerEmail);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving activation records for customer {}: {}", customerEmail, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/activations/successful")
    public ResponseEntity<List<SimCardActivationRecord>> getSuccessfulActivations() {
        try {
            logger.info("Retrieving successful activations");
            List<SimCardActivationRecord> records = activationService.getSuccessfulActivations();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving successful activations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/activations/failed")
    public ResponseEntity<List<SimCardActivationRecord>> getFailedActivations() {
        try {
            logger.info("Retrieving failed activations");
            List<SimCardActivationRecord> records = activationService.getFailedActivations();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving failed activations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
