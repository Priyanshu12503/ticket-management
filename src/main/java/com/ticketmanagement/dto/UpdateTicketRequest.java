package com.ticketmanagement.dto;

import com.ticketmanagement.model.Priority;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a ticket.
 * Used in PATCH /api/tickets/{id} requests.
 * All fields are optional.
 */
public record UpdateTicketRequest(
    @Size(min = 1, max = 200, message = "Title must be 1-200 characters")
    String title,

    @Size(min = 1, max = 2000, message = "Description must be 1-2000 characters")
    String description,

    Priority priority,

    @Size(max = 100, message = "Assignee must be 0-100 characters")
    String assignee
) {
}
