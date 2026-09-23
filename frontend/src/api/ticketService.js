import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Ticket API service
 */
export const ticketService = {
  /**
   * Create a new ticket
   */
  createTicket: (data) => api.post('/tickets', data),

  /**
   * Get all tickets with pagination, sorting, and filtering
   */
  listTickets: (page = 1, pageSize = 20, sortBy = 'createdAt', sortDir = 'desc', status = null) => {
    const params = { page, pageSize, sortBy, sortDir };
    if (status) params.status = status;
    return api.get('/tickets', { params });
  },

  /**
   * Get a single ticket by ID
   */
  getTicket: (id) => api.get(`/tickets/${id}`),

  /**
   * Update ticket fields (partial update)
   */
  updateTicket: (id, data) => api.patch(`/tickets/${id}`, data),

  /**
   * Update ticket status
   */
  updateStatus: (id, status) => api.patch(`/tickets/${id}/status`, { status }),

  /**
   * Search tickets by keyword
   */
  searchTickets: (keyword, page = 1, pageSize = 20) =>
    api.get('/tickets/search', { params: { keyword, page, pageSize } }),

  /**
   * Add a comment to a ticket
   */
  addComment: (ticketId, text) => api.post(`/tickets/${ticketId}/comments`, { text }),

  /**
   * Get all comments for a ticket
   */
  getComments: (ticketId) => api.get(`/tickets/${ticketId}/comments`),
};

export default ticketService;
