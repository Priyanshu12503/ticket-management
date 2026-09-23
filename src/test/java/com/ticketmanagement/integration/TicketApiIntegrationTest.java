package com.ticketmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.ListTicketsResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.dto.UpdateTicketRequest;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Ticket API endpoints.
 * Tests complete request/response cycles through Spring MVC.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class TicketApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    // Create Ticket Tests

    @Test
    @DisplayName("POST /api/tickets - Create ticket returns 201")
    void createTicket_WithValidData_Returns201() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
            "Database connection failing",
            "App crashes on startup when connecting to DB",
            Priority.HIGH,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.title").value("Database connection failing"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andReturn();

        TicketResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            TicketResponse.class
        );

        // Verify persistence
        assertThat(ticketRepository.findById(UUID.fromString(response.id())))
            .isPresent()
            .get()
            .hasFieldOrPropertyWithValue("title", "Database connection failing")
            .hasFieldOrPropertyWithValue("status", TicketStatus.OPEN);
    }

    @Test
    @DisplayName("POST /api/tickets - Blank title returns 400")
    void createTicket_WithBlankTitle_Returns400() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
            "",
            "Description",
            Priority.MEDIUM,
            null
        );

        mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details[0].field").value("title"));
    }

    @Test
    @DisplayName("POST /api/tickets - Null priority returns 400")
    void createTicket_WithNullPriority_Returns400() throws Exception {
        String request = """
            {
              "title": "Test",
              "description": "Test",
              "priority": null
            }
            """;

        mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    // Get Ticket Tests

    @Test
    @DisplayName("GET /api/tickets/{id} - Returns ticket with comments")
    void getTicket_WithValidId_ReturnsTicketWithComments() throws Exception {
        // Create ticket
        CreateTicketRequest createReq = new CreateTicketRequest(
            "Test Ticket",
            "Test Description",
            Priority.MEDIUM,
            null
        );

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        TicketResponse created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            TicketResponse.class
        );

        // Get ticket
        mockMvc.perform(get("/api/tickets/{id}", created.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.id()))
            .andExpect(jsonPath("$.title").value("Test Ticket"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    @DisplayName("GET /api/tickets/{id} - Invalid ID returns 404")
    void getTicket_WithInvalidId_Returns404() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/api/tickets/{id}", invalidId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Ticket not found"));
    }

    // List Tickets Tests

    @Test
    @DisplayName("GET /api/tickets - List with pagination")
    void listTickets_WithPagination_ReturnsList() throws Exception {
        // Create 3 tickets
        for (int i = 0; i < 3; i++) {
            CreateTicketRequest req = new CreateTicketRequest(
                "Ticket " + i,
                "Description " + i,
                Priority.MEDIUM,
                null
            );
            mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/tickets")
            .param("page", "1")
            .param("pageSize", "20")
            .param("sortBy", "createdAt")
            .param("sortDir", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(3)))
            .andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @DisplayName("GET /api/tickets - Invalid page size returns 400")
    void listTickets_WithInvalidPageSize_Returns400() throws Exception {
        mockMvc.perform(get("/api/tickets")
            .param("page", "1")
            .param("pageSize", "101"))
            .andExpect(status().isBadRequest());
    }

    // Filter by Status Tests

    @Test
    @DisplayName("GET /api/tickets?status=OPEN - Filter by status")
    void listTickets_FilterByStatus_ReturnsFilteredList() throws Exception {
        // Create tickets with different statuses
        CreateTicketRequest req1 = new CreateTicketRequest("Open Ticket", "Desc", Priority.HIGH, null);
        CreateTicketRequest req2 = new CreateTicketRequest("Another Ticket", "Desc", Priority.MEDIUM, null);

        mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // Filter by OPEN status
        mockMvc.perform(get("/api/tickets")
            .param("status", "OPEN")
            .param("page", "1")
            .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].status").value("OPEN"));
    }

    // Search Tests

    @Test
    @DisplayName("GET /api/tickets/search?keyword=database - Search tickets")
    void searchTickets_WithKeyword_ReturnsMatches() throws Exception {
        CreateTicketRequest req = new CreateTicketRequest(
            "Database Issue",
            "Database connection failing",
            Priority.HIGH,
            null
        );

        mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        // Search
        mockMvc.perform(get("/api/tickets/search")
            .param("keyword", "database")
            .param("page", "1")
            .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].title").value("Database Issue"));
    }

    @Test
    @DisplayName("GET /api/tickets/search with blank keyword returns 400")
    void searchTickets_WithBlankKeyword_Returns400() throws Exception {
        mockMvc.perform(get("/api/tickets/search")
            .param("keyword", "")
            .param("page", "1")
            .param("pageSize", "20"))
            .andExpect(status().isBadRequest());
    }

    // Update Ticket Tests

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Update ticket fields")
    void updateTicket_WithValidData_UpdatesFields() throws Exception {
        // Create ticket
        CreateTicketRequest createReq = new CreateTicketRequest(
            "Original Title",
            "Original Description",
            Priority.LOW,
            null
        );

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        TicketResponse created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            TicketResponse.class
        );

        // Update ticket
        UpdateTicketRequest updateReq = new UpdateTicketRequest(
            "Updated Title",
            "Updated Description",
            Priority.HIGH,
            "john@example.com"
        );

        mockMvc.perform(patch("/api/tickets/{id}", created.id())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.assignee").value("john@example.com"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Partial update")
    void updateTicket_WithPartialData_UpdatesOnlyProvidedFields() throws Exception {
        // Create ticket
        CreateTicketRequest createReq = new CreateTicketRequest(
            "Original Title",
            "Original Description",
            Priority.LOW,
            null
        );

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        TicketResponse created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            TicketResponse.class
        );

        // Partial update - only title
        UpdateTicketRequest updateReq = new UpdateTicketRequest(
            "New Title",
            null,
            null,
            null
        );

        mockMvc.perform(patch("/api/tickets/{id}", created.id())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New Title"))
            .andExpect(jsonPath("$.description").value("Original Description"))
            .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Invalid ID returns 404")
    void updateTicket_WithInvalidId_Returns404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        UpdateTicketRequest updateReq = new UpdateTicketRequest("Title", null, null, null);

        mockMvc.perform(patch("/api/tickets/{id}", invalidId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isNotFound());
    }
}
