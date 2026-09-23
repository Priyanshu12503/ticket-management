import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ticketService from '../api/ticketService';
import ErrorMessage from './ErrorMessage';
import './TicketForm.css';

/**
 * Form for creating new tickets
 */
function TicketForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    assignee: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [details, setDetails] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    setDetails(null);

    try {
      const response = await ticketService.createTicket(formData);
      navigate(`/tickets/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create ticket');
      if (err.response?.data?.details) {
        setDetails(err.response.data.details);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="ticket-form-page">
      <button className="btn btn-secondary mb-6" onClick={() => navigate(-1)}>
        ← Back
      </button>

      <div className="form-container card">
        <h1>Create New Ticket</h1>

        {error && <ErrorMessage message={error} details={details} />}

        <form onSubmit={handleSubmit} className="ticket-form">
          <div className="form-group">
            <label htmlFor="title">Title *</label>
            <input
              id="title"
              name="title"
              type="text"
              value={formData.title}
              onChange={handleChange}
              placeholder="Brief summary of the issue"
              maxLength="200"
              required
            />
            <span className="char-count">{formData.title.length}/200</span>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Detailed explanation of the issue"
              maxLength="2000"
              required
            />
            <span className="char-count">{formData.description.length}/2000</span>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="priority">Priority *</label>
              <select
                id="priority"
                name="priority"
                value={formData.priority}
                onChange={handleChange}
              >
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="assignee">Assignee (optional)</label>
              <input
                id="assignee"
                name="assignee"
                type="text"
                value={formData.assignee}
                onChange={handleChange}
                placeholder="Email or name"
                maxLength="100"
              />
              <span className="char-count">{formData.assignee.length}/100</span>
            </div>
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Creating...' : 'Create Ticket'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default TicketForm;
