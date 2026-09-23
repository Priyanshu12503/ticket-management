package com.ticketmanagement.repository;

import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Ticket entity.
 * Provides data access methods for ticket operations including filtering, searching, and pagination.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Find all tickets with a specific status, with pagination support.
     *
     * @param status the ticket status to filter by
     * @param pageable pagination and sorting information
     * @return page of tickets matching the status
     */
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    /**
     * Search tickets by keyword in title and description fields.
     * Search is case-insensitive.
     *
     * @param keyword the search term
     * @param pageable pagination and sorting information
     * @return page of tickets matching the keyword
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Ticket> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
