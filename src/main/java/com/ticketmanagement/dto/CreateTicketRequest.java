package com.ticketmanagement.dto;

import com.ticketmanagement.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new ticket.
 * Used in POST /api/tickets requests.
 */
public record CreateTicketRequest(
    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 200, message = "Title must be 1-200 characters")
    String title,

    @NotBlank(message = "Description must not be blank")
    @Size(min = 1, max = 2000, message = "Description must be 1-2000 characters")
    String description,

    @NotNull(message = "Priority is required")
    Priority priority,

    @Size(max = 100, message = "Assignee must be 0-100 characters")
    String assignee
) {
}
