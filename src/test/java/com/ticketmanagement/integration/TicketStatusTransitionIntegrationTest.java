package com.ticketmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.dto.UpdateStatusRequest;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.Ticket;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ticket status transitions via API.
 * Tests the complete state machine through REST endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class TicketStatusTransitionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    private UUID ticketId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        ticket = new Ticket("Test", "Description", Priority.HIGH);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        ticketId = saved.getId();
    }

    // Valid Transitions

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - OPEN → IN_PROGRESS succeeds")
    void updateStatus_OpenToInProgress_Succeeds() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - OPEN → CANCELLED succeeds")
    void updateStatus_OpenToCancelled_Succeeds() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.CANCELLED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - IN_PROGRESS → RESOLVED succeeds")
    void updateStatus_InProgressToResolved_Succeeds() throws Exception {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.RESOLVED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - IN_PROGRESS → CANCELLED succeeds")
    void updateStatus_InProgressToCancelled_Succeeds() throws Exception {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.CANCELLED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - RESOLVED → CLOSED succeeds")
    void updateStatus_ResolvedToClosed_Succeeds() throws Exception {
        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.CLOSED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    // Invalid Transitions

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - OPEN → RESOLVED returns 409")
    void updateStatus_OpenToResolved_Returns409() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.RESOLVED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - RESOLVED → OPEN returns 409")
    void updateStatus_ResolvedToOpen_Returns409() throws Exception {
        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.OPEN);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - CLOSED → OPEN returns 409 (terminal)")
    void updateStatus_ClosedToOpen_Returns409() throws Exception {
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.OPEN);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - CANCELLED → IN_PROGRESS returns 409 (terminal)")
    void updateStatus_CancelledToInProgress_Returns409() throws Exception {
        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - Invalid ID returns 404")
    void updateStatus_WithInvalidId_Returns404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/{id}/status", invalidId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
}
