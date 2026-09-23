package com.ticketmanagement.exception;

import com.ticketmanagement.model.TicketStatus;

/**
 * Exception thrown when an invalid ticket status transition is attempted.
 * Maps to HTTP 409 Conflict.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TicketStatus currentStatus, TicketStatus requestedStatus) {
        super("Cannot transition from " + currentStatus + " to " + requestedStatus);
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
