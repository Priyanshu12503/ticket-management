# Code Review Standards

## Review Checklist

### Functional Correctness
- [ ] Code implements the spec/requirement accurately
- [ ] All edge cases are handled
- [ ] State machine transitions are validated (if applicable)
- [ ] Error handling is appropriate
- [ ] No hardcoded values or magic numbers

### Design & Architecture
- [ ] Follows project structure and naming conventions
- [ ] No unnecessary dependencies or circular imports
- [ ] Single Responsibility Principle: classes have one reason to change
- [ ] Services are not coupled to controllers
- [ ] Repositories are the only database access layer

### Spring Boot Patterns
- [ ] Controllers return `ResponseEntity<T>`, not bare objects
- [ ] Services use `@Transactional` appropriately
- [ ] Repositories extend `JpaRepository`
- [ ] Entities have proper JPA annotations
- [ ] DTOs are separate from entities
- [ ] No business logic in controllers

### Validation
- [ ] Input validated with Jakarta Bean Validation annotations
- [ ] Custom validators implemented correctly
- [ ] Validation messages are user-friendly
- [ ] Backend validation mirrors frontend (don't trust client)

### Error Handling
- [ ] Custom exceptions used for domain errors
- [ ] `@RestControllerAdvice` handles exceptions globally
- [ ] Error responses follow standard format
- [ ] HTTP status codes are semantically correct
  - 400 for validation errors
  - 404 for not found
  - 409 for conflict (state transitions)
  - 500 for server errors

### Testing
- [ ] Unit tests cover happy path and error cases
- [ ] Mocks are used appropriately
- [ ] Integration tests test real database interactions
- [ ] State machine tests cover all valid and invalid transitions
- [ ] Test names describe the scenario clearly
- [ ] No test interdependencies

### Code Quality
- [ ] Follows Java naming conventions
- [ ] Methods are reasonably sized (< 30 lines ideal)
- [ ] Complex logic has explanatory comments
- [ ] No commented-out code
- [ ] No println() or System.out
- [ ] Proper use of logging (SLF4J)

### Security
- [ ] No secrets in code
- [ ] Input sanitized (SQL injection prevention)
- [ ] No sensitive data in error messages
- [ ] Timestamps are in UTC

### Documentation
- [ ] Public methods have JavaDoc
- [ ] Complex business logic has comments
- [ ] No obvious typos in names or comments

### Database
- [ ] Entities have proper JPA annotations
- [ ] `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- [ ] Enums use `@Enumerated(EnumType.STRING)`
- [ ] No N+1 query problems
- [ ] Indexes added for frequently queried columns

## Review Workflow

1. **Author**: Submit PR with clear description
2. **Reviewer**: Run tests locally, check acceptance criteria
3. **Discussion**: Comment on specific lines, request changes
4. **Author**: Address feedback with new commits
5. **Approval**: Mark as approved when satisfied
6. **Merge**: Author merges to main after approval

## Comment Guidelines

### Good Comments
- Reference specific code lines or methods
- Explain *why*, not what (code shows what)
- Suggest concrete improvements
- Acknowledge good practices

Example:
```
Good: "The validation happens in the controller, but it should be in the 
       service layer so it's reusable. Consider moving this to 
       TicketService.validateTicketRequest()."

Bad:  "This is wrong."
```

### Feedback Tone
- Collaborative, not critical
- Use "Consider..." instead of "You should..."
- Praise good practices: "Nice use of @Transactional here"
- Ask questions when uncertain: "Is this handling null correctly?"

## State Machine Reviews

Special attention to ticket status updates:
- [ ] All valid transitions are accepted
- [ ] All invalid transitions are rejected
- [ ] Transition logic is centralized in one place
- [ ] Exception thrown for invalid transitions (not silent failure)
- [ ] Tests cover all transitions explicitly

Example invalid transitions (should throw):
- CLOSED → anything
- RESOLVED → OPEN
- CANCELLED → IN_PROGRESS

## API Endpoint Reviews

- [ ] Correct HTTP method and status code
- [ ] Request/response match documented schemas
- [ ] Error responses follow standard format
- [ ] Pagination working correctly
- [ ] Search/filter parameters work as documented

## Performance Reviews

For critical paths:
- [ ] No N+1 queries (eager loading if needed)
- [ ] Pagination prevents loading all records
- [ ] Indexes created for sort/filter columns
- [ ] No unnecessary object creation in loops

## Common Issues to Watch For

### In Services
```java
// ❌ Don't do this
public TicketResponse getTicket(Long id) {
    return ticketRepository.findById(id).orElse(null);
}

// ✅ Do this
public TicketResponse getTicket(Long id) {
    return ticketRepository.findById(id)
        .map(ticketMapper::toResponse)
        .orElseThrow(() -> new TicketNotFoundException(id));
}
```

### In Controllers
```java
// ❌ Don't do this
@PostMapping
public Ticket createTicket(@RequestBody Ticket ticket) {
    return ticketService.createTicket(ticket);
}

// ✅ Do this
@PostMapping
public ResponseEntity<TicketResponse> createTicket(
    @Valid @RequestBody CreateTicketRequest request) {
    TicketResponse response = ticketService.createTicket(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### In Repositories
```java
// ✅ Good
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(TicketStatus status);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(?1) OR LOWER(t.description) LIKE LOWER(?1)")
    Page<Ticket> searchByKeyword(String keyword, Pageable pageable);
}
```
