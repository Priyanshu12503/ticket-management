package com.ticketmanagement.dto;

/**
 * DTO representing a field-level validation error.
 * Used in ErrorResponse details.
 */
public record FieldError(
    String field,
    String error
) {
}
