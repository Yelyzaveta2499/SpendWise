package com.example.SpendWise.api;

import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API-level tests for the expenses REST endpoints.
 * Uses a plain RestTemplate against the RANDOM_PORT Spring Boot test server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseApiIT {

    @LocalServerPort
    private int port;

    private final UserRepository userRepository;

    private RestTemplate restTemplate;

    private final String username = "indiv";

    @Autowired
    ExpenseApiIT(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        // Build a simple RestTemplate instance for each test
        this.restTemplate = new RestTemplate();

        // Ensure there is at least a basic user record; adjust if  data.sql already creates it.
        userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPassword("password");
            return userRepository.save(user);
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void getExpenses_unauthenticated_returnsNon5xx() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(url("/api/expenses"), String.class);


        assertFalse(response.getStatusCode().is5xxServerError(),
                "Expected non-5xx status but got: " + response.getStatusCode());
    }

    @Test
    void createExpense_andFetchList_authenticated_basicSmoke() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Expense");
        payload.put("category", "Food & Dining");
        payload.put("amount", new BigDecimal("12.34"));
        payload.put("date", LocalDate.now().toString());

        headers.setBasicAuth(username, "password");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> createResponse = restTemplate
                .postForEntity(url("/api/expenses"), request, String.class);

        assertFalse(createResponse.getStatusCode().is5xxServerError(),
                "Create should not return 5xx but was: " + createResponse.getStatusCode());

        HttpHeaders listHeaders = new HttpHeaders();
        listHeaders.setBasicAuth(username, "password");
        HttpEntity<Void> listRequest = new HttpEntity<>(listHeaders);

        ResponseEntity<String> listResponse = restTemplate.exchange(
                url("/api/expenses"),
                HttpMethod.GET,
                listRequest,
                String.class
        );

        assertFalse(listResponse.getStatusCode().is5xxServerError(),
                "List should not return 5xx but was: " + listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
    }
}
