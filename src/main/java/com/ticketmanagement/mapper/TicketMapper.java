package com.ticketmanagement.mapper;

import com.ticketmanagement.dto.TicketResponse;
import com.ticketmanagement.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for Ticket entity to/from DTO.
 * Handles conversion between JPA Ticket entity and TicketResponse DTO.
 * Simple mapping logic only; business rules belong in the service layer.
 */
@Component
public class TicketMapper {

    private final CommentMapper commentMapper;

    public TicketMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    /**
     * Convert Ticket entity to TicketResponse DTO.
     * Includes nested comments as CommentResponse objects.
     *
     * @param ticket the ticket entity to convert
     * @return the ticket response DTO with all fields populated
     */
    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketResponse(
            ticket.getId().toString(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getAssignee(),
            ticket.getComments() != null
                ? ticket.getComments().stream()
                    .map(commentMapper::toResponse)
                    .collect(Collectors.toList())
                : null,
            ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : null,
            ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : null
        );
    }

    /**
     * Convert Ticket entity to TicketResponse DTO without loading comments.
     * Useful for list operations to avoid N+1 query problems.
     *
     * @param ticket the ticket entity to convert
     * @return the ticket response DTO with comments set to null
     */
    public TicketResponse toResponseWithoutComments(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketResponse(
            ticket.getId().toString(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getAssignee(),
            null, // Comments explicitly excluded for list operations
            ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : null,
            ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : null
        );
    }
}
