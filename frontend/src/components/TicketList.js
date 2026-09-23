import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ticketService from '../api/ticketService';
import ErrorMessage from './ErrorMessage';
import './TicketList.css';

/**
 * Displays list of tickets with filtering, searching, and pagination
 */
function TicketList() {
  const [tickets, setTickets] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState(null);
  const [isSearching, setIsSearching] = useState(false);

  const statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED'];

  const loadTickets = async (pageNum = 1, status = null) => {
    setLoading(true);
    setError(null);
    try {
      console.log('Fetching tickets from API...');
      const response = await ticketService.listTickets(pageNum, pageSize, 'createdAt', 'desc', status);
      console.log('Response:', response);
      
      // Handle the response structure
      if (response && response.data) {
        const responseData = response.data;
        const ticketList = responseData.data || [];
        const totalCount = responseData.total !== undefined ? responseData.total : 0;
        
        console.log('Tickets loaded:', ticketList.length, 'Total:', totalCount);
        setTickets(ticketList);
        setTotal(totalCount);
      }
      setPage(pageNum);
      setIsSearching(false);
    } catch (err) {
      console.error('Error loading tickets:', err);
      console.error('Full error:', {
        message: err.message,
        response: err.response?.data,
        status: err.response?.status,
        url: err.config?.url
      });
      
      // Extract error message
      let errorMsg = 'An unexpected error occurred';
      if (err.response?.data?.message) {
        errorMsg = err.response.data.message;
      } else if (err.message === 'Network Error') {
        errorMsg = 'Cannot connect to backend API at http://localhost:8080';
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchTerm.trim()) {
      setError('Please enter a search term');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.searchTickets(searchTerm, 1, pageSize);
      setTickets(response.data.data);
      setTotal(response.data.total);
      setPage(1);
      setIsSearching(true);
      setStatusFilter(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterStatus = (status) => {
    setSearchTerm('');
    setStatusFilter(status);
    setPage(1);
    loadTickets(1, status);
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setStatusFilter(null);
    setIsSearching(false);
    loadTickets(1, null);
  };

  const handlePrevPage = () => {
    const newPage = Math.max(1, page - 1);
    if (isSearching) {
      handleSearch({ preventDefault: () => {} });
    } else {
      loadTickets(newPage, statusFilter);
    }
    setPage(newPage);
  };

  const handleNextPage = () => {
    const newPage = page + 1;
    if (isSearching) {
      handleSearch({ preventDefault: () => {} });
    } else {
      loadTickets(newPage, statusFilter);
    }
    setPage(newPage);
  };

  useEffect(() => {
    loadTickets(1, null);
    
    // Check if backend is reachable
    const checkBackend = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/health');
        if (!response.ok) {
          console.warn('Backend health check failed:', response.status);
        } else {
          console.log('Backend is reachable');
        }
      } catch (err) {
        console.error('Backend not reachable:', err.message);
      }
    };
    
    checkBackend();
  }, []);

  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="ticket-list">
      <div className="list-header">
        <h1>Support Tickets</h1>
        <Link to="/create" className="btn btn-primary">
          + New Ticket
        </Link>
      </div>

      {error && <ErrorMessage message={error} />}

      <div className="search-section card">
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            placeholder="Search tickets by keyword..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">
            Search
          </button>
        </form>
      </div>

      <div className="filter-section card">
        <div className="filter-buttons">
          <button
            className={`btn ${statusFilter === null ? 'btn-primary' : 'btn-secondary'}`}
            onClick={handleClearFilters}
          >
            All Tickets ({total})
          </button>
          {statuses.map((status) => (
            <button
              key={status}
              className={`btn ${statusFilter === status ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => handleFilterStatus(status)}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {loading && <div className="spinner"></div>}

      {!loading && tickets.length === 0 ? (
        <div className="card empty-state">
          <p>No tickets found. {isSearching ? 'Try a different search.' : 'Create your first ticket!'}</p>
        </div>
      ) : (
        <>
          <div className="tickets-grid">
            {tickets.map((ticket) => (
              <Link key={ticket.id} to={`/tickets/${ticket.id}`} className="ticket-card card">
                <div className="ticket-header">
                  <h3>{ticket.title}</h3>
                  <span className={`badge badge-${ticket.status.toLowerCase()}`}>
                    {ticket.status}
                  </span>
                </div>
                <p className="ticket-description">{ticket.description.substring(0, 100)}...</p>
                <div className="ticket-footer">
                  <span className={`badge badge-${ticket.priority.toLowerCase()}`}>
                    {ticket.priority}
                  </span>
                  <span className="ticket-meta">
                    {ticket.comments?.length || 0} comments
                  </span>
                </div>
              </Link>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button 
                className="btn btn-secondary" 
                onClick={handlePrevPage} 
                disabled={page === 1}
              >
                ← Previous
              </button>
              <span className="page-info">
                Page {page} of {totalPages}
              </span>
              <button 
                className="btn btn-secondary" 
                onClick={handleNextPage} 
                disabled={page >= totalPages}
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default TicketList;
