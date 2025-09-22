package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class SimCardActivationService {
    
    private static final String ACTUATOR_URL = "http://localhost:8444/actuate";
    
    @Autowired
    private RestTemplate restTemplate;
    
    public boolean activateSimCard(String iccid) {
        try {
            // Create request for actuator
            ActuatorRequest actuatorRequest = new ActuatorRequest(iccid);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<ActuatorRequest> requestEntity = new HttpEntity<>(actuatorRequest, headers);
            
            // Make POST request to actuator
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                ACTUATOR_URL, 
                requestEntity, 
                ActuatorResponse.class
            );
            
            // Return the success status
            return response.getBody() != null && response.getBody().isSuccess();
            
        } catch (Exception e) {
            System.err.println("Error calling actuator service: " + e.getMessage());
            return false;
        }
    }
}
