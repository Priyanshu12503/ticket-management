import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ticketService from '../api/ticketService';
import ErrorMessage from './ErrorMessage';
import CommentSection from './CommentSection';
import './TicketDetail.css';

/**
 * Display full ticket details with status, comments, and edit capabilities
 */
function TicketDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({});
  const [isSaving, setIsSaving] = useState(false);

  const statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED'];

  useEffect(() => {
    loadTicket();
  }, [id]);

  const loadTicket = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.getTicket(id);
      setTicket(response.data);
      setEditData({
        title: response.data.title,
        description: response.data.description,
        priority: response.data.priority,
        assignee: response.data.assignee || '',
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load ticket');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateFields = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    try {
      const response = await ticketService.updateTicket(id, editData);
      setTicket(response.data);
      setIsEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update ticket');
      if (err.response?.data?.details) {
        setError(err.response.data.message);
      }
    } finally {
      setIsSaving(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    setError(null);
    try {
      const response = await ticketService.updateStatus(id, newStatus);
      setTicket(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update status');
    }
  };

  const handleAddComment = async (text) => {
    try {
      const response = await ticketService.addComment(id, text);
      setTicket({
        ...ticket,
        comments: [...(ticket.comments || []), response.data],
      });
      return true;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add comment');
      return false;
    }
  };

  if (loading) return <div className="spinner"></div>;

  if (!ticket) {
    return (
      <div className="ticket-detail">
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          ← Back
        </button>
        <ErrorMessage message={error || 'Ticket not found'} />
      </div>
    );
  }

  return (
    <div className="ticket-detail">
      <button className="btn btn-secondary mb-6" onClick={() => navigate(-1)}>
        ← Back to Tickets
      </button>

      {error && <ErrorMessage message={error} />}

      <div className="ticket-header-section card">
        <div className="header-top">
          <div>
            <h1>{ticket.title}</h1>
            <p className="ticket-id">Ticket #{ticket.id.slice(0, 8)}</p>
          </div>
          <div className="badges">
            <span className={`badge badge-${ticket.status.toLowerCase()}`}>
              {ticket.status}
            </span>
            <span className={`badge badge-${ticket.priority.toLowerCase()}`}>
              {ticket.priority}
            </span>
          </div>
        </div>

        <div className="status-selector mt-6">
          <label>Change Status:</label>
          <div className="status-buttons">
            {statuses.map((status) => (
              <button
                key={status}
                className={`btn ${ticket.status === status ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => handleStatusChange(status)}
                disabled={ticket.status === status}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="ticket-content">
        <div className="main-content">
          <div className="card">
            <div className="section-header">
              <h2>Details</h2>
              {!isEditing && (
                <button
                  className="btn btn-secondary"
                  onClick={() => setIsEditing(true)}
                >
                  Edit
                </button>
              )}
            </div>

            {isEditing ? (
              <form onSubmit={handleUpdateFields} className="edit-form">
                <div className="form-group">
                  <label htmlFor="title">Title</label>
                  <input
                    id="title"
                    type="text"
                    value={editData.title}
                    onChange={(e) => setEditData({ ...editData, title: e.target.value })}
                    maxLength="200"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="description">Description</label>
                  <textarea
                    id="description"
                    value={editData.description}
                    onChange={(e) => setEditData({ ...editData, description: e.target.value })}
                    maxLength="2000"
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="priority">Priority</label>
                    <select
                      id="priority"
                      value={editData.priority}
                      onChange={(e) => setEditData({ ...editData, priority: e.target.value })}
                    >
                      <option value="LOW">LOW</option>
                      <option value="MEDIUM">MEDIUM</option>
                      <option value="HIGH">HIGH</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="assignee">Assignee</label>
                    <input
                      id="assignee"
                      type="text"
                      placeholder="Optional"
                      value={editData.assignee}
                      onChange={(e) => setEditData({ ...editData, assignee: e.target.value })}
                      maxLength="100"
                    />
                  </div>
                </div>

                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={isSaving}>
                    {isSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      setIsEditing(false);
                      loadTicket();
                    }}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <div className="details-view">
                <div className="detail-group">
                  <h3>Description</h3>
                  <p>{ticket.description}</p>
                </div>

                <div className="detail-group">
                  <h3>Priority</h3>
                  <p>{ticket.priority}</p>
                </div>

                <div className="detail-group">
                  <h3>Assignee</h3>
                  <p>{ticket.assignee || 'Not assigned'}</p>
                </div>

                <div className="detail-group">
                  <h3>Created</h3>
                  <p>{new Date(ticket.createdAt).toLocaleString()}</p>
                </div>

                <div className="detail-group">
                  <h3>Updated</h3>
                  <p>{new Date(ticket.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            )}
          </div>
        </div>

        <aside className="sidebar">
          <CommentSection
            ticketId={id}
            comments={ticket.comments || []}
            onCommentAdded={handleAddComment}
          />
        </aside>
      </div>
    </div>
  );
}

export default TicketDetail;
