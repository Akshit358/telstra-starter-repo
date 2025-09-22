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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = SimCardActivator.class, loader = SpringBootContextLoader.class)
public class SimCardActivatorStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SimCardActivationService activationService;

    private SimCardActivationRequest request;
    private ResponseEntity<String> response;
    private ResponseEntity<SimCardResponse> simCardResponse;
    private List<SimCardActivationRecord> allRecords;

    @Given("the SIM card activation service is running")
    public void the_sim_card_activation_service_is_running() {
        // Service is running via Spring Boot test context
        assertNotNull(restTemplate);
    }

    @Given("the actuator service is available")
    public void the_actuator_service_is_available() {
        // This would require the actuator service to be running
        // For now, we'll test with the service unavailable scenario
    }

    @Given("I have a valid SIM card with ICCID {string}")
    public void i_have_a_valid_sim_card_with_iccid(String iccid) {
        request = new SimCardActivationRequest();
        request.setIccid(iccid);
    }

    @Given("I have an invalid SIM card with ICCID {string}")
    public void i_have_an_invalid_sim_card_with_iccid(String iccid) {
        request = new SimCardActivationRequest();
        request.setIccid(iccid);
    }

    @Given("the customer email is {string}")
    public void the_customer_email_is(String email) {
        if (request == null) {
            request = new SimCardActivationRequest();
        }
        request.setCustomerEmail(email);
    }

    @When("I submit the activation request")
    public void i_submit_the_activation_request() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SimCardActivationRequest> entity = new HttpEntity<>(request, headers);
        
        response = restTemplate.postForEntity("/api/activate", entity, String.class);
    }

    @When("I query the database for all activation records")
    public void i_query_the_database_for_all_activation_records() {
        allRecords = activationService.getAllActivationRecords();
    }

    @When("I query the database for SIM card with ID {long}")
    public void i_query_the_database_for_sim_card_with_id(Long id) {
        ResponseEntity<SimCardResponse> response = restTemplate.getForEntity("/api/simcard/" + id, SimCardResponse.class);
        simCardResponse = response;
    }

    @Then("the activation should be successful")
    public void the_activation_should_be_successful() {
        assertNotNull(response);
        assertTrue(response.getBody().contains("SUCCESS"));
    }

    @Then("the activation should fail with validation error")
    public void the_activation_should_fail_with_validation_error() {
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("required"));
    }

    @Then("the activation should fail due to actuator service unavailable")
    public void the_activation_should_fail_due_to_actuator_service_unavailable() {
        assertNotNull(response);
        assertTrue(response.getBody().contains("FAILURE"));
    }

    @Then("the activation record should be saved")
    public void the_activation_record_should_be_saved() {
        assertNotNull(allRecords);
        assertFalse(allRecords.isEmpty());
        
        // Find the record for our test ICCID
        boolean found = false;
        for (SimCardActivationRecord record : allRecords) {
            if (record.getIccid().equals(request.getIccid())) {
                found = true;
                assertEquals(request.getCustomerEmail(), record.getCustomerEmail());
                break;
            }
        }
        assertTrue(found, "Activation record should be saved in database");
    }

    @Then("I should receive a success response")
    public void i_should_receive_a_success_response() {
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Then("I should receive a failure response")
    public void i_should_receive_a_failure_response() {
        assertNotNull(response);
        assertTrue(response.getBody().contains("FAILURE"));
    }

    @Then("the SIM card should be marked as active")
    public void the_sim_card_should_be_marked_as_active() {
        assertNotNull(allRecords);
        boolean found = false;
        for (SimCardActivationRecord record : allRecords) {
            if (record.getIccid().equals(request.getIccid())) {
                found = true;
                assertTrue(record.isActive(), "SIM card should be marked as active");
                break;
            }
        }
        assertTrue(found, "SIM card record should exist");
    }

    @Then("the SIM card should be marked as inactive")
    public void the_sim_card_should_be_marked_as_inactive() {
        assertNotNull(allRecords);
        boolean found = false;
        for (SimCardActivationRecord record : allRecords) {
            if (record.getIccid().equals(request.getIccid())) {
                found = true;
                assertFalse(record.isActive(), "SIM card should be marked as inactive");
                break;
            }
        }
        assertTrue(found, "SIM card record should exist");
    }

    @Then("I should receive the SIM card details")
    public void i_should_receive_the_sim_card_details() {
        assertNotNull(simCardResponse);
        assertEquals(200, simCardResponse.getStatusCodeValue());
        assertNotNull(simCardResponse.getBody());
    }

    @Then("the response should contain ICCID {string}")
    public void the_response_should_contain_iccid(String expectedIccid) {
        assertNotNull(simCardResponse.getBody());
        assertEquals(expectedIccid, simCardResponse.getBody().getIccid());
    }

    @Then("the response should contain customer email {string}")
    public void the_response_should_contain_customer_email(String expectedEmail) {
        assertNotNull(simCardResponse.getBody());
        assertEquals(expectedEmail, simCardResponse.getBody().getCustomerEmail());
    }

    @Then("the response should show active status as {string}")
    public void the_response_should_show_active_status_as(String expectedStatus) {
        assertNotNull(simCardResponse.getBody());
        boolean expectedActive = "true".equalsIgnoreCase(expectedStatus);
        assertEquals(expectedActive, simCardResponse.getBody().isActive());
    }
}
