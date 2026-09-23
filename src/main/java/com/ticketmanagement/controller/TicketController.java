package com.ticketmanagement.controller;

import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.ListTicketsResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.dto.UpdateStatusRequest;
import com.ticketmanagement.dto.UpdateTicketRequest;
import com.ticketmanagement.model.TicketStatus;
import com.ticketmanagement.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for ticket operations.
 * Exposes endpoints for CRUD operations, status updates, search, and filtering.
 * All requests and responses are validated and use the DTOs for serialization.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Create a new ticket.
     * POST /api/tickets
     *
     * @param request the ticket creation request (validated)
     * @return 201 Created with the new ticket
     */
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        logger.info("POST /api/tickets - Creating ticket");
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all tickets with pagination, sorting, and optional status filter.
     * GET /api/tickets?page=1&pageSize=20&sortBy=createdAt&sortDir=desc&status=OPEN
     *
     * @param page     page number (1-indexed, default: 1)
     * @param pageSize results per page (default: 20, max: 100)
     * @param sortBy   field to sort by: title, priority, status, createdAt, updatedAt (default: createdAt)
     * @param sortDir  sort direction: asc or desc (default: desc)
     * @param status   optional status filter (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)
     * @return 200 OK with paginated ticket list
     */
    @GetMapping
    public ResponseEntity<ListTicketsResponse> listTickets(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(required = false) TicketStatus status
    ) {
        logger.info("GET /api/tickets - Listing tickets: page={}, pageSize={}, sortBy={}, sortDir={}, status={}",
            page, pageSize, sortBy, sortDir, status);

        ListTicketsResponse response;
        if (status != null) {
            response = ticketService.filterByStatus(status, page, pageSize, sortBy, sortDir);
        } else {
            response = ticketService.listTickets(page, pageSize, sortBy, sortDir);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get a ticket by ID with all its comments.
     * GET /api/tickets/{id}
     *
     * @param id the ticket ID
     * @return 200 OK with the ticket details
     * @throws TicketNotFoundException if ticket does not exist (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable UUID id) {
        logger.info("GET /api/tickets/{} - Fetching ticket", id);
        TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Search tickets by keyword in title and description.
     * GET /api/tickets/search?keyword=database&page=1&pageSize=20
     *
     * @param keyword  search term (required)
     * @param page     page number (1-indexed, default: 1)
     * @param pageSize results per page (default: 20, max: 100)
     * @return 200 OK with paginated search results
     * @throws ValidationException if keyword is empty (400)
     */
    @GetMapping("/search")
    public ResponseEntity<ListTicketsResponse> searchTickets(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        logger.info("GET /api/tickets/search - Searching tickets: keyword={}, page={}, pageSize={}",
            keyword, page, pageSize);
        ListTicketsResponse response = ticketService.searchByKeyword(keyword, page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Update ticket fields (title, description, priority, assignee).
     * PATCH /api/tickets/{id}
     * Only provided fields are updated; null fields are skipped.
     *
     * @param id      the ticket ID
     * @param request the update request with fields to update
     * @return 200 OK with the updated ticket
     * @throws TicketNotFoundException if ticket does not exist (404)
     * @throws ValidationException    if provided fields are invalid (400)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTicketRequest request
    ) {
        logger.info("PATCH /api/tickets/{} - Updating ticket fields", id);
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update ticket status with state machine validation.
     * PATCH /api/tickets/{id}/status
     *
     * @param id      the ticket ID
     * @param request the status update request
     * @return 200 OK with the updated ticket
     * @throws TicketNotFoundException           if ticket does not exist (404)
     * @throws InvalidStatusTransitionException if transition is invalid (409)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        logger.info("PATCH /api/tickets/{}/status - Updating status to {}", id, request.status());
        TicketResponse response = ticketService.updateStatus(id, request.status());
        return ResponseEntity.ok(response);
    }
}
