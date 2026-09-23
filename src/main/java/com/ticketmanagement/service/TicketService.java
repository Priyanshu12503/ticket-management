package com.ticketmanagement.service;

import com.ticketmanagement.dto.CreateTicketRequest;
import com.ticketmanagement.dto.ListTicketsResponse;
import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.dto.UpdateTicketRequest;
import com.ticketmanagement.exception.InvalidStatusTransitionException;
import com.ticketmanagement.exception.TicketNotFoundException;
import com.ticketmanagement.exception.ValidationException;
import com.ticketmanagement.mapper.TicketMapper;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.model.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing ticket operations.
 * Handles CRUD operations, state machine validation, and business logic for tickets.
 * All state transitions are validated according to the ticket status state machine.
 */
@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    /**
     * Create a new ticket.
     * Initial status is always OPEN.
     *
     * @param request the ticket creation request
     * @return the created ticket response
     * @throws ValidationException if request validation fails
     */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        logger.debug("Creating ticket with title: {}", request.title());

        validateCreateTicketRequest(request);

        Ticket ticket = new Ticket(request.title(), request.description(), request.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignee(request.assignee());

        Ticket saved = ticketRepository.save(ticket);
        logger.info("Ticket created with ID: {}", saved.getId());

        return ticketMapper.toResponse(saved);
    }

    /**
     * Get a ticket by ID with all comments.
     *
     * @param ticketId the ticket ID
     * @return the ticket response with comments
     * @throws TicketNotFoundException if ticket does not exist
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID ticketId) {
        logger.debug("Fetching ticket with ID: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> {
                logger.warn("Ticket not found: {}", ticketId);
                return new TicketNotFoundException(ticketId);
            });

        return ticketMapper.toResponse(ticket);
    }

    /**
     * List tickets with pagination and sorting.
     * Returns tickets without comments to avoid N+1 queries.
     *
     * @param page      page number (1-indexed, converted to 0-indexed for Spring)
     * @param pageSize  number of results per page
     * @param sortBy    field to sort by (title, priority, createdAt, status)
     * @param sortDir   sort direction (asc or desc)
     * @return paginated and sorted ticket list
     * @throws ValidationException if parameters are invalid
     */
    @Transactional(readOnly = true)
    public ListTicketsResponse listTickets(int page, int pageSize, String sortBy, String sortDir) {
        logger.debug("Listing tickets: page={}, pageSize={}, sortBy={}, sortDir={}", page, pageSize, sortBy, sortDir);

        validatePaginationParams(page, pageSize);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = normalizeSortField(sortBy);
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortField));

        Page<Ticket> ticketPage = ticketRepository.findAll(pageable);

        return new ListTicketsResponse(
            ticketPage.getContent().stream()
                .map(ticketMapper::toResponseWithoutComments)
                .collect(Collectors.toList()),
            ticketPage.getTotalElements(),
            page,
            pageSize
        );
    }

    /**
     * Filter tickets by status with pagination.
     *
     * @param status   the ticket status to filter by
     * @param page     page number (1-indexed)
     * @param pageSize number of results per page
     * @param sortBy   field to sort by
     * @param sortDir  sort direction
     * @return paginated filtered ticket list
     */
    @Transactional(readOnly = true)
    public ListTicketsResponse filterByStatus(TicketStatus status, int page, int pageSize, String sortBy, String sortDir) {
        logger.debug("Filtering tickets by status: {}", status);

        validatePaginationParams(page, pageSize);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = normalizeSortField(sortBy);
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortField));

        Page<Ticket> ticketPage = ticketRepository.findByStatus(status, pageable);

        return new ListTicketsResponse(
            ticketPage.getContent().stream()
                .map(ticketMapper::toResponseWithoutComments)
                .collect(Collectors.toList()),
            ticketPage.getTotalElements(),
            page,
            pageSize
        );
    }

    /**
     * Search tickets by keyword in title and description.
     *
     * @param keyword  search term
     * @param page     page number (1-indexed)
     * @param pageSize number of results per page
     * @return paginated search results
     */
    @Transactional(readOnly = true)
    public ListTicketsResponse searchByKeyword(String keyword, int page, int pageSize) {
        logger.debug("Searching tickets by keyword: {}", keyword);

        validatePaginationParams(page, pageSize);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> ticketPage = ticketRepository.searchByKeyword(keyword, pageable);

        return new ListTicketsResponse(
            ticketPage.getContent().stream()
                .map(ticketMapper::toResponseWithoutComments)
                .collect(Collectors.toList()),
            ticketPage.getTotalElements(),
            page,
            pageSize
        );
    }

    /**
     * Update ticket fields (title, description, priority, assignee).
     *
     * @param ticketId the ticket ID
     * @param request  the update request with fields to update
     * @return the updated ticket response
     * @throws TicketNotFoundException if ticket does not exist
     * @throws ValidationException    if request validation fails
     */
    @Transactional
    public TicketResponse updateTicket(UUID ticketId, UpdateTicketRequest request) {
        logger.debug("Updating ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> {
                logger.warn("Ticket not found for update: {}", ticketId);
                return new TicketNotFoundException(ticketId);
            });

        // Update fields if provided
        if (request.title() != null) {
            validateTitle(request.title());
            ticket.setTitle(request.title());
        }

        if (request.description() != null) {
            validateDescription(request.description());
            ticket.setDescription(request.description());
        }

        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }

        if (request.assignee() != null) {
            validateAssignee(request.assignee());
            ticket.setAssignee(request.assignee());
        }

        Ticket updated = ticketRepository.save(ticket);
        logger.info("Ticket updated: {}", ticketId);

        return ticketMapper.toResponse(updated);
    }

    /**
     * Update ticket status with state machine validation.
     * Enforces the valid status transition paths:
     * - OPEN → IN_PROGRESS, CANCELLED
     * - IN_PROGRESS → RESOLVED, CANCELLED
     * - RESOLVED → CLOSED
     * - All other transitions are rejected
     *
     * @param ticketId  the ticket ID
     * @param newStatus the new status
     * @return the updated ticket response
     * @throws TicketNotFoundException       if ticket does not exist
     * @throws InvalidStatusTransitionException if transition is not allowed
     */
    @Transactional
    public TicketResponse updateStatus(UUID ticketId, TicketStatus newStatus) {
        logger.debug("Updating ticket status: {} -> {}", ticketId, newStatus);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> {
                logger.warn("Ticket not found for status update: {}", ticketId);
                return new TicketNotFoundException(ticketId);
            });

        TicketStatus currentStatus = ticket.getStatus();

        // Validate transition
        if (!isValidTransition(currentStatus, newStatus)) {
            logger.warn("Invalid status transition: {} -> {}", currentStatus, newStatus);
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }

        ticket.setStatus(newStatus);
        Ticket updated = ticketRepository.save(ticket);
        logger.info("Ticket status updated: {} - {} -> {}", ticketId, currentStatus, newStatus);

        return ticketMapper.toResponse(updated);
    }

    /**
     * Validate if a status transition is allowed according to the state machine.
     * Valid transitions:
     * - OPEN → IN_PROGRESS, CANCELLED
     * - IN_PROGRESS → RESOLVED, CANCELLED
     * - RESOLVED → CLOSED
     *
     * @param currentStatus the current ticket status
     * @param newStatus     the desired new status
     * @return true if transition is valid, false otherwise
     */
    private boolean isValidTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        // Cannot transition from same status
        if (currentStatus == newStatus) {
            return false;
        }

        return switch (currentStatus) {
            case OPEN -> newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CANCELLED;
            case RESOLVED -> newStatus == TicketStatus.CLOSED;
            case CLOSED, CANCELLED -> false; // Terminal states - no transitions allowed
        };
    }

    /**
     * Validate CreateTicketRequest fields.
     */
    private void validateCreateTicketRequest(CreateTicketRequest request) {
        validateTitle(request.title());
        validateDescription(request.description());

        if (request.priority() == null) {
            throw new ValidationException("Priority is required");
        }

        if (request.assignee() != null && !request.assignee().isBlank()) {
            validateAssignee(request.assignee());
        }
    }

    /**
     * Validate ticket title.
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title must not be blank");
        }

        if (title.length() > 200) {
            throw new ValidationException("Title must not exceed 200 characters");
        }
    }

    /**
     * Validate ticket description.
     */
    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Description must not be blank");
        }

        if (description.length() > 2000) {
            throw new ValidationException("Description must not exceed 2000 characters");
        }
    }

    /**
     * Validate assignee field.
     */
    private void validateAssignee(String assignee) {
        if (assignee.length() > 100) {
            throw new ValidationException("Assignee must not exceed 100 characters");
        }
    }

    /**
     * Validate pagination parameters.
     */
    private void validatePaginationParams(int page, int pageSize) {
        if (page < 1) {
            throw new ValidationException("Page must be >= 1");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new ValidationException("Page size must be between 1 and 100");
        }
    }

    /**
     * Normalize sort field name to database column name.
     */
    private String normalizeSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        return switch (sortBy.toLowerCase()) {
            case "title" -> "title";
            case "priority" -> "priority";
            case "status" -> "status";
            case "created", "createdat" -> "createdAt";
            case "updated", "updatedat" -> "updatedAt";
            default -> "createdAt";
        };
    }
}
