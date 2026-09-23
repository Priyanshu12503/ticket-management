# Test Strategy

## Testing Pyramid

```
       ▲
      ╱ ╲
     ╱   ╲  E2E Tests (React component integration)
    ╱─────╲ Optional for MVP
   ╱       ╲
  ╱─────────╲
 ╱           ╲  Integration Tests
╱   Service   ╲ API endpoints, database
╱─────────────╲ State machine transitions
│   Unit Tests │
│  Services,   │
│  Business    │
│   Logic      │
└─────────────┘
```

## Unit Tests (Services)

**Framework:** JUnit 5 + Mockito  
**Location:** `src/test/java/com/ticketmanagement/unit/service/`

### TicketService Tests

**createTicket():**
- ✅ Valid input returns ticket with OPEN status
- ✅ Title validation (blank, too long)
- ✅ Description validation (blank, too long)
- ✅ Priority required
- ✅ Assignee optional and validated
- ✅ Created/updated timestamps set to now (UTC)
- ✅ ID auto-generated

**getTicket():**
- ✅ Valid ID returns ticket with comments
- ✅ Invalid ID throws TicketNotFoundException
- ✅ Comments ordered oldest first

**listTickets():**
- ✅ Returns paginated results
- ✅ Default sort by createdAt descending
- ✅ Custom sort parameters work
- ✅ Empty list returns empty array

**updateTicket():**
- ✅ Valid update returns updated ticket
- ✅ Partial update (one field) works
- ✅ Field validation applied
- ✅ Status cannot be changed via this endpoint
- ✅ Updated timestamp changed
- ✅ Created timestamp unchanged
- ✅ Invalid ID throws TicketNotFoundException

**updateStatus():**
- ✅ OPEN → IN_PROGRESS succeeds
- ✅ IN_PROGRESS → RESOLVED succeeds
- ✅ RESOLVED → CLOSED succeeds
- ✅ OPEN → CANCELLED succeeds
- ✅ IN_PROGRESS → CANCELLED succeeds
- ✅ CLOSED → anything throws InvalidStatusTransitionException
- ✅ RESOLVED → OPEN throws InvalidStatusTransitionException
- ✅ CANCELLED → anything throws InvalidStatusTransitionException
- ✅ Updated timestamp changes, created timestamp unchanged
- ✅ Invalid ID throws TicketNotFoundException

**searchTickets():**
- ✅ Returns tickets matching keyword (case-insensitive)
- ✅ Searches both title and description
- ✅ Pagination works
- ✅ Empty keyword validation (handled in controller)
- ✅ No matches returns empty array

**filterByStatus():**
- ✅ Returns tickets with matching status
- ✅ Pagination works
- ✅ Invalid status handled (handled in controller)
- ✅ Each status filter returns correct tickets

### CommentService Tests

**addComment():**
- ✅ Valid text returns comment with ID
- ✅ Text validation (blank, too long)
- ✅ Created timestamp set to now (UTC)
- ✅ Invalid ticket ID throws TicketNotFoundException

**getComments():**
- ✅ Returns all comments for ticket
- ✅ Ordered by created time (oldest first)
- ✅ Empty comments returns empty array
- ✅ Invalid ticket ID throws TicketNotFoundException

## Integration Tests (API Endpoints)

**Framework:** Spring Boot Test + TestRestTemplate  
**Database:** H2 in-memory  
**Location:** `src/test/java/com/ticketmanagement/integration/controller/`

### TicketController Tests

**POST /api/tickets**
- ✅ Valid request returns 201 with ticket response
- ✅ Missing title returns 400 with validation error
- ✅ Missing description returns 400
- ✅ Missing priority returns 400
- ✅ Invalid priority value returns 400
- ✅ Title too long returns 400
- ✅ Title blank returns 400
- ✅ Description too long returns 400
- ✅ Assignee too long returns 400
- ✅ Status always OPEN on creation
- ✅ Ticket persisted to database

**GET /api/tickets**
- ✅ Returns 200 with list response
- ✅ Includes pagination metadata (total, page, pageSize)
- ✅ Default pageSize 20, max 100
- ✅ Pagination parameters work (page, pageSize)
- ✅ Sorting works (sortBy, sortDir)
- ✅ Invalid sortBy parameter returns 400
- ✅ Empty result returns 200 with empty array

**GET /api/tickets/{id}**
- ✅ Valid ID returns 200 with ticket
- ✅ Invalid ID returns 404
- ✅ Includes all comments ordered oldest first
- ✅ All fields present in response

**PATCH /api/tickets/{id}**
- ✅ Valid update returns 200
- ✅ Partial update (single field) works
- ✅ Title validation enforced
- ✅ Description validation enforced
- ✅ Invalid ID returns 404
- ✅ Cannot change status via this endpoint (field ignored or error)
- ✅ Updated timestamp changes

**PATCH /api/tickets/{id}/status**
- ✅ OPEN → IN_PROGRESS returns 200
- ✅ IN_PROGRESS → RESOLVED returns 200
- ✅ RESOLVED → CLOSED returns 200
- ✅ OPEN → CANCELLED returns 200
- ✅ IN_PROGRESS → CANCELLED returns 200
- ✅ CLOSED → anything returns 409 with error message
- ✅ RESOLVED → OPEN returns 409
- ✅ CANCELLED → anything returns 409
- ✅ Invalid status value returns 400
- ✅ Invalid ID returns 404
- ✅ Response includes updated ticket

**GET /api/tickets/search?keyword=...**
- ✅ Valid keyword returns 200 with matching tickets
- ✅ Case-insensitive search
- ✅ Searches title and description
- ✅ Blank keyword returns 400
- ✅ Pagination parameters work
- ✅ No matches returns 200 with empty array

**GET /api/tickets?status=...**
- ✅ Valid status returns 200 with filtered tickets
- ✅ Each status returns correct tickets
- ✅ Invalid status returns 400
- ✅ Pagination works with filter
- ✅ Sorting works with filter

### CommentController Tests

**POST /api/tickets/{id}/comments**
- ✅ Valid text returns 201 with comment
- ✅ Blank text returns 400
- ✅ Text too long returns 400
- ✅ Invalid ticket ID returns 404
- ✅ Comment persisted to database

**GET /api/tickets/{id}/comments**
- ✅ Valid ID returns 200 with comments
- ✅ Ordered oldest first
- ✅ Empty comments returns 200 with empty array
- ✅ Invalid ticket ID returns 404

## State Machine Integration Tests

**Location:** `src/test/java/com/ticketmanagement/integration/state_machine/`  
**Class:** `TicketStatusTransitionIntegrationTest`

Test all valid and invalid transitions using the API endpoint (PATCH /api/tickets/{id}/status).

**Valid Transitions (5 tests):**
- ✅ OPEN → IN_PROGRESS succeeds
- ✅ IN_PROGRESS → RESOLVED succeeds
- ✅ RESOLVED → CLOSED succeeds
- ✅ OPEN → CANCELLED succeeds
- ✅ IN_PROGRESS → CANCELLED succeeds

**Invalid Transitions (minimum 3-5 tests each):**
- ❌ CLOSED → OPEN
- ❌ CLOSED → IN_PROGRESS
- ❌ CLOSED → RESOLVED
- ❌ CLOSED → CANCELLED
- ❌ RESOLVED → OPEN
- ❌ RESOLVED → IN_PROGRESS
- ❌ RESOLVED → CANCELLED
- ❌ CANCELLED → OPEN
- ❌ CANCELLED → IN_PROGRESS
- ❌ CANCELLED → RESOLVED
- ❌ IN_PROGRESS → OPEN
- ❌ OPEN → RESOLVED

## Error Scenario Tests

**Exception Handling:**
- ✅ TicketNotFoundException returns 404
- ✅ InvalidStatusTransitionException returns 409
- ✅ Validation exceptions return 400 with field-level errors
- ✅ All errors return structured ErrorResponse format

## Test Data Builders

Create reusable fixtures in `src/test/java/com/ticketmanagement/fixtures/`:

```java
public class TicketTestBuilder {
    private String title = "Test Ticket";
    private String description = "Test Description";
    private Priority priority = Priority.MEDIUM;
    private TicketStatus status = TicketStatus.OPEN;
    private String assignee = null;
    
    public TicketTestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public TicketTestBuilder withStatus(TicketStatus status) {
        this.status = status;
        return this;
    }
    
    public Ticket build() {
        Ticket ticket = new Ticket(title, description, priority);
        ticket.setStatus(status);
        ticket.setAssignee(assignee);
        return ticket;
    }
}
```

## Coverage Targets

| Component | Target | Notes |
|-----------|--------|-------|
| TicketService | 85%+ | Critical business logic |
| CommentService | 80%+ | Business logic |
| TicketController | 75%+ | Happy paths + error cases |
| CommentController | 75%+ | Happy paths + error cases |
| Exception Handler | 70%+ | All exception types |
| Models/DTOs | 50%+ | Simple getters/setters |

## Running Tests

```bash
# All tests
mvn test

# Coverage report
mvn test jacoco:report
# View: target/site/jacoco/index.html

# State machine tests only
mvn test -Dtest=*StatusTransition*

# Specific test class
mvn test -Dtest=TicketServiceTest

# Specific test method
mvn test -Dtest=TicketServiceTest#testCreateTicket_WithValidInput_ReturnsOpenTicket
```

## CI/CD Requirements

- All tests must pass before merge
- Coverage reports generated automatically
- State machine tests are critical — do not skip
- Minimum coverage thresholds enforced
