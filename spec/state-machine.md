# State Machine

## Ticket Status Flow

Five states define the ticket lifecycle:
- **OPEN** - Newly created, awaiting work
- **IN_PROGRESS** - Work started
- **RESOLVED** - Issue fixed, awaiting confirmation
- **CLOSED** - Confirmed resolved, terminal state
- **CANCELLED** - Cancelled, terminal state

## Valid Transitions

All transitions are initiated via PATCH /api/tickets/{id}/status.

| From | To | Allowed |
|------|-----|---------|
| OPEN | IN_PROGRESS | ✅ Yes |
| OPEN | CANCELLED | ✅ Yes |
| IN_PROGRESS | RESOLVED | ✅ Yes |
| IN_PROGRESS | CANCELLED | ✅ Yes |
| RESOLVED | CLOSED | ✅ Yes |

**Visual Flow:**

```
      ┌─────────────┐
      │    OPEN     │
      └─────┬───────┘
            │
        ┌───┴──────────────┐
        │                  │
        ▼                  ▼
   ┌──────────┐      ┌──────────┐
   │IN_PROGRESS│      │CANCELLED │ (terminal)
   └──────┬───┘      └──────────┘
          │
      ┌───┴────────┐
      │            │
      ▼            ▼
  ┌─────────┐  ┌──────────┐
  │RESOLVED │  │CANCELLED │ (terminal)
  └────┬────┘  └──────────┘
       │
       ▼
  ┌──────────┐
  │  CLOSED  │ (terminal)
  └──────────┘
```

## Invalid Transitions

All other transitions must be rejected with HTTP 409 Conflict.

**Examples of invalid transitions:**

| From | To | Reason |
|------|-----|--------|
| CLOSED | OPEN | Terminal state cannot transition |
| CLOSED | IN_PROGRESS | Terminal state cannot transition |
| CLOSED | RESOLVED | Terminal state cannot transition |
| CANCELLED | OPEN | Terminal state cannot transition |
| CANCELLED | IN_PROGRESS | Terminal state cannot transition |
| RESOLVED | OPEN | Already resolved, cannot reopen |
| RESOLVED | IN_PROGRESS | Already resolved, cannot resume |
| IN_PROGRESS | OPEN | Cannot regress to earlier state |

Any attempt to transition to invalid state returns:
```json
{
  "status": 409,
  "message": "Invalid status transition",
  "details": "Cannot transition from RESOLVED to OPEN",
  "timestamp": "2026-09-22T10:30:00Z"
}
```

## Implementation

### Transition Rules Map

Store allowed transitions in TicketService:

```java
static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = 
    Map.ofEntries(
        Map.entry(TicketStatus.OPEN, Set.of(
            TicketStatus.IN_PROGRESS,
            TicketStatus.CANCELLED
        )),
        Map.entry(TicketStatus.IN_PROGRESS, Set.of(
            TicketStatus.RESOLVED,
            TicketStatus.CANCELLED
        )),
        Map.entry(TicketStatus.RESOLVED, Set.of(
            TicketStatus.CLOSED
        )),
        Map.entry(TicketStatus.CLOSED, Set.of()),
        Map.entry(TicketStatus.CANCELLED, Set.of())
    );
```

### Validation Method

```java
public TicketResponse updateStatus(UUID ticketId, TicketStatus newStatus) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new TicketNotFoundException(ticketId));
    
    TicketStatus currentStatus = ticket.getStatus();
    
    if (!isValidTransition(currentStatus, newStatus)) {
        throw new InvalidStatusTransitionException(currentStatus, newStatus);
    }
    
    ticket.setStatus(newStatus);
    ticket.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    Ticket updated = ticketRepository.save(ticket);
    return ticketMapper.toResponse(updated);
}

private boolean isValidTransition(TicketStatus from, TicketStatus to) {
    return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
}
```

### Custom Exception

```java
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
            InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(
                409, 
                "Invalid status transition", 
                ex.getMessage(), 
                now()
            ));
    }
}
```

## Testing Requirements

All transitions must be tested:

### Valid Transitions (5 tests minimum)
- ✅ OPEN → IN_PROGRESS
- ✅ IN_PROGRESS → RESOLVED
- ✅ RESOLVED → CLOSED
- ✅ OPEN → CANCELLED
- ✅ IN_PROGRESS → CANCELLED

### Invalid Transitions (at least 3 tests each)
- ❌ CLOSED → any
- ❌ RESOLVED → OPEN
- ❌ CANCELLED → any
- ❌ Plus others as comprehensive coverage allows

See test-strategy.md for test implementation details.

## Future Enhancements (Not in MVP)

- Transition audit log (record who changed status and when)
- Transition notifications/events
- Role-based transition restrictions (e.g., only manager can close)
- Transition guards (e.g., cannot close without resolution comment)
