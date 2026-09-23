package com.ticketmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmanagement.dto.AddCommentRequest;
import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.CommentResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.model.Priority;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Comment API endpoints.
 * Tests complete request/response cycles for comment operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class CommentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    private String ticketId;

    @BeforeEach
    void setUp() throws Exception {
        ticketRepository.deleteAll();

        // Create a ticket for use in tests
        CreateTicketRequest createReq = new CreateTicketRequest(
            "Test Ticket",
            "Test Description",
            Priority.HIGH,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        TicketResponse ticket = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            TicketResponse.class
        );
        ticketId = ticket.id();
    }

    // Add Comment Tests

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Add comment returns 201")
    void addComment_WithValidData_Returns201() throws Exception {
        AddCommentRequest request = new AddCommentRequest("This is a test comment");

        MvcResult result = mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.text").value("This is a test comment"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andReturn();

        CommentResponse comment = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            CommentResponse.class
        );

        // Verify comment is now included in ticket
        mockMvc.perform(get("/api/tickets/{id}", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comments", hasSize(1)))
            .andExpect(jsonPath("$.comments[0].text").value("This is a test comment"));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Blank text returns 400")
    void addComment_WithBlankText_Returns400() throws Exception {
        AddCommentRequest request = new AddCommentRequest("");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Text over 2000 chars returns 400")
    void addComment_WithTextOver2000Chars_Returns400() throws Exception {
        String longText = "a".repeat(2001);
        AddCommentRequest request = new AddCommentRequest(longText);

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Invalid ticket ID returns 404")
    void addComment_WithInvalidTicketId_Returns404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        AddCommentRequest request = new AddCommentRequest("Valid comment");

        mockMvc.perform(post("/api/tickets/{id}/comments", invalidId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    // Get Comments Tests

    @Test
    @DisplayName("GET /api/tickets/{id}/comments - Get comments returns list")
    void getComments_WithValidTicketId_ReturnsComments() throws Exception {
        // Add two comments
        AddCommentRequest req1 = new AddCommentRequest("First comment");
        AddCommentRequest req2 = new AddCommentRequest("Second comment");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // Get comments
        mockMvc.perform(get("/api/tickets/{id}/comments", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].text").value("First comment"))
            .andExpect(jsonPath("$[1].text").value("Second comment"));
    }

    @Test
    @DisplayName("GET /api/tickets/{id}/comments - No comments returns empty list")
    void getComments_WithNoComments_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}/comments", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/tickets/{id}/comments - Invalid ticket ID returns 404")
    void getComments_WithInvalidTicketId_Returns404() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/api/tickets/{id}/comments", invalidId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/tickets/{id}/comments - Comments ordered chronologically")
    void getComments_OrderedChronologically() throws Exception {
        // Add comments with small delays
        for (int i = 1; i <= 3; i++) {
            AddCommentRequest req = new AddCommentRequest("Comment " + i);
            mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
            Thread.sleep(10); // Ensure different timestamps
        }

        // Verify order (oldest first)
        mockMvc.perform(get("/api/tickets/{id}/comments", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].text").value("Comment 1"))
            .andExpect(jsonPath("$[1].text").value("Comment 2"))
            .andExpect(jsonPath("$[2].text").value("Comment 3"));
    }
}
