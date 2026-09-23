package com.ticketmanagement.dto;

import java.util.List;

/**
 * DTO for paginated ticket list responses.
 * Used in GET /api/tickets and GET /api/tickets/search responses.
 * Includes pagination metadata.
 */
public record ListTicketsResponse(
    List<TicketResponse> data,
    long total,
    int page,
    int pageSize
) {
}
