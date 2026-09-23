# Support Ticket Management System - Frontend

React frontend for the Support Ticket Management System. Connects to the Spring Boot backend REST API.

## Setup

### Prerequisites
- Node.js 16+
- npm or yarn
- Backend running on http://localhost:8080

### Installation

```bash
cd frontend
npm install
```

### Configuration

Create a `.env` file in the frontend directory:

```
REACT_APP_API_URL=http://localhost:8080/api
```

The default configuration connects to a backend running locally on port 8080.

### Running Locally

Start the development server:

```bash
npm start
```

The app will open at http://localhost:3000

### Build for Production

```bash
npm run build
```

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── api/
│   │   └── ticketService.js      # REST API client
│   ├── components/
│   │   ├── TicketList.js         # List tickets, search, filter
│   │   ├── TicketDetail.js       # View and edit ticket details
│   │   ├── TicketForm.js         # Create new ticket
│   │   ├── CommentSection.js     # View and add comments
│   │   ├── ErrorMessage.js       # Error display component
│   │   └── *.css                 # Component styles
│   ├── hooks/
│   │   └── useTickets.js         # Custom React hooks
│   ├── App.js                     # Main app component
│   ├── App.css                    # App styles
│   ├── index.js                   # Entry point
│   └── index.css                  # Global styles
├── package.json
├── .env.example
└── README.md
```

## Features

### Ticket Management
- ✅ Create new tickets with title, description, priority, and optional assignee
- ✅ List all tickets with pagination (default 20 per page)
- ✅ View full ticket details with all comments
- ✅ Update ticket fields (title, description, priority, assignee)
- ✅ Change ticket status with state machine validation
- ✅ Search tickets by keyword (case-insensitive)
- ✅ Filter tickets by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)

### Comments
- ✅ Add comments to tickets
- ✅ View all comments ordered chronologically (oldest first)
- ✅ Character count on comment input (max 2000)

### Validation & Errors
- ✅ Display field-level validation errors
- ✅ Show API error messages to users
- ✅ Character count indicators on forms
- ✅ Disable invalid status transitions

### UI/UX
- ✅ Clean, simple design
- ✅ Status and priority color coding
- ✅ Responsive layout (desktop and mobile)
- ✅ Loading indicators
- ✅ Empty states
- ✅ Pagination controls

## API Integration

The frontend connects to the REST API using axios. All API calls go through `src/api/ticketService.js`.

### Endpoints Used
- `POST /api/tickets` — Create ticket
- `GET /api/tickets` — List tickets with pagination/filtering
- `GET /api/tickets/{id}` — Get ticket details with comments
- `PATCH /api/tickets/{id}` — Update ticket fields
- `PATCH /api/tickets/{id}/status` — Update ticket status
- `GET /api/tickets/search` — Search tickets
- `POST /api/tickets/{id}/comments` — Add comment
- `GET /api/tickets/{id}/comments` — Get comments

## Running Alongside Backend

### Start Backend
```bash
cd ..  # Go to project root
mvn spring-boot:run
```

Backend runs on http://localhost:8080

### Start Frontend (in separate terminal)
```bash
cd frontend
npm start
```

Frontend runs on http://localhost:3000

Both can run simultaneously. Frontend communicates with backend via REST API on port 8080.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

Part of Support Ticket Management System
