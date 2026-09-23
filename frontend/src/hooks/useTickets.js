import { useState, useCallback } from 'react';
import ticketService from '../api/ticketService';

/**
 * Hook for managing ticket list state and operations
 */
export const useTickets = () => {
  const [tickets, setTickets] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTickets = useCallback(async (pageNum = 1, size = 20, sortBy = 'createdAt', sortDir = 'desc', status = null) => {
    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.listTickets(pageNum, size, sortBy, sortDir, status);
      setTickets(response.data.data);
      setTotal(response.data.total);
      setPage(response.data.page);
      setPageSize(response.data.pageSize);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load tickets');
    } finally {
      setLoading(false);
    }
  }, []);

  const searchTickets = useCallback(async (keyword, pageNum = 1, size = 20) => {
    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.searchTickets(keyword, pageNum, size);
      setTickets(response.data.data);
      setTotal(response.data.total);
      setPage(response.data.page);
      setPageSize(response.data.pageSize);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  }, []);

  const filterByStatus = useCallback(async (status, pageNum = 1, size = 20) => {
    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.listTickets(pageNum, size, 'createdAt', 'desc', status);
      setTickets(response.data.data);
      setTotal(response.data.total);
      setPage(response.data.page);
      setPageSize(response.data.pageSize);
    } catch (err) {
      setError(err.response?.data?.message || 'Filter failed');
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    tickets,
    total,
    page,
    pageSize,
    loading,
    error,
    fetchTickets,
    searchTickets,
    filterByStatus,
    setPage,
  };
};

/**
 * Hook for managing single ticket state and operations
 */
export const useTicket = (ticketId) => {
  const [ticket, setTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTicket = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await ticketService.getTicket(ticketId);
      setTicket(response.data);
      setComments(response.data.comments || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load ticket');
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  const updateTicket = useCallback(async (data) => {
    setError(null);
    try {
      const response = await ticketService.updateTicket(ticketId, data);
      setTicket(response.data);
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to update ticket';
      setError(errorMsg);
      throw err;
    }
  }, [ticketId]);

  const updateStatus = useCallback(async (status) => {
    setError(null);
    try {
      const response = await ticketService.updateStatus(ticketId, status);
      setTicket(response.data);
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to update status';
      setError(errorMsg);
      throw err;
    }
  }, [ticketId]);

  const addComment = useCallback(async (text) => {
    setError(null);
    try {
      const response = await ticketService.addComment(ticketId, text);
      setComments([...comments, response.data]);
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to add comment';
      setError(errorMsg);
      throw err;
    }
  }, [ticketId, comments]);

  return {
    ticket,
    comments,
    loading,
    error,
    fetchTicket,
    updateTicket,
    updateStatus,
    addComment,
  };
};
