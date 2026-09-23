# API Contract

## Base URL

```
/api/tickets
```

All endpoints use JSON payloads and return `application/json`.

## Endpoints

### 1. Create Ticket

```
POST /api/tickets
Status: 201 Created
```

**Request:**
```json
{
  "title": "string (required, 1-200 chars)",
  "description": "string (required, 1-2000 chars)",
  "priority": "HIGH|MEDIUM|LOW (required)",
  "assignee": "string (optional, 0-100 chars)"
}
```

**Response (201):**
```json
{
  "id": "UUID",
  "title": "string",
  "description": "string",
  "status": "OPEN",
  "priority": "HIGH|MEDIUM|LOW",
  "assignee": "string or null",
  "comments": [],
  "createdAt": "2026-09-22T10:30:45Z",
  "updatedAt": "2026-09-22T10:30:45Z"
}
```

**Errors:**
- 400 Bad Request: Validation failed (missing fields, invalid values)

---

### 2. List Tickets

```
GET /api/tickets
Status: 200 OK
```

**Query Parameters:**
- `page` (optional, default 1) - page number
- `pageSize` (optional, default 20, max 100) - results per page
- `sortBy` (optional, default createdAt) - title, createdAt, priority
- `sortDir` (optional, default desc) - asc, desc

**Response (200):**
```json
{
  "data": [
    { TicketResponse },
    { TicketResponse }
  ],
  "total": 42,
  "page": 1,
  "pageSize": 20
}
```

**Errors:**
- 400 Bad Request: Invalid query parameters

---

### 3. Get Ticket Details

```
GET /api/tickets/{id}
Status: 200 OK
```

**Response (200):**
```json
{
  "id": "UUID",
  "title": "string",
  "description": "string",
  "status": "OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED",
  "priority": "HIGH|MEDIUM|LOW",
  "assignee": "string or null",
  "comments": [
    {
      "id": "UUID",
      "text": "string",
      "createdAt": "2026-09-22T10:30:45Z"
    }
  ],
  "createdAt": "2026-09-22T10:30:45Z",
  "updatedAt": "2026-09-22T10:30:45Z"
}
```

**Errors:**
- 404 Not Found: Ticket does not exist

---

### 4. Update Ticket Fields

```
PATCH /api/tickets/{id}
Status: 200 OK
```

**Request (all fields optional):**
```json
{
  "title": "string (1-200 chars)",
  "description": "string (1-2000 chars)",
  "priority": "HIGH|MEDIUM|LOW",
  "assignee": "string (0-100 chars)"
}
```

**Response (200):** Full TicketResponse

**Errors:**
- 400 Bad Request: Validation failed
- 404 Not Found: Ticket does not exist

---

### 5. Update Ticket Status

```
PATCH /api/tickets/{id}/status
Status: 200 OK
```

**Request:**
```json
{
  "status": "OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED (required)"
}
```

**Response (200):** Full TicketResponse with updated status

**Errors:**
- 400 Bad Request: Invalid status value
- 404 Not Found: Ticket does not exist
- 409 Conflict: Invalid status transition

---

### 6. Add Comment

```
POST /api/tickets/{id}/comments
Status: 201 Created
```

**Request:**
```json
{
  "text": "string (required, 1-2000 chars)"
}
```

**Response (201):**
```json
{
  "id": "UUID",
  "text": "string",
  "createdAt": "2026-09-22T10:30:45Z"
}
```

**Errors:**
- 400 Bad Request: Text blank or too long
- 404 Not Found: Ticket does not exist

---

### 7. List Comments

```
GET /api/tickets/{id}/comments
Status: 200 OK
```

**Response (200):**
```json
[
  {
    "id": "UUID",
    "text": "string",
    "createdAt": "2026-09-22T10:30:45Z"
  }
]
```

Ordered by creation time (oldest first). Empty array if no comments.

**Errors:**
- 404 Not Found: Ticket does not exist

---

### 8. Search Tickets

```
GET /api/tickets/search?keyword=term
Status: 200 OK
```

**Query Parameters:**
- `keyword` (required) - search term (1+ chars, non-blank)
- `page` (optional, default 1)
- `pageSize` (optional, default 20, max 100)

Searches title and description fields, case-insensitive.

**Response (200):** ListTicketsResponse with matching tickets

**Errors:**
- 400 Bad Request: Blank keyword or invalid pagination

---

### 9. Filter Tickets by Status

```
GET /api/tickets?status=OPEN
Status: 200 OK
```

**Query Parameters:**
- `status` (required) - OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED
- `page` (optional, default 1)
- `pageSize` (optional, default 20, max 100)
- `sortBy`, `sortDir` (optional, same as list endpoint)

**Response (200):** ListTicketsResponse with filtered tickets

**Errors:**
- 400 Bad Request: Invalid status value or pagination

---

## Error Response Format

All errors return this structure:

```json
{
  "status": 400,
  "message": "Validation failed",
  "details": [
    {
      "field": "title",
      "error": "Title must not be blank"
    },
    {
      "field": "priority",
      "error": "Invalid priority value"
    }
  ],
  "timestamp": "2026-09-22T10:30:00Z"
}
```

### HTTP Status Codes

- **201 Created** - Resource created successfully
- **200 OK** - Request succeeded
- **400 Bad Request** - Validation failed, missing fields, invalid values
- **404 Not Found** - Ticket or resource not found
- **409 Conflict** - Invalid state transition (e.g., CLOSED → OPEN)
- **500 Internal Server Error** - Server error (no sensitive details)

### Common Error Scenarios

| Scenario | Status | Message |
|----------|--------|---------|
| Missing required field | 400 | "Field X must not be blank" |
| Invalid enum value | 400 | "Invalid [field] value" |
| String too long | 400 | "[Field] must be X-Y characters" |
| Ticket not found | 404 | "Ticket not found" |
| Invalid status transition | 409 | "Cannot transition from X to Y" |

---

## Pagination

- Default page size: 20
- Maximum page size: 100
- Pages are 1-indexed
- Response includes: data array, total count, current page, page size

---

## No Authentication

All endpoints are public. No authentication or authorization required.
