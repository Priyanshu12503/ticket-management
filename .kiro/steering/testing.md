# Testing Standards

## Testing Pyramid

```
       /\
      /  \  E2E Tests (React component tests, optional)
     /----\
    /      \
   /  Unit  \ Integration Tests (REST endpoints, database)
  /----+----\
 /           \ Unit Tests (business logic, entities)
/-------------\
```

## Unit Testing

### Framework: JUnit 5 + Mockito

- Test individual methods in isolation
- Mock external dependencies (repositories, services)
- Use `@ExtendWith(MockitoExtension.class)`
- Naming: `<MethodName>_<Scenario>_<ExpectedResult>`

Example:
```java
@Test
void createTicket_WithValidData_ReturnsTicketWithId() {
    // Arrange
    TicketRequest request = new TicketRequest("Title", "Desc", Priority.HIGH);
    
    // Act
    TicketResponse response = ticketService.createTicket(request);
    
    // Assert
    assertNotNull(response.getId());
    assertEquals(TicketStatus.OPEN, response.getStatus());
}
```

### Coverage Targets
- Services: 80%+ coverage
- Controllers: 70%+ (focus on error paths, validation)
- Repositories: Usually not tested (Spring Data JPA)

## Integration Testing

### Framework: Spring Boot Test + RestAssured or MockMvc

- Test API endpoints with `@SpringBootTest`
- Use H2 in-memory database (configured in `application-test.yml`)
- Test database persistence, validation, and business logic together

Example:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TicketControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCreateTicket() {
        TicketRequest request = new TicketRequest("Title", "Desc", Priority.HIGH);
        ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
            "/api/tickets", request, TicketResponse.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
```

## State Machine Testing

### Focus on Ticket Status Transitions

Test all valid transitions:
- OPEN → IN_PROGRESS ✓
- IN_PROGRESS → RESOLVED ✓
- RESOLVED → CLOSED ✓
- OPEN → CANCELLED ✓
- IN_PROGRESS → CANCELLED ✓

Test all invalid transitions (should throw exception):
- CLOSED → OPEN ✗
- RESOLVED → OPEN ✗
- CANCELLED → OPEN ✗
- CANCELLED → IN_PROGRESS ✗
- etc.

Example test:
```java
@Test
void updateStatus_FromResolvedToOpen_ThrowsInvalidTransitionException() {
    Ticket ticket = new Ticket();
    ticket.setStatus(TicketStatus.RESOLVED);
    
    assertThrows(InvalidStatusTransitionException.class, () -> {
        ticketService.updateStatus(ticket.getId(), TicketStatus.OPEN);
    });
}
```

## Test Organization

```
src/test/java/com/ticketmanagement/
├── unit/
│   ├── service/
│   └── model/
├── integration/
│   ├── controller/
│   └── repository/
└── state_machine/
    └── TicketStatusTransitionTest.java
```

## Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=TicketServiceTest

# Specific test method
mvn test -Dtest=TicketServiceTest#testCreateTicket

# With coverage
mvn test jacoco:report
# Report: target/site/jacoco/index.html

# State machine tests only
mvn test -Dtest=*StatusTransition*
```

## Test Data and Fixtures

- Use `@BeforeEach` for common setup
- Create reusable test builders or factories
- Use `@Transactional` to clean up after tests (optional, depends on setup)

## Continuous Integration

- Tests must pass before merging
- Coverage reports generated automatically
- State machine tests are critical — do not skip
