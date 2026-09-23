package com.ticketmanagement.mapper;

import com.ticketmanagement.dto.CommentResponse;
import com.ticketmanagement.model.Comment;
import org.springframework.stereotype.Component;

/**
 * Mapper for Comment entity to/from DTO.
 * Handles conversion between JPA Comment entity and CommentResponse DTO.
 * Simple mapping logic only; business rules belong in the service layer.
 */
@Component
public class CommentMapper {

    /**
     * Convert Comment entity to CommentResponse DTO.
     *
     * @param comment the comment entity to convert
     * @return the comment response DTO with all fields populated
     */
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponse(
            comment.getId().toString(),
            comment.getText(),
            comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null
        );
    }
}
