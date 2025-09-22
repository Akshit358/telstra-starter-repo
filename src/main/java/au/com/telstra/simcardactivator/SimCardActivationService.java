package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SimCardActivationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimCardActivationService.class);
    
    @Value("${actuator.service.url}")
    private String actuatorUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private SimCardActivationRepository repository;
    
    public boolean activateSimCard(String iccid, String customerEmail) {
        logger.info("Starting SIM card activation for ICCID: {} and customer: {}", iccid, customerEmail);
        
        try {
            // Check if this ICCID has been activated before
            if (repository.existsByIccid(iccid)) {
                logger.warn("ICCID {} has already been activated", iccid);
                Optional<SimCardActivationRecord> existingRecord = repository.findByIccid(iccid);
                if (existingRecord.isPresent()) {
                    SimCardActivationRecord record = existingRecord.get();
                    logger.info("Previous activation result for ICCID {}: {}", iccid, record.isActive());
                    return record.isActive();
                }
            }
            
            // Create request for actuator
            ActuatorRequest actuatorRequest = new ActuatorRequest(iccid);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<ActuatorRequest> requestEntity = new HttpEntity<>(actuatorRequest, headers);
            
            logger.debug("Calling actuator service at: {}", actuatorUrl);
            
            // Make POST request to actuator
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                actuatorUrl, 
                requestEntity, 
                ActuatorResponse.class
            );
            
            boolean success = response.getBody() != null && response.getBody().isSuccess();
            String responseBody = response.getBody() != null ? response.getBody().toString() : "null";
            
            logger.info("Actuator response for ICCID {}: {}", iccid, responseBody);
            
            // Save the activation record
            SimCardActivationRecord record = new SimCardActivationRecord(iccid, customerEmail, success);
            record.setActuatorResponse(responseBody);
            repository.save(record);
            
            logger.info("SIM card activation {} for ICCID: {}", success ? "SUCCESS" : "FAILED", iccid);
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error calling actuator service for ICCID {}: {}", iccid, e.getMessage(), e);
            
            // Save failed activation record
            SimCardActivationRecord record = new SimCardActivationRecord(iccid, customerEmail, false);
            record.setActuatorResponse("Error: " + e.getMessage());
            repository.save(record);
            
            return false;
        }
    }
    
    public List<SimCardActivationRecord> getAllActivationRecords() {
        logger.debug("Retrieving all activation records");
        return repository.findAll();
    }
    
    public Optional<SimCardActivationRecord> getActivationRecordByIccid(String iccid) {
        logger.debug("Retrieving activation record for ICCID: {}", iccid);
        return repository.findByIccid(iccid);
    }
    
    public Optional<SimCardActivationRecord> getActivationRecordById(Long id) {
        logger.debug("Retrieving activation record for ID: {}", id);
        return repository.findById(id);
    }
    
    public List<SimCardActivationRecord> getActivationRecordsByCustomerEmail(String customerEmail) {
        logger.debug("Retrieving activation records for customer: {}", customerEmail);
        return repository.findByCustomerEmail(customerEmail);
    }
    
    public List<SimCardActivationRecord> getActiveSimCards() {
        logger.debug("Retrieving all active SIM cards");
        return repository.findByActiveTrue();
    }
    
    public List<SimCardActivationRecord> getInactiveSimCards() {
        logger.debug("Retrieving all inactive SIM cards");
        return repository.findByActiveFalse();
    }
}
