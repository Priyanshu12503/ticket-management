package com.ticketmanagement.controller;

import com.ticketmanagement.dto.ListTicketsResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Health check controller to verify the backend is running.
 */
@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * Health check endpoint
     * GET /health
     *
     * @return 200 OK with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check requested");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Ticket Management API is running");
        return ResponseEntity.ok(response);
    }

    /**
     * API health check endpoint
     * GET /api/health
     *
     * @return 200 OK with API status
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> apiHealth() {
        logger.info("API health check requested");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Support Ticket Management System");
        response.put("version", "1.0.0");
        response.put("database", "H2 In-Memory");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint with sample data
     * GET /api/test-data
     *
     * @return 200 OK with sample tickets
     */
    @GetMapping("/api/test-data")
    public ResponseEntity<ListTicketsResponse> testData() {
        logger.info("Test data endpoint called");
        
        List<TicketResponse> tickets = new ArrayList<>();
        
        TicketResponse ticket1 = new TicketResponse(
            UUID.randomUUID().toString(),
            "Test Ticket 1",
            "This is a test ticket to verify the API is working correctly",
            TicketStatus.OPEN,
            Priority.HIGH,
            "john@example.com",
            new ArrayList<>(),
            OffsetDateTime.now().toString(),
            OffsetDateTime.now().toString()
        );
        
        TicketResponse ticket2 = new TicketResponse(
            UUID.randomUUID().toString(),
            "Test Ticket 2",
            "Another test ticket to verify pagination",
            TicketStatus.IN_PROGRESS,
            Priority.MEDIUM,
            "jane@example.com",
            new ArrayList<>(),
            OffsetDateTime.now().toString(),
            OffsetDateTime.now().toString()
        );
        
        tickets.add(ticket1);
        tickets.add(ticket2);
        
        ListTicketsResponse response = new ListTicketsResponse(tickets, 2, 1, 20);
        return ResponseEntity.ok(response);
    }
}
