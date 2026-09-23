package com.ticketmanagement.repository;

import com.ticketmanagement.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment entity.
 * Provides data access methods for comment operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Find all comments for a specific ticket, ordered by creation time (oldest first).
     *
     * @param ticketId the ID of the ticket
     * @return list of comments ordered by creation time ascending
     */
    List<Comment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

}
