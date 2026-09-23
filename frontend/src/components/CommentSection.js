import React, { useState } from 'react';
import ErrorMessage from './ErrorMessage';
import './CommentSection.css';

/**
 * Display comments and add new comments to a ticket
 */
function CommentSection({ ticketId, comments = [], onCommentAdded }) {
  const [newComment, setNewComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) {
      setError('Comment cannot be empty');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      const success = await onCommentAdded(newComment);
      if (success) {
        setNewComment('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add comment');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="comment-section card">
      <h2>Comments ({comments.length})</h2>

      {error && <ErrorMessage message={error} />}

      <form onSubmit={handleSubmit} className="comment-form">
        <textarea
          placeholder="Add a comment..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          maxLength="2000"
          disabled={isSubmitting}
        />
        <div className="comment-actions">
          <span className="char-count">
            {newComment.length}/2000
          </span>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting || !newComment.trim()}
          >
            {isSubmitting ? 'Posting...' : 'Post Comment'}
          </button>
        </div>
      </form>

      <div className="comments-list">
        {comments.length === 0 ? (
          <p className="no-comments">No comments yet. Be the first to comment!</p>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="comment">
              <div className="comment-meta">
                <span className="comment-time">
                  {new Date(comment.createdAt).toLocaleString()}
                </span>
              </div>
              <p className="comment-text">{comment.text}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default CommentSection;
