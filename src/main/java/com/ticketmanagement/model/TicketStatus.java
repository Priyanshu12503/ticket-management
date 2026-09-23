package com.ticketmanagement.model;

/**
 * Enumeration of valid ticket statuses.
 * Defines the state machine for ticket lifecycle.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
