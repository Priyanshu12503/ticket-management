package com.ticketmanagement.service;

import com.ticketmanagement.exception.InvalidStatusTransitionException;
import com.ticketmanagement.mapper.TicketMapper;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.model.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test suite for ticket status state machine transitions.
 * Verifies all valid and invalid transitions are correctly enforced.
 */
@ExtendWith(MockitoExtension.class)
class TicketStatusTransitionTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    private UUID ticketId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        ticket = new Ticket("Test", "Description", Priority.HIGH);
        ticket.setId(ticketId);
    }

    // Valid Transitions - Should Succeed

    @Test
    @DisplayName("Valid: OPEN → IN_PROGRESS")
    void openToInProgress_Succeeds() {
        ticket.setStatus(TicketStatus.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(null);

        // Should not throw
        ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Valid: OPEN → CANCELLED")
    void openToCancelled_Succeeds() {
        ticket.setStatus(TicketStatus.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(null);

        // Should not throw
        ticketService.updateStatus(ticketId, TicketStatus.CANCELLED);
    }

    @Test
    @DisplayName("Valid: IN_PROGRESS → RESOLVED")
    void inProgressToResolved_Succeeds() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(null);

        // Should not throw
        ticketService.updateStatus(ticketId, TicketStatus.RESOLVED);
    }

    @Test
    @DisplayName("Valid: IN_PROGRESS → CANCELLED")
    void inProgressToCancelled_Succeeds() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(null);

        // Should not throw
        ticketService.updateStatus(ticketId, TicketStatus.CANCELLED);
    }

    @Test
    @DisplayName("Valid: RESOLVED → CLOSED")
    void resolvedToClosed_Succeeds() {
        ticket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(null);

        // Should not throw
        ticketService.updateStatus(ticketId, TicketStatus.CLOSED);
    }

    // Invalid Transitions - Should Throw

    @Test
    @DisplayName("Invalid: OPEN → RESOLVED")
    void openToResolved_ThrowsException() {
        ticket.setStatus(TicketStatus.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.RESOLVED)
        );
    }

    @Test
    @DisplayName("Invalid: OPEN → CLOSED")
    void openToClosed_ThrowsException() {
        ticket.setStatus(TicketStatus.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.CLOSED)
        );
    }

    @Test
    @DisplayName("Invalid: IN_PROGRESS → OPEN")
    void inProgressToOpen_ThrowsException() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.OPEN)
        );
    }

    @Test
    @DisplayName("Invalid: IN_PROGRESS → CLOSED")
    void inProgressToClosed_ThrowsException() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.CLOSED)
        );
    }

    @Test
    @DisplayName("Invalid: RESOLVED → OPEN")
    void resolvedToOpen_ThrowsException() {
        ticket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.OPEN)
        );
    }

    @Test
    @DisplayName("Invalid: RESOLVED → IN_PROGRESS")
    void resolvedToInProgress_ThrowsException() {
        ticket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS)
        );
    }

    @Test
    @DisplayName("Invalid: RESOLVED → CANCELLED")
    void resolvedToCancelled_ThrowsException() {
        ticket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Invalid: CLOSED → OPEN (terminal state)")
    void closedToOpen_ThrowsException() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.OPEN)
        );
    }

    @Test
    @DisplayName("Invalid: CLOSED → IN_PROGRESS (terminal state)")
    void closedToInProgress_ThrowsException() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS)
        );
    }

    @Test
    @DisplayName("Invalid: CLOSED → RESOLVED (terminal state)")
    void closedToResolved_ThrowsException() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.RESOLVED)
        );
    }

    @Test
    @DisplayName("Invalid: CLOSED → CANCELLED (terminal state)")
    void closedToCancelled_ThrowsException() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Invalid: CANCELLED → OPEN (terminal state)")
    void cancelledToOpen_ThrowsException() {
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.OPEN)
        );
    }

    @Test
    @DisplayName("Invalid: CANCELLED → IN_PROGRESS (terminal state)")
    void cancelledToInProgress_ThrowsException() {
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS)
        );
    }

    @Test
    @DisplayName("Invalid: CANCELLED → RESOLVED (terminal state)")
    void cancelledToResolved_ThrowsException() {
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.RESOLVED)
        );
    }

    @Test
    @DisplayName("Invalid: CANCELLED → CLOSED (terminal state)")
    void cancelledToClosed_ThrowsException() {
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () ->
            ticketService.updateStatus(ticketId, TicketStatus.CLOSED)
        );
    }
}
