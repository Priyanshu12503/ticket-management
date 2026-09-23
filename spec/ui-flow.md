# UI Flow

## User Workflows

### 1. Create a Ticket

**Flow:**
1. User navigates to "Create Ticket" page
2. Form displays fields: Title, Description, Priority, Assignee (optional)
3. User fills in required fields
4. User clicks "Create" button
5. Frontend validates locally (optional, for UX)
6. Frontend calls POST /api/tickets
7. On success (201): Display ticket details, show ticket ID
8. On error (400): Show field-level validation errors above each field
9. On error (500): Show generic "Something went wrong" message

**Error Display Examples:**
- "Title must not be blank"
- "Description must be 1-2000 characters"
- "Invalid priority value"

---

### 2. View All Tickets

**Flow:**
1. User navigates to "Tickets" list view
2. Frontend calls GET /api/tickets (page=1, pageSize=20, sortBy=createdAt, sortDir=desc)
3. Display table with columns: ID, Title, Status, Priority, Assignee, Created Date
4. Show pagination controls (previous, next, page indicator)
5. Allow user to change pageSize dropdown (20, 50, 100)
6. Allow user to sort by clicking column headers
7. Allow user to click row to view details

**Error Handling:**
- 404 Not Found: Show "No tickets found"
- 500 Server Error: Show "Failed to load tickets. Try again."

---

### 3. Search Tickets

**Flow:**
1. User enters keyword in search box
2. User presses Enter or clicks "Search" button
3. Frontend calls GET /api/tickets/search?keyword=term&page=1
4. Display results in table format
5. Support pagination on results
6. Show "X results found" message
7. Empty search returns 400 or shows "Enter a search term"

**Error Handling:**
- 400 Bad Request: "Search term required"
- 500 Server Error: "Search failed"

---

### 4. Filter Tickets by Status

**Flow:**
1. User clicks status filter dropdown or buttons
2. Options: All, OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
3. Frontend calls GET /api/tickets?status=OPEN&page=1
4. Display filtered results in table
5. Show badge or highlight indicating active filter
6. Support pagination on filtered results

**Error Handling:**
- 400 Bad Request: Show "Invalid filter selection"
- 500 Server Error: Show "Failed to load filtered tickets"

---

### 5. View Ticket Details

**Flow:**
1. User clicks on ticket from list or has direct link to /tickets/{id}
2. Frontend calls GET /api/tickets/{id}
3. Display ticket information:
   - Title (large heading)
   - Description
   - Status badge (with status color)
   - Priority badge (color-coded: red=HIGH, yellow=MEDIUM, green=LOW)
   - Assignee (if set)
   - Created/Updated timestamps
   - Comments section (see below)
4. Show edit/update button (pencil icon)
5. Show status update dropdown (if current user authorized; for MVP, always show)

**Comments Display:**
1. Show all comments in chronological order (oldest first)
2. Each comment shows: text, created timestamp
3. "Add comment" form at bottom with textarea and "Post" button

**Error Handling:**
- 404 Not Found: Show "Ticket not found"
- 500 Server Error: Show "Failed to load ticket details"

---

### 6. Update Ticket Fields

**Flow:**
1. User clicks "Edit" button on ticket details page
2. Show modal or inline form with fields: Title, Description, Priority, Assignee
3. Pre-fill with current values
4. User modifies one or more fields
5. User clicks "Save" button
6. Frontend validates locally (optional)
7. Frontend calls PATCH /api/tickets/{id}
8. On success (200): Update display, show toast "Ticket updated"
9. On error (400): Show validation errors
10. On error (404): Show "Ticket not found"

**Error Display:**
- Field-level validation errors shown above each field
- Highlight affected fields in red
- Show "Save failed" toast on 500 error

---

### 7. Update Ticket Status

**Flow:**
1. User sees status dropdown on ticket details (e.g., "Current Status: OPEN")
2. User clicks dropdown to see available transitions
3. Dropdown shows only valid next statuses (based on state machine)
4. User selects new status
5. Frontend calls PATCH /api/tickets/{id}/status
6. On success (200):
   - Update status display
   - Show confirmation toast "Status changed from OPEN to IN_PROGRESS"
   - Refresh comments/details
7. On error (409): Show "Invalid status transition. Current: OPEN. Cannot change to RESOLVED."
8. On error (400): Show "Invalid status value"
9. On error (404): Show "Ticket not found"

**Valid Status Transitions in UI:**
- From OPEN: Show buttons/options for IN_PROGRESS, CANCELLED
- From IN_PROGRESS: Show buttons/options for RESOLVED, CANCELLED
- From RESOLVED: Show buttons/options for CLOSED
- From CLOSED: Show disabled/no options
- From CANCELLED: Show disabled/no options

**Error Display:**
- Alert/toast with clear message
- Disallow transition if already terminal state
- Show helpful message if transition invalid

---

### 8. Add Comment

**Flow:**
1. User scrolls to comments section on ticket details
2. User types comment in textarea ("Add a comment...")
3. User clicks "Post" or presses Ctrl+Enter
4. Frontend validates comment not blank
5. Frontend calls POST /api/tickets/{id}/comments
6. On success (201):
   - Clear textarea
   - Add new comment to list (at bottom, newest first if we sort that way)
   - Show "Comment added" toast
7. On error (400): Show "Comment too long (max 2000 characters)"
8. On error (404): Show "Ticket not found"

**Error Display:**
- Field-level error above textarea
- Red border on textarea if error

---

## Visual Design Notes

### Status Badge Colors
- OPEN: Blue
- IN_PROGRESS: Orange
- RESOLVED: Green
- CLOSED: Gray
- CANCELLED: Red

### Priority Badge Colors
- HIGH: Red
- MEDIUM: Yellow/Orange
- LOW: Green

### Error States
- Validation errors: Red text below field, red border
- 404 errors: "Not found" page or message
- 500 errors: Generic "Something went wrong" message
- Toast notifications for success/error feedback

### Loading States
- Spinner while loading tickets/ticket details
- Disable buttons while API call in progress
- Show skeleton loaders on list view (optional, for UX)

---

## Constraints & Assumptions

- No user authentication for MVP (all users can view/edit any ticket)
- No real-time updates (refresh to see changes from other users)
- No drag-and-drop status transitions (use dropdown only)
- No bulk operations (create/update one at a time)
- No ticket templates or cloning
- Comments are text only (no markdown, no attachments)
