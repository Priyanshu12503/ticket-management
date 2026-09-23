import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TicketList from './components/TicketList';
import TicketDetail from './components/TicketDetail';
import TicketForm from './components/TicketForm';
import './App.css';

/**
 * Main App component with routing
 */
function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <header className="app-header">
          <div className="container">
            <h1 className="app-title">Support Tickets</h1>
          </div>
        </header>

        <main className="app-main">
          <div className="container">
            <Routes>
              <Route path="/" element={<TicketList />} />
              <Route path="/create" element={<TicketForm />} />
              <Route path="/tickets/:id" element={<TicketDetail />} />
            </Routes>
          </div>
        </main>

        <footer className="app-footer">
          <div className="container">
            <p>&copy; 2026 Support Ticket Management System</p>
          </div>
        </footer>
      </div>
    </BrowserRouter>
  );
}

export default App;
