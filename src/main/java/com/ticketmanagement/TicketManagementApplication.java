package com.ticketmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Support Ticket Management System application.
 *
 * Enables Spring Boot auto-configuration and component scanning for the
 * com.ticketmanagement package and all sub-packages.
 */
@SpringBootApplication
public class TicketManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketManagementApplication.class, args);
    }

}
