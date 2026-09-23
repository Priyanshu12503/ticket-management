package com.ticketmanagement.dto;

import java.util.List;

/**
 * DTO for error responses.
 * Used for all error responses (4xx, 5xx).
 * Includes structured error information with optional field-level details.
 */
public record ErrorResponse(
    int status,
    String message,
    List<FieldError> details,
    String timestamp
) {
}
