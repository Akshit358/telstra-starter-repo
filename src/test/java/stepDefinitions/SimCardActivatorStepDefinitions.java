package stepDefinitions;

import au.com.telstra.simcardactivator.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = SimCardActivator.class, loader = SpringBootContextLoader.class)
public class SimCardActivatorStepDefinitions {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private SimCardActivationRepository repository;
    
    private SimCardActivationRequest currentRequest;
    private ResponseEntity<String> activationResponse;
    private ResponseEntity<SimCardActivationRecord[]> recordsResponse;
    private ResponseEntity<SimCardActivationRecord> singleRecordResponse;
    private String currentIccid;
    private String currentCustomerEmail;
    
    @Given("the SIM card activation service is running")
    public void the_sim_card_activation_service_is_running() {
        // Service is already running due to SpringBootTest
        assertNotNull(restTemplate);
    }
    
    @Given("the actuator service is available")
    public void the_actuator_service_is_available() {
        // In a real test, you might want to mock the actuator service
        // For now, we'll assume it's available
    }
    
    @Given("I have a valid SIM card with ICCID {string}")
    public void i_have_a_valid_sim_card_with_iccid(String iccid) {
        this.currentIccid = iccid;
    }
    
    @Given("I have an invalid SIM card with ICCID {string}")
    public void i_have_an_invalid_sim_card_with_iccid(String iccid) {
        this.currentIccid = iccid;
    }
    
    @Given("the customer email is {string}")
    public void the_customer_email_is(String email) {
        this.currentCustomerEmail = email;
    }
    
    @When("I submit the activation request")
    public void i_submit_the_activation_request() {
        currentRequest = new SimCardActivationRequest(currentIccid, currentCustomerEmail);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SimCardActivationRequest> requestEntity = new HttpEntity<>(currentRequest, headers);
        
        activationResponse = restTemplate.postForEntity("/api/activate", requestEntity, String.class);
    }
    
    @Then("the activation should be successful")
    public void the_activation_should_be_successful() {
        assertEquals(200, activationResponse.getStatusCodeValue());
        assertTrue(activationResponse.getBody().contains("SUCCESS"));
    }
    
    @Then("the activation should fail with validation error")
    public void the_activation_should_fail_with_validation_error() {
        assertEquals(400, activationResponse.getStatusCodeValue());
        assertTrue(activationResponse.getBody().contains("required"));
    }
    
    @Then("I should receive a success response")
    public void i_should_receive_a_success_response() {
        assertEquals(200, activationResponse.getStatusCodeValue());
    }
    
    @Then("I should receive a bad request response")
    public void i_should_receive_a_bad_request_response() {
        assertEquals(400, activationResponse.getStatusCodeValue());
    }
    
    @Then("the activation record should be saved")
    public void the_activation_record_should_be_saved() {
        assertTrue(repository.existsByIccid(currentIccid));
    }
    
    @Given("there are existing activation records")
    public void there_are_existing_activation_records() {
        // Create some test data
        SimCardActivationRecord record1 = new SimCardActivationRecord("11111111111111111111", "test1@example.com", true);
        SimCardActivationRecord record2 = new SimCardActivationRecord("22222222222222222222", "test2@example.com", false);
        repository.save(record1);
        repository.save(record2);
    }
    
    @When("I request all activation records")
    public void i_request_all_activation_records() {
        recordsResponse = restTemplate.getForEntity("/api/activations", SimCardActivationRecord[].class);
    }
    
    @Then("I should receive a list of activation records")
    public void i_should_receive_a_list_of_activation_records() {
        assertEquals(200, recordsResponse.getStatusCodeValue());
        assertTrue(recordsResponse.getBody().length > 0);
    }
    
    @Given("there is an activation record for ICCID {string}")
    public void there_is_an_activation_record_for_iccid(String iccid) {
        SimCardActivationRecord record = new SimCardActivationRecord(iccid, "test@example.com", true);
        repository.save(record);
    }
    
    @When("I request the activation record for ICCID {string}")
    public void i_request_the_activation_record_for_iccid(String iccid) {
        singleRecordResponse = restTemplate.getForEntity("/api/activations/" + iccid, SimCardActivationRecord.class);
    }
    
    @Then("I should receive the activation record")
    public void i_should_receive_the_activation_record() {
        assertEquals(200, singleRecordResponse.getStatusCodeValue());
        assertNotNull(singleRecordResponse.getBody());
    }
    
    @Given("there are activation records for customer {string}")
    public void there_are_activation_records_for_customer(String customerEmail) {
        SimCardActivationRecord record1 = new SimCardActivationRecord("33333333333333333333", customerEmail, true);
        SimCardActivationRecord record2 = new SimCardActivationRecord("44444444444444444444", customerEmail, false);
        repository.save(record1);
        repository.save(record2);
    }
    
    @When("I request activation records for customer {string}")
    public void i_request_activation_records_for_customer(String customerEmail) {
        recordsResponse = restTemplate.getForEntity("/api/activations/customer/" + customerEmail, SimCardActivationRecord[].class);
    }
    
    @Then("I should receive the customer's activation records")
    public void i_should_receive_the_customer_s_activation_records() {
        assertEquals(200, recordsResponse.getStatusCodeValue());
        assertTrue(recordsResponse.getBody().length > 0);
    }
    
    @Given("there are both successful and failed activation records")
    public void there_are_both_successful_and_failed_activation_records() {
        SimCardActivationRecord successRecord = new SimCardActivationRecord("55555555555555555555", "test@example.com", true);
        SimCardActivationRecord failedRecord = new SimCardActivationRecord("66666666666666666666", "test@example.com", false);
        repository.save(successRecord);
        repository.save(failedRecord);
    }
    
    @When("I request successful activations")
    public void i_request_successful_activations() {
        recordsResponse = restTemplate.getForEntity("/api/activations/successful", SimCardActivationRecord[].class);
    }
    
    @When("I request failed activations")
    public void i_request_failed_activations() {
        recordsResponse = restTemplate.getForEntity("/api/activations/failed", SimCardActivationRecord[].class);
    }
    
    @Then("I should receive only successful activation records")
    public void i_should_receive_only_successful_activation_records() {
        assertEquals(200, recordsResponse.getStatusCodeValue());
        for (SimCardActivationRecord record : recordsResponse.getBody()) {
            assertTrue(record.isActivationSuccess());
        }
    }
    
    @Then("I should receive only failed activation records")
    public void i_should_receive_only_failed_activation_records() {
        assertEquals(200, recordsResponse.getStatusCodeValue());
        for (SimCardActivationRecord record : recordsResponse.getBody()) {
            assertFalse(record.isActivationSuccess());
        }
    }
    
    @Given("there is already an activation record for ICCID {string}")
    public void there_is_already_an_activation_record_for_iccid(String iccid) {
        SimCardActivationRecord existingRecord = new SimCardActivationRecord(iccid, "existing@example.com", true);
        repository.save(existingRecord);
    }
    
    @When("I try to activate the same ICCID {string} again")
    public void i_try_to_activate_the_same_iccid_again(String iccid) {
        currentIccid = iccid;
        currentCustomerEmail = "new@example.com";
        i_submit_the_activation_request();
    }
    
    @Then("the system should return the existing activation result")
    public void the_system_should_return_the_existing_activation_result() {
        assertEquals(200, activationResponse.getStatusCodeValue());
        assertTrue(activationResponse.getBody().contains("SUCCESS"));
    }
    
    @Then("no new activation record should be created")
    public void no_new_activation_record_should_be_created() {
        // Check that only one record exists for this ICCID
        long count = repository.findAll().stream()
            .filter(record -> record.getIccid().equals(currentIccid))
            .count();
        assertEquals(1, count);
    }
}
