package com.ticketmanagement.dto;

/**
 * DTO for comment responses.
 * Used in GET /api/tickets/{id}/comments and POST /api/tickets/{id}/comments responses.
 */
public record CommentResponse(
    String id,
    String text,
    String createdAt
) {
}
