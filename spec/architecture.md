# Architecture

## High-Level Design

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                        │
│  (TicketController, CommentController, ExceptionHandler) │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                   Service Layer                         │
│  (TicketService, CommentService)                        │
│  - Business logic                                       │
│  - State machine validation                             │
│  - Transaction management                              │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│               Data Access Layer                         │
│  (TicketRepository, CommentRepository)                  │
│  - Spring Data JPA                                      │
│  - Custom query methods                                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Database Layer                             │
│  PostgreSQL (prod) / H2 (dev & test)                   │
└─────────────────────────────────────────────────────────┘
```

## Request Flow

1. Client sends HTTP request → REST Controller
2. Controller validates input with `@Valid` and Jakarta Bean Validation
3. Controller calls Service layer with request DTO
4. Service validates business rules (state machine, constraints)
5. Service calls Repository for database operations
6. Repository executes query via Spring Data JPA
7. Response flows back as DTO through layers
8. Exceptions caught by `@RestControllerAdvice` for consistent error format

## Components

### REST API Layer

**TicketController**
- POST /api/tickets
- GET /api/tickets
- GET /api/tickets/{id}
- PATCH /api/tickets/{id}
- PATCH /api/tickets/{id}/status
- GET /api/tickets/search
- GET /api/tickets?status=...

**CommentController**
- POST /api/tickets/{id}/comments
- GET /api/tickets/{id}/comments

**GlobalExceptionHandler** (@RestControllerAdvice)
- Maps exceptions to HTTP status codes
- Returns structured error responses
- Logs errors

### Service Layer

**TicketService** (@Service, @Transactional)
- createTicket(CreateTicketRequest) → TicketResponse
- getTicket(UUID id) → TicketResponse
- listTickets(Pageable) → Page<TicketResponse>
- updateTicket(UUID id, UpdateTicketRequest) → TicketResponse
- updateStatus(UUID id, TicketStatus newStatus) → TicketResponse
- searchTickets(String keyword, Pageable) → Page<TicketResponse>
- filterByStatus(TicketStatus status, Pageable) → Page<TicketResponse>

**CommentService** (@Service, @Transactional)
- addComment(UUID ticketId, AddCommentRequest) → CommentResponse
- getComments(UUID ticketId) → List<CommentResponse>

### Data Access Layer

**TicketRepository** (extends JpaRepository<Ticket, UUID>)
- findByStatus(TicketStatus status, Pageable pageable)
- searchByKeyword(String keyword, Pageable pageable) - @Query

**CommentRepository** (extends JpaRepository<Comment, UUID>)
- findByTicketIdOrderByCreatedAtAsc(UUID ticketId)

## Design Principles

- **Layered Architecture**: Clean separation of concerns (API → Service → Data)
- **DTOs**: Request/response models separate from entities
- **Single Responsibility**: Each service handles one domain area
- **Transactions**: Service layer manages transaction boundaries
- **Exception Handling**: Global handler for consistent error responses
- **Validation**: Both annotation-based (@Valid) and business logic validation
- **State Machine**: Centralized in service layer, not in entity

## Technology Stack

- **Java 21** with Spring Boot 3.x
- **Maven** for build
- **Spring Data JPA** with Hibernate
- **PostgreSQL** (production), **H2** (dev/test)
- **Jakarta Bean Validation** for constraints
- **Jackson** for JSON serialization
