package com.ticketmanagement.service;

import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.ListTicketsResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.dto.UpdateTicketRequest;
import com.ticketmanagement.exception.InvalidStatusTransitionException;
import com.ticketmanagement.exception.TicketNotFoundException;
import com.ticketmanagement.exception.ValidationException;
import com.ticketmanagement.mapper.TicketMapper;
import com.ticketmanagement.model.Comment;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.model.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    private UUID ticketId;
    private Ticket ticket;
    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        ticket = new Ticket("Test Title", "Test Description", Priority.HIGH);
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);

        ticketResponse = new TicketResponse(
            ticketId.toString(),
            "Test Title",
            "Test Description",
            TicketStatus.OPEN,
            Priority.HIGH,
            null,
            new ArrayList<>(),
            "2026-09-22T10:00:00Z",
            "2026-09-22T10:00:00Z"
        );
    }

    // Create Ticket Tests

    @Test
    void createTicket_WithValidData_ReturnsTicketWithOpenStatus() {
        CreateTicketRequest request = new CreateTicketRequest("Title", "Description", Priority.HIGH, null);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.createTicket(request);

        assertNotNull(response);
        assertEquals(TicketStatus.OPEN, response.status());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithBlankTitle_ThrowsValidationException() {
        CreateTicketRequest request = new CreateTicketRequest("", "Description", Priority.HIGH, null);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    @Test
    void createTicket_WithTitleOver200Chars_ThrowsValidationException() {
        String longTitle = "a".repeat(201);
        CreateTicketRequest request = new CreateTicketRequest(longTitle, "Description", Priority.HIGH, null);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    @Test
    void createTicket_WithBlankDescription_ThrowsValidationException() {
        CreateTicketRequest request = new CreateTicketRequest("Title", "", Priority.HIGH, null);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    @Test
    void createTicket_WithDescriptionOver2000Chars_ThrowsValidationException() {
        String longDescription = "a".repeat(2001);
        CreateTicketRequest request = new CreateTicketRequest("Title", longDescription, Priority.HIGH, null);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    @Test
    void createTicket_WithNullPriority_ThrowsValidationException() {
        CreateTicketRequest request = new CreateTicketRequest("Title", "Description", null, null);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    @Test
    void createTicket_WithValidAssignee_Success() {
        CreateTicketRequest request = new CreateTicketRequest("Title", "Description", Priority.MEDIUM, "john@example.com");
        ticket.setAssignee("john@example.com");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.createTicket(request);

        assertNotNull(response);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithAssigneeOver100Chars_ThrowsValidationException() {
        String longAssignee = "a".repeat(101);
        CreateTicketRequest request = new CreateTicketRequest("Title", "Description", Priority.HIGH, longAssignee);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
    }

    // Get Ticket Tests

    @Test
    void getTicket_WithValidId_ReturnsTicket() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.getTicket(ticketId);

        assertNotNull(response);
        assertEquals(ticketId.toString(), response.id());
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void getTicket_WithInvalidId_ThrowsTicketNotFoundException() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.getTicket(ticketId));
    }

    // List Tickets Tests

    @Test
    void listTickets_WithValidPagination_ReturnsListResponse() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(ticketMapper.toResponseWithoutComments(ticket)).thenReturn(ticketResponse);

        ListTicketsResponse response = ticketService.listTickets(1, 20, "createdAt", "desc");

        assertNotNull(response);
        assertEquals(1, response.data().size());
        assertEquals(1, response.total());
        verify(ticketRepository).findAll(any(Pageable.class));
    }

    @Test
    void listTickets_WithPageLessThanOne_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> ticketService.listTickets(0, 20, "createdAt", "desc"));
    }

    @Test
    void listTickets_WithPageSizeZero_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> ticketService.listTickets(1, 0, "createdAt", "desc"));
    }

    @Test
    void listTickets_WithPageSizeOver100_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> ticketService.listTickets(1, 101, "createdAt", "desc"));
    }

    // Filter by Status Tests

    @Test
    void filterByStatus_WithOpenStatus_ReturnsOpenTickets() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findByStatus(TicketStatus.OPEN, any(Pageable.class))).thenReturn(page);
        when(ticketMapper.toResponseWithoutComments(ticket)).thenReturn(ticketResponse);

        ListTicketsResponse response = ticketService.filterByStatus(TicketStatus.OPEN, 1, 20, "createdAt", "desc");

        assertNotNull(response);
        assertEquals(1, response.data().size());
        verify(ticketRepository).findByStatus(TicketStatus.OPEN, any(Pageable.class));
    }

    // Search Tests

    @Test
    void searchByKeyword_WithValidKeyword_ReturnsMatchingTickets() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.searchByKeyword("database", any(Pageable.class))).thenReturn(page);
        when(ticketMapper.toResponseWithoutComments(ticket)).thenReturn(ticketResponse);

        ListTicketsResponse response = ticketService.searchByKeyword("database", 1, 20);

        assertNotNull(response);
        assertEquals(1, response.data().size());
        verify(ticketRepository).searchByKeyword("database", any(Pageable.class));
    }

    @Test
    void searchByKeyword_WithBlankKeyword_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> ticketService.searchByKeyword("", 1, 20));
    }

    @Test
    void searchByKeyword_WithNullKeyword_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> ticketService.searchByKeyword(null, 1, 20));
    }

    // Update Ticket Tests

    @Test
    void updateTicket_WithValidData_UpdatesTicket() {
        Ticket updated = new Ticket("Updated Title", "Updated Description", Priority.LOW);
        updated.setId(ticketId);
        UpdateTicketRequest request = new UpdateTicketRequest("Updated Title", "Updated Description", Priority.LOW, null);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(updated);
        when(ticketMapper.toResponse(updated)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.updateTicket(ticketId, request);

        assertNotNull(response);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void updateTicket_WithInvalidId_ThrowsTicketNotFoundException() {
        UpdateTicketRequest request = new UpdateTicketRequest("Title", "Description", Priority.HIGH, null);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.updateTicket(ticketId, request));
    }

    @Test
    void updateTicket_WithPartialUpdate_UpdatesOnlyProvidedFields() {
        UpdateTicketRequest request = new UpdateTicketRequest("New Title", null, null, null);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.updateTicket(ticketId, request);

        assertNotNull(response);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // Update Status Tests

    @Test
    void updateStatus_WithValidTransition_UpdatesStatus() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse response = ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS);

        assertNotNull(response);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void updateStatus_WithInvalidId_ThrowsTicketNotFoundException() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.updateStatus(ticketId, TicketStatus.IN_PROGRESS));
    }

    @Test
    void updateStatus_WithSameStatus_ThrowsInvalidStatusTransitionException() {
        ticket.setStatus(TicketStatus.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class, () -> ticketService.updateStatus(ticketId, TicketStatus.OPEN));
    }
}
