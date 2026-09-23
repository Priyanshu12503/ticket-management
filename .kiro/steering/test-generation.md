# Test Generation Standards

## Test-Driven Development Approach

When implementing a feature:
1. **Understand the spec** — Read requirements and design
2. **Write tests first** — Implement tests for expected behavior
3. **Implement feature** — Write code to pass tests
4. **Verify coverage** — Check test coverage targets
5. **Refactor** — Clean up code while keeping tests passing

## Unit Test Generation

### Service Tests Template

```java
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    
    @Mock
    private TicketRepository ticketRepository;
    
    @InjectMocks
    private TicketService ticketService;
    
    // Test methods follow: <Method>_<Scenario>_<ExpectedResult>
    
    @Test
    void createTicket_WithValidRequest_ReturnsTicketWithOpenStatus() {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest(
            "Title", "Description", Priority.HIGH, null
        );
        Ticket savedTicket = new Ticket(/* ... */);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        
        // Act
        TicketResponse response = ticketService.createTicket(request);
        
        // Assert
        assertNotNull(response.getId());
        assertEquals(TicketStatus.OPEN, response.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }
    
    @Test
    void createTicket_WithBlankTitle_ThrowsValidationException() {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest(
            "", "Description", Priority.HIGH, null
        );
        
        // Act & Assert
        assertThrows(ValidationException.class, 
            () -> ticketService.createTicket(request)
        );
    }
}
```

### Generate Tests For

**Service Methods:**
- Happy path (valid input)
- Validation failures (invalid input)
- Not found scenarios
- Business rule violations
- State transitions (if applicable)

**Controllers:**
- Happy path (200/201)
- Validation failure (400)
- Not found (404)
- State transition error (409)
- Invalid HTTP method (405)

**Entities/Models:**
- Enum values
- Setter/getter behavior
- Validation constraints
- Default values

## Integration Test Generation

### State Machine Tests

Create a dedicated test class for status transitions:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TicketStatusTransitionIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    private String ticketId;
    
    @BeforeEach
    void setUp() {
        Ticket ticket = new Ticket("Title", "Description", Priority.HIGH);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        ticketId = saved.getId().toString();
    }
    
    // Valid Transitions - should succeed
    
    @Test
    void updateStatus_FromOpenToInProgress_Succeeds() {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS);
        
        ResponseEntity<TicketResponse> response = restTemplate.exchange(
            "/api/tickets/{id}/status",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            TicketResponse.class,
            ticketId
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TicketStatus.IN_PROGRESS, response.getBody().getStatus());
    }
    
    @Test
    void updateStatus_FromOpenToCancelled_Succeeds() {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.CANCELLED);
        
        ResponseEntity<TicketResponse> response = restTemplate.exchange(
            "/api/tickets/{id}/status",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            TicketResponse.class,
            ticketId
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TicketStatus.CANCELLED, response.getBody().getStatus());
    }
    
    // Invalid Transitions - should fail
    
    @Test
    void updateStatus_FromOpenToResolved_ThrowsConflict() {
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.RESOLVED);
        
        ResponseEntity<?> response = restTemplate.exchange(
            "/api/tickets/{id}/status",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            ErrorResponse.class,
            ticketId
        );
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
    
    @Test
    void updateStatus_FromClosedToAnything_ThrowsConflict() {
        // First, transition to CLOSED
        Ticket ticket = ticketRepository.findById(UUID.fromString(ticketId)).get();
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);
        
        // Try to transition from CLOSED
        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.OPEN);
        
        ResponseEntity<?> response = restTemplate.exchange(
            "/api/tickets/{id}/status",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            ErrorResponse.class,
            ticketId
        );
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
```

## Test Data Builders

Create reusable builders for test data:

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

Usage:
```java
Ticket ticket = new TicketTestBuilder()
    .withStatus(TicketStatus.IN_PROGRESS)
    .withTitle("Critical Bug")
    .build();
```

## API Endpoint Test Checklist

For each endpoint, write tests covering:

### POST /api/tickets
- ✅ Valid creation returns 201
- ✅ Missing required field returns 400
- ✅ Field validation (title length, etc.) returns 400
- ✅ Status always initialized to OPEN
- ✅ Created ticket in database persists

### GET /api/tickets
- ✅ Returns all tickets with 200
- ✅ Includes pagination metadata
- ✅ Empty list returns 200 with empty array
- ✅ Pagination parameters work (page, pageSize)
- ✅ Sorting works (sortBy, sortDir)

### GET /api/tickets/{id}
- ✅ Valid ID returns 200 with ticket
- ✅ Invalid ID returns 404
- ✅ Response includes all fields and comments

### PATCH /api/tickets/{id}
- ✅ Valid update returns 200
- ✅ Partial update (only some fields) works
- ✅ Invalid ID returns 404
- ✅ Validation errors return 400

### PATCH /api/tickets/{id}/status
- ✅ Valid transitions succeed
- ✅ Invalid transitions return 409
- ✅ Invalid status value returns 400

### POST /api/tickets/{id}/comments
- ✅ Valid comment returns 201
- ✅ Empty text returns 400
- ✅ Invalid ticket ID returns 404
- ✅ Comment persisted to database

### GET /api/tickets/{id}/comments
- ✅ Returns all comments with 200
- ✅ Ordered by creation time (newest first)

### GET /api/tickets/search
- ✅ Searches title and description
- ✅ Case-insensitive search
- ✅ Pagination works
- ✅ Returns 200 with matching tickets

### GET /api/tickets?status=...
- ✅ Filters by status
- ✅ Multiple statuses if supported
- ✅ Returns 200 with matching tickets

## Coverage Targets

| Component | Target |
|-----------|--------|
| Services | 80%+ |
| Controllers | 70%+ |
| Repositories | N/A (Spring Data) |
| DTOs | N/A |
| Models | 60%+ |

## Test Execution

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage: target/site/jacoco/index.html

# Run state machine tests only
mvn test -Dtest=*StatusTransition*

# Run and skip integration tests (fast)
mvn test -DskipITs

# Run specific test class
mvn test -Dtest=TicketServiceTest

# Run specific test method
mvn test -Dtest=TicketServiceTest#testCreateTicket_WithValidRequest_ReturnsTicketWithOpenStatus
```

## Common Test Patterns

### Testing Validation
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "a".repeat(201)})
void createTicket_WithInvalidTitle_ThrowsException(String invalidTitle) {
    CreateTicketRequest request = new CreateTicketRequest(
        invalidTitle, "Desc", Priority.HIGH, null
    );
    
    assertThrows(ValidationException.class, () -> ticketService.createTicket(request));
}
```

### Testing Exception Messages
```java
@Test
void createTicket_WithBlankTitle_IncludesFieldNameInError() {
    CreateTicketRequest request = new CreateTicketRequest(
        "", "Desc", Priority.HIGH, null
    );
    
    ValidationException ex = assertThrows(ValidationException.class, 
        () -> ticketService.createTicket(request)
    );
    
    assertTrue(ex.getMessage().contains("title"));
}
```

### Testing Database Persistence
```java
@Test
void createTicket_PersistsToDatabase() {
    CreateTicketRequest request = new CreateTicketRequest(
        "Title", "Desc", Priority.HIGH, null
    );
    
    TicketResponse response = ticketService.createTicket(request);
    
    Optional<Ticket> persisted = ticketRepository.findById(UUID.fromString(response.getId()));
    assertTrue(persisted.isPresent());
    assertEquals("Title", persisted.get().getTitle());
}
```
