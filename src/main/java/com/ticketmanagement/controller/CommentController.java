package com.ticketmanagement.controller;

import com.ticketmanagement.dto.AddCommentRequest;
import com.ticketmanagement.dto.CommentResponse;
import com.ticketmanagement.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for comment operations.
 * Exposes endpoints for adding comments and retrieving comments for tickets.
 * All requests and responses are validated and use the DTOs for serialization.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a comment to a ticket.
     * POST /api/tickets/{ticketId}/comments
     *
     * @param ticketId the ticket ID
     * @param request  the add comment request (validated)
     * @return 201 Created with the new comment
     * @throws TicketNotFoundException if ticket does not exist (404)
     * @throws ValidationException    if comment text is invalid (400)
     */
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
        @PathVariable UUID ticketId,
        @Valid @RequestBody AddCommentRequest request
    ) {
        logger.info("POST /api/tickets/{}/comments - Adding comment", ticketId);
        CommentResponse response = commentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all comments for a ticket.
     * GET /api/tickets/{ticketId}/comments
     * Comments are ordered chronologically (oldest first).
     *
     * @param ticketId the ticket ID
     * @return 200 OK with list of comments
     * @throws TicketNotFoundException if ticket does not exist (404)
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID ticketId) {
        logger.info("GET /api/tickets/{}/comments - Fetching comments", ticketId);
        List<CommentResponse> responses = commentService.getCommentsByTicket(ticketId);
        return ResponseEntity.ok(responses);
    }
}
