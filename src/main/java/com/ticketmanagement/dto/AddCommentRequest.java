package com.ticketmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for adding a comment to a ticket.
 * Used in POST /api/tickets/{id}/comments requests.
 */
public record AddCommentRequest(
    @NotBlank(message = "Comment text must not be blank")
    @Size(min = 1, max = 2000, message = "Comment must be 1-2000 characters")
    String text
) {
}
