package com.ticketmanagement.dto;

import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.TicketStatus;

import java.util.List;

/**
 * DTO for ticket responses.
 * Used in GET /api/tickets, GET /api/tickets/{id}, POST /api/tickets, and PATCH /api/tickets/{id} responses.
 * Includes nested comments.
 */
public record TicketResponse(
    String id,
    String title,
    String description,
    TicketStatus status,
    Priority priority,
    String assignee,
    List<CommentResponse> comments,
    String createdAt,
    String updatedAt
) {
}
