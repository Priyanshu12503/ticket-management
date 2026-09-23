# REST API Standards

## Base URL and Versioning

```
/api/v1/tickets
```

Future versions: `/api/v2/tickets` (breaking changes only)

## HTTP Methods and Status Codes

| Method | Endpoint | Status | Body | Purpose |
|--------|----------|--------|------|---------|
| POST | `/api/tickets` | 201 | TicketResponse | Create ticket |
| GET | `/api/tickets` | 200 | TicketResponse[] | List all tickets |
| GET | `/api/tickets/{id}` | 200 | TicketResponse | Get ticket details |
| PATCH | `/api/tickets/{id}` | 200 | TicketResponse | Update ticket (title, description, priority, assignee) |
| PATCH | `/api/tickets/{id}/status` | 200 | TicketResponse | Update ticket status |
| POST | `/api/tickets/{id}/comments` | 201 | CommentResponse | Add comment |
| GET | `/api/tickets/{id}/comments` | 200 | CommentResponse[] | List comments |
| GET | `/api/tickets/search?keyword=...` | 200 | TicketResponse[] | Search tickets |
| GET | `/api/tickets?status=OPEN` | 200 | TicketResponse[] | Filter by status |

## Error Responses

All error responses return HTTP 4xx or 5xx with this structure:

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

### Common Error Codes

- **400 Bad Request**: Invalid input, validation failure
- **404 Not Found**: Ticket not found
- **409 Conflict**: Invalid state transition (e.g., CLOSED → OPEN)
- **500 Internal Server Error**: Server error

## Request/Response Models

### CreateTicketRequest
```json
{
  "title": "string (required, 1-200 chars)",
  "description": "string (required, 1-2000 chars)",
  "priority": "HIGH|MEDIUM|LOW (required)",
  "assignee": "string (optional, max 100 chars)"
}
```

### UpdateTicketRequest
```json
{
  "title": "string (optional, 1-200 chars)",
  "description": "string (optional, 1-2000 chars)",
  "priority": "HIGH|MEDIUM|LOW (optional)",
  "assignee": "string (optional, max 100 chars)"
}
```

### UpdateStatusRequest
```json
{
  "status": "OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED (required)"
}
```

### AddCommentRequest
```json
{
  "text": "string (required, 1-2000 chars)"
}
```

### TicketResponse
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
      "createdAt": "ISO 8601 timestamp"
    }
  ],
  "createdAt": "ISO 8601 timestamp",
  "updatedAt": "ISO 8601 timestamp"
}
```

### ListTicketsResponse
```json
{
  "data": [
    { TicketResponse object },
    { TicketResponse object }
  ],
  "total": 42,
  "page": 1,
  "pageSize": 20
}
```

## Query Parameters

### List Tickets
- `status`: Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)
- `page`: Page number (default: 1)
- `pageSize`: Results per page (default: 20, max: 100)
- `sortBy`: Field to sort (title, createdAt, priority; default: createdAt)
- `sortDir`: asc or desc (default: desc)

### Search Tickets
- `keyword`: Search term (searches title and description)
- `page`: Page number
- `pageSize`: Results per page

Example:
```
GET /api/tickets?status=OPEN&page=1&pageSize=10&sortBy=priority&sortDir=desc
GET /api/tickets/search?keyword=database&page=1
```

## Content-Type

All endpoints accept and return `application/json`

```
Content-Type: application/json
Accept: application/json
```

## Timestamps

- All timestamps in **ISO 8601** format with **UTC timezone**
- Format: `2026-09-22T10:30:45Z`
- Stored as `OffsetDateTime` in Java

## Pagination

- Default page size: 20
- Maximum page size: 100
- Pages are 1-indexed
- Response includes total count

## No Authentication

All APIs are public. No authentication or authorization required.

## Rate Limiting (Future)

Not implemented in MVP, but reserve headers for future use:
- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`
