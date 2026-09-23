package com.ticketmanagement.service;

import com.ticketmanagement.dto.AddCommentRequest;
import com.ticketmanagement.dto.CommentResponse;
import com.ticketmanagement.exception.TicketNotFoundException;
import com.ticketmanagement.exception.ValidationException;
import com.ticketmanagement.mapper.CommentMapper;
import com.ticketmanagement.model.Comment;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing comments on tickets.
 * Handles adding comments to tickets and retrieving comments.
 */
@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.commentMapper = commentMapper;
    }

    /**
     * Add a comment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param request  the add comment request
     * @return the created comment response
     * @throws TicketNotFoundException if ticket does not exist
     * @throws ValidationException    if request validation fails
     */
    @Transactional
    public CommentResponse addComment(UUID ticketId, AddCommentRequest request) {
        logger.debug("Adding comment to ticket: {}", ticketId);

        validateAddCommentRequest(request);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> {
                logger.warn("Ticket not found for comment: {}", ticketId);
                return new TicketNotFoundException(ticketId);
            });

        Comment comment = new Comment(ticket, request.text());
        Comment saved = commentRepository.save(comment);
        logger.info("Comment added to ticket {}: {}", ticketId, saved.getId());

        return commentMapper.toResponse(saved);
    }

    /**
     * Get all comments for a ticket, ordered chronologically (oldest first).
     *
     * @param ticketId the ticket ID
     * @return list of comments for the ticket
     * @throws TicketNotFoundException if ticket does not exist
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicket(UUID ticketId) {
        logger.debug("Fetching comments for ticket: {}", ticketId);

        // Verify ticket exists
        if (!ticketRepository.existsById(ticketId)) {
            logger.warn("Ticket not found: {}", ticketId);
            throw new TicketNotFoundException(ticketId);
        }

        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        logger.debug("Found {} comments for ticket {}", comments.size(), ticketId);

        return comments.stream()
            .map(commentMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Validate AddCommentRequest fields.
     */
    private void validateAddCommentRequest(AddCommentRequest request) {
        if (request.text() == null || request.text().trim().isEmpty()) {
            throw new ValidationException("Comment text must not be blank");
        }

        if (request.text().length() > 2000) {
            throw new ValidationException("Comment text must not exceed 2000 characters");
        }
    }
}
