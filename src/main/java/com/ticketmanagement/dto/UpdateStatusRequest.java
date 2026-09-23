package com.ticketmanagement.dto;

import com.ticketmanagement.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating a ticket's status.
 * Used in PATCH /api/tickets/{id}/status requests.
 */
public record UpdateStatusRequest(
    @NotNull(message = "Status is required")
    TicketStatus status
) {
}
