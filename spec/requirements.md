# Support Ticket Management System - Requirements

## Overview

A REST API-based support ticket management system built with Java 21, Spring Boot, and PostgreSQL. The system manages the complete lifecycle of support tickets from creation through resolution, with a state machine-enforced status workflow.

**Technology Stack:**
- Backend: Java 21, Spring Boot 3.x, Spring Data JPA
- Database: PostgreSQL (production), H2 (testing)
- Frontend: React/Next.js (separate repository)
- API: REST with JSON payloads

## User Stories

### 1. Ticket Creation
**As a** support agent or customer  
**I want to** create a new support ticket  
**So that** issues can be tracked and resolved

**Acceptance Criteria:**
- User provides title (1-200 characters, required, non-blank)
- User provides description (1-2000 characters, required, non-blank)
- User provides priority (HIGH, MEDIUM, LOW; required)
- User optionally provides assignee (0-100 characters)
- System sets status to OPEN on creation
- System records creation timestamp (UTC)
- System returns 201 Created with ticket ID and all details
- System validates all inputs and returns 400 Bad Request with field-level errors if invalid
- Ticket persists in database and survives application restart

### 2. List Tickets
**As a** support manager  
**I want to** view all tickets  
**So that** I can monitor support activity

**Acceptance Criteria:**
- GET /api/tickets returns all tickets with 200 OK
- Response includes pagination metadata (page, pageSize, total)
- Default page size is 20, max page size is 100
- Results sorted by creation time (newest first) by default
- Supports sortBy parameter (title, createdAt, priority)
- Supports sortDir parameter (asc, desc)
- Returns empty array if no tickets exist
- Each ticket includes all fields: id, title, description, status, priority, assignee, comments, createdAt, updatedAt

### 3. View Ticket Details
**As a** support agent  
**I want to** view full details of a specific ticket  
**So that** I can understand the issue completely

**Acceptance Criteria:**
- GET /api/tickets/{id} returns ticket with 200 OK
- Returns 404 Not Found if ticket ID doesn't exist
- Response includes all ticket fields and all comments
- Comments ordered by creation time (oldest first)

### 4. Update Ticket Fields
**As a** support agent  
**I want to** update ticket title, description, priority, and assignee  
**So that** I can refine ticket information

**Acceptance Criteria:**
- PATCH /api/tickets/{id} updates one or more fields
- All fields optional (can update any subset)
- Validates field constraints (title length, priority enum, etc.)
- Returns 200 OK with updated ticket
- Returns 400 Bad Request with validation errors if invalid
- Returns 404 Not Found if ticket doesn't exist
- Records update timestamp (UTC)
- Does not allow changing status via this endpoint (use /status endpoint)

### 5. Update Ticket Status (State Machine)
**As a** support agent  
**I want to** update a ticket's status following workflow rules  
**So that** tickets progress correctly through the workflow

**Valid Status Transitions (All Must Be Supported):**
- OPEN → IN_PROGRESS (agent starts work)
- IN_PROGRESS → RESOLVED (agent resolves issue)
- RESOLVED → CLOSED (manager confirms resolution)
- OPEN → CANCELLED (cancel before work starts)
- IN_PROGRESS → CANCELLED (cancel during work)

**Invalid Status Transitions (All Must Be Rejected):**
- CLOSED → any status (terminal state)
- RESOLVED → OPEN
- RESOLVED → IN_PROGRESS
- CANCELLED → any status (terminal state)
- Any attempt to transition to invalid state (e.g., IN_PROGRESS → RESOLVED → IN_PROGRESS)

**Acceptance Criteria:**
- PATCH /api/tickets/{id}/status updates status
- Request body contains status enum value
- Valid transitions return 200 OK with updated ticket
- Invalid transitions return 409 Conflict with error message explaining why transition not allowed
- Returns 400 Bad Request if status value is invalid/unknown
- Returns 404 Not Found if ticket doesn't exist
- Validates transition at service layer (business rule enforcement)
- Records update timestamp (UTC)

### 6. Add Comments
**As a** support agent or customer  
**I want to** add comments to a ticket  
**So that** I can provide updates and resolution details

**Acceptance Criteria:**
- POST /api/tickets/{id}/comments adds a comment
- Request body contains text (1-2000 characters, required, non-blank)
- Comment text required and validated
- Returns 201 Created with comment details
- Returns 400 Bad Request if text is blank or exceeds 2000 chars
- Returns 404 Not Found if ticket doesn't exist
- Records comment creation timestamp (UTC)
- Comments persisted in database
- Each comment includes id, text, and createdAt

### 7. List Comments
**As a** support agent  
**I want to** view all comments on a ticket  
**So that** I can see the conversation history

**Acceptance Criteria:**
- GET /api/tickets/{id}/comments returns all comments
- Returns 200 OK
- Returns 404 Not Found if ticket doesn't exist
- Comments ordered by creation time (oldest first)
- Returns empty array if no comments exist

### 8. Search Tickets
**As a** support manager  
**I want to** search tickets by keyword  
**So that** I can find specific issues quickly

**Acceptance Criteria:**
- GET /api/tickets/search?keyword=term searches tickets
- Searches title and description fields (both)
- Search is case-insensitive
- Returns matching tickets with 200 OK
- Supports pagination (page, pageSize parameters)
- Returns empty array if no matches
- Returns 400 Bad Request if keyword is blank

### 9. Filter Tickets by Status
**As a** support manager  
**I want to** filter tickets by status  
**So that** I can focus on tickets in a specific stage

**Acceptance Criteria:**
- GET /api/tickets?status=OPEN filters by status
- Accepts status parameter (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)
- Returns matching tickets with 200 OK
- Returns empty array if no tickets match status
- Returns 400 Bad Request if status value is invalid
- Supports pagination with status filter
- Can combine with other parameters (sortBy, sortDir, etc.)

## Data Model

### Ticket Entity

| Field | Type | Constraints | Notes |
|-------|------|-----------|-------|
| id | UUID | Primary Key, auto-generated | Unique ticket identifier |
| title | String | 1-200 chars, non-blank | Required |
| description | String | 1-2000 chars, non-blank | Required |
| status | Enum | OPEN\|IN_PROGRESS\|RESOLVED\|CLOSED\|CANCELLED | Stored as string, initial value: OPEN |
| priority | Enum | HIGH\|MEDIUM\|LOW | Stored as string, required |
| assignee | String | 0-100 chars, nullable | Optional |
| createdAt | OffsetDateTime | UTC timezone | Immutable, set on creation |
| updatedAt | OffsetDateTime | UTC timezone | Set on creation, updated on changes |

### Comment Entity

| Field | Type | Constraints | Notes |
|-------|------|-----------|-------|
| id | UUID | Primary Key, auto-generated | Unique comment identifier |
| ticketId | UUID | Foreign Key to Ticket | Required |
| text | String | 1-2000 chars, non-blank | Required |
| createdAt | OffsetDateTime | UTC timezone | Immutable, set on creation |

**Relationships:**
- One Ticket has many Comments (1:N)
- Deleting a ticket cascades to delete comments

## Error Handling

### Validation Errors (HTTP 400)

Return structured error response:
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": [
    {"field": "title", "error": "Title must not be blank"},
    {"field": "priority", "error": "Invalid priority value"}
  ],
  "timestamp": "2026-09-22T10:30:00Z"
}
```

### Not Found (HTTP 404)

```json
{
  "status": 404,
  "message": "Ticket not found",
  "details": "Ticket with ID f47ac10b-58cc-4372-a567-0e02b2c3d479 not found",
  "timestamp": "2026-09-22T10:30:00Z"
}
```

### State Transition Conflict (HTTP 409)

```json
{
  "status": 409,
  "message": "Invalid status transition",
  "details": "Cannot transition from CLOSED to OPEN",
  "timestamp": "2026-09-22T10:30:00Z"
}
```

### Server Error (HTTP 500)

Generic server error response without sensitive details.

## API Endpoints Summary

| Method | Endpoint | Status | Purpose |
|--------|----------|--------|---------|
| POST | /api/tickets | 201 | Create ticket |
| GET | /api/tickets | 200 | List all tickets (paginated) |
| GET | /api/tickets/{id} | 200 | Get ticket details |
| PATCH | /api/tickets/{id} | 200 | Update ticket fields |
| PATCH | /api/tickets/{id}/status | 200 | Update status |
| POST | /api/tickets/{id}/comments | 201 | Add comment |
| GET | /api/tickets/{id}/comments | 200 | List comments |
| GET | /api/tickets/search | 200 | Search by keyword |
| GET | /api/tickets?status=... | 200 | Filter by status |

## Non-Functional Requirements

### Database
- PostgreSQL in production
- H2 in-memory for development and testing
- Migrations managed via Flyway (optional)
- Data must persist across application restarts

### Validation
- Backend validation required on all inputs
- No trust of frontend validation
- Field-level validation errors returned to client
- Enum validation for status and priority

### State Machine
- Validation enforced at service layer
- No bypassing state machine rules
- Invalid transitions rejected with 409 Conflict
- Transaction-safe (no partial updates)

### Performance
- List endpoint paginated (default 20, max 100 per page)
- Search supports pagination
- Indexes on frequently queried columns (status, createdAt)
- No N+1 query problems

### Security
- No authentication/authorization required (public API)
- No secrets in code or responses
- Timestamps in UTC
- Input validation prevents injection attacks
- No sensitive data in error messages

### Testing
- State machine transitions fully tested (all valid and invalid)
- API endpoints covered with integration tests
- Business logic covered with unit tests
- Test data survives H2 in-memory database
- Coverage targets: Services 80%, Controllers 70%, Models 60%

## Acceptance Criteria Summary

- ✅ Ticket can be created from UI with title, description, priority, optional assignee
- ✅ Tickets can be listed and viewed with all details
- ✅ Ticket fields (title, description, priority, assignee) can be updated
- ✅ Status transitions follow state machine rules exactly
- ✅ All valid transitions work correctly
- ✅ All invalid transitions rejected with 409 error
- ✅ Comments can be added to tickets
- ✅ Search by keyword works (title + description, case-insensitive)
- ✅ Filter by status works
- ✅ Data persists across application restarts
- ✅ Backend validation on all inputs with meaningful errors
- ✅ UI shows useful error messages
- ✅ State machine integration tests pass
- ✅ No secrets committed to repository
