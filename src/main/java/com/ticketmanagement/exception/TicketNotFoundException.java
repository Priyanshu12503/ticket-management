package com.ticketmanagement.exception;

import java.util.UUID;

/**
 * Exception thrown when a ticket is not found.
 * Maps to HTTP 404 Not Found.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(UUID ticketId) {
        super("Ticket with ID " + ticketId + " not found");
    }

    public TicketNotFoundException(String message) {
        super(message);
    }
}
