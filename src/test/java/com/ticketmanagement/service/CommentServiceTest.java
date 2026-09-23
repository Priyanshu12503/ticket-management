package com.ticketmanagement.service;

import com.ticketmanagement.dto.AddCommentRequest;
import com.ticketmanagement.dto.CommentResponse;
import com.ticketmanagement.exception.TicketNotFoundException;
import com.ticketmanagement.exception.ValidationException;
import com.ticketmanagement.mapper.CommentMapper;
import com.ticketmanagement.model.Comment;
import com.ticketmanagement.model.Priority;
import com.ticketmanagement.model.Ticket;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private UUID ticketId;
    private UUID commentId;
    private Ticket ticket;
    private Comment comment;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        ticket = new Ticket("Test", "Description", Priority.HIGH);
        ticket.setId(ticketId);

        comment = new Comment(ticket, "This is a comment");
        comment.setId(commentId);

        commentResponse = new CommentResponse(
            commentId.toString(),
            "This is a comment",
            "2026-09-22T10:00:00Z"
        );
    }

    // Add Comment Tests

    @Test
    void addComment_WithValidData_ReturnsComment() {
        AddCommentRequest request = new AddCommentRequest("This is a comment");
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        CommentResponse response = commentService.addComment(ticketId, request);

        assertNotNull(response);
        assertEquals(commentId.toString(), response.id());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_WithInvalidTicketId_ThrowsTicketNotFoundException() {
        AddCommentRequest request = new AddCommentRequest("This is a comment");
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> commentService.addComment(ticketId, request));
    }

    @Test
    void addComment_WithBlankText_ThrowsValidationException() {
        AddCommentRequest request = new AddCommentRequest("");

        assertThrows(ValidationException.class, () -> commentService.addComment(ticketId, request));
    }

    @Test
    void addComment_WithNullText_ThrowsValidationException() {
        AddCommentRequest request = new AddCommentRequest(null);

        assertThrows(ValidationException.class, () -> commentService.addComment(ticketId, request));
    }

    @Test
    void addComment_WithTextOver2000Chars_ThrowsValidationException() {
        String longText = "a".repeat(2001);
        AddCommentRequest request = new AddCommentRequest(longText);

        assertThrows(ValidationException.class, () -> commentService.addComment(ticketId, request));
    }

    // Get Comments Tests

    @Test
    void getCommentsByTicket_WithValidTicket_ReturnsComments() {
        when(ticketRepository.existsById(ticketId)).thenReturn(true);
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
            .thenReturn(List.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        List<CommentResponse> responses = commentService.getCommentsByTicket(ticketId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(commentRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Test
    void getCommentsByTicket_WithInvalidTicketId_ThrowsTicketNotFoundException() {
        when(ticketRepository.existsById(ticketId)).thenReturn(false);

        assertThrows(TicketNotFoundException.class, () -> commentService.getCommentsByTicket(ticketId));
    }

    @Test
    void getCommentsByTicket_WithNoComments_ReturnsEmptyList() {
        when(ticketRepository.existsById(ticketId)).thenReturn(true);
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId))
            .thenReturn(List.of());

        List<CommentResponse> responses = commentService.getCommentsByTicket(ticketId);

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }
}
