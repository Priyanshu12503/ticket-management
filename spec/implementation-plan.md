# Implementation Plan

Breakdown of work into focused, testable tasks. Each task builds on previous ones and includes what to test.

## Phase 1: Project Setup

### Task 1.1: Maven Project Structure

**Objective:** Create Maven project with Spring Boot dependencies

**Dependencies:** None (start here)

**Deliverables:**
- `pom.xml` with Spring Boot 3.x, Java 21, all required dependencies
- Basic project structure: `src/main/java/com/ticketmanagement/`, `src/test/java/`, `src/main/resources/`
- `application.yml` configuration files (dev, test, prod profiles)
- `.gitignore` excluding `.env`, secrets, build artifacts

**Dependencies to Add:**
- Spring Boot Web, Data JPA, Validation
- PostgreSQL driver + H2 for tests
- Lombok (optional, for DTOs)
- JUnit 5, Mockito, AssertJ
- Jackson for JSON

**Testing:**
- ✅ Maven builds without errors: `mvn clean compile`
- ✅ Can run empty Spring Boot app: `mvn spring-boot:run`
- ✅ Test profile uses H2 in-memory

**Files Created:**
- `pom.xml`
- `src/main/resources/application.yml` (dev)
- `src/main/resources/application-test.yml` (H2)
- `src/main/resources/application-prod.yml` (PostgreSQL placeholder)
- `src/main/java/com/ticketmanagement/TicketManagementApplication.java`

**Estimated Time:** 30 min  
**Complexity:** Low

---

## Phase 2: Data Layer

### Task 2.1: Database Configuration + H2 Setup

**Objective:** Configure database for both H2 (test) and PostgreSQL (prod), verify connection

**Dependencies:** Task 1.1 (Maven setup)

**Deliverables:**
- H2 database configured for test profile
- PostgreSQL configuration template for prod (with environment variable placeholders)
- Database connection verified in both profiles

**Testing:**
- ✅ H2 connection works: `mvn test` succeeds
- ✅ PostgreSQL config correct (manual verification)
- ✅ JPA logging shows SQL (enable in application-dev.yml)

**Files Modified:**
- `src/main/resources/application-test.yml` — H2 config complete
- `src/main/resources/application-prod.yml` — PostgreSQL config template

**Estimated Time:** 20 min  
**Complexity:** Low

---

### Task 2.2: Entity Models (Ticket, Comment, Enums)

**Objective:** Implement JPA entities with all validation annotations and constraints

**Dependencies:** Task 2.1 (Database config)

**Deliverables:**
- `Ticket` entity with all fields, validation, timestamps, indexes
- `Comment` entity with FK to Ticket, cascade delete
- `TicketStatus` and `Priority` enums (stored as strings)

**Entity Details:**
```
Ticket:
  - id (UUID, auto-generated)
  - title (String, 1-200, @NotBlank, @Size)
  - description (String, 1-2000, TEXT, @NotBlank, @Size)
  - status (Enum, @Enumerated(STRING), default OPEN)
  - priority (Enum, @Enumerated(STRING), @NotNull)
  - assignee (String, 0-100, nullable)
  - createdAt (@CreationTimestamp, UTC)
  - updatedAt (@UpdateTimestamp, UTC)
  - comments (OneToMany, cascade all, orphan removal)

Comment:
  - id (UUID, auto-generated)
  - ticket (ManyToOne, FK, lazy)
  - text (String, 1-2000, TEXT, @NotBlank, @Size)
  - createdAt (@CreationTimestamp, UTC)
```

**Testing:**
- ✅ Entities load without errors
- ✅ Validation annotations work: create invalid Ticket, catch validation error
- ✅ Hibernate generates correct SQL (verify in logs)
- ✅ Timestamps are OffsetDateTime in UTC
- ✅ Cascade delete works: delete ticket, comments gone

**Files Created:**
- `src/main/java/com/ticketmanagement/model/Ticket.java`
- `src/main/java/com/ticketmanagement/model/Comment.java`
- `src/main/java/com/ticketmanagement/model/TicketStatus.java`
- `src/main/java/com/ticketmanagement/model/Priority.java`

**Estimated Time:** 45 min  
**Complexity:** Medium

---

### Task 2.3: DTOs (Request/Response Models)

**Objective:** Create data transfer objects for API requests and responses

**Dependencies:** Task 2.2 (Entities)

**Deliverables:**
- `CreateTicketRequest` — title, description, priority, assignee (with validation)
- `UpdateTicketRequest` — all fields optional
- `UpdateStatusRequest` — status only
- `AddCommentRequest` — text only
- `TicketResponse` — full ticket with nested comments
- `CommentResponse` — id, text, createdAt
- `ListTicketsResponse` — data[], total, page, pageSize

**Record or Class:** Use records (Java 16+) if possible, else @Data Lombok

**Testing:**
- ✅ Jackson serialization/deserialization works
- ✅ Validation annotations trigger on invalid data
- ✅ Nested objects (comments in TicketResponse) serialize correctly

**Files Created:**
- `src/main/java/com/ticketmanagement/dto/CreateTicketRequest.java`
- `src/main/java/com/ticketmanagement/dto/UpdateTicketRequest.java`
- `src/main/java/com/ticketmanagement/dto/UpdateStatusRequest.java`
- `src/main/java/com/ticketmanagement/dto/AddCommentRequest.java`
- `src/main/java/com/ticketmanagement/dto/TicketResponse.java`
- `src/main/java/com/ticketmanagement/dto/CommentResponse.java`
- `src/main/java/com/ticketmanagement/dto/ListTicketsResponse.java`
- `src/main/java/com/ticketmanagement/dto/ErrorResponse.java`
- `src/main/java/com/ticketmanagement/dto/FieldError.java`

**Estimated Time:** 30 min  
**Complexity:** Low

---

### Task 2.4: Repositories

**Objective:** Create Spring Data JPA repositories with custom query methods

**Dependencies:** Task 2.2 (Entities)

**Deliverables:**
- `TicketRepository` extending `JpaRepository<Ticket, UUID>`
  - `findByStatus(TicketStatus, Pageable)` — for filtering
  - `searchByKeyword(String keyword, Pageable)` — JPQL query for title/description
- `CommentRepository` extending `JpaRepository<Comment, UUID>`
  - `findByTicketIdOrderByCreatedAtAsc(UUID)` — get comments for ticket

**Testing:**
- ✅ Repository methods return correct data types
- ✅ Pageable parameters work
- ✅ Search query is case-insensitive
- ✅ Comments ordered oldest first
- ✅ No N+1 queries (verify in logs)

**Files Created:**
- `src/main/java/com/ticketmanagement/repository/TicketRepository.java`
- `src/main/java/com/ticketmanagement/repository/CommentRepository.java`

**Estimated Time:** 25 min  
**Complexity:** Low

---

## Phase 3: Business Logic

### Task 3.1: Custom Exceptions + Global Exception Handler

**Objective:** Implement exception hierarchy and centralized error handling

**Dependencies:** Task 2.3 (DTOs for ErrorResponse)

**Deliverables:**
- `TicketNotFoundException` — extends RuntimeException
- `InvalidStatusTransitionException` — extends RuntimeException
- `ValidationException` — custom or use MethodArgumentNotValidException
- `GlobalExceptionHandler` — @RestControllerAdvice
  - Handle TicketNotFoundException → 404
  - Handle InvalidStatusTransitionException → 409
  - Handle validation errors → 400 with field-level details
  - Handle generic exceptions → 500

**Testing:**
- ✅ Each exception maps to correct HTTP status
- ✅ Error response includes all required fields (status, message, details, timestamp)
- ✅ Validation errors include field names
- ✅ Timestamp is in ISO 8601 UTC format

**Files Created:**
- `src/main/java/com/ticketmanagement/exception/TicketNotFoundException.java`
- `src/main/java/com/ticketmanagement/exception/InvalidStatusTransitionException.java`
- `src/main/java/com/ticketmanagement/exception/GlobalExceptionHandler.java`

**Estimated Time:** 40 min  
**Complexity:** Medium

---

### Task 3.2: Mappers (Entity ↔ DTO)

**Objective:** Create mappers to convert between entities and DTOs

**Dependencies:** Task 2.2 (Entities), Task 2.3 (DTOs)

**Deliverables:**
- `TicketMapper` interface with methods:
  - `toResponse(Ticket)` → TicketResponse
  - `toEntity(CreateTicketRequest)` → Ticket
  - `updateEntity(UpdateTicketRequest, Ticket)` → update existing ticket
- `CommentMapper` interface with method:
  - `toResponse(Comment)` → CommentResponse

**Implementation:** Use MapStruct or manual mappers

**Testing:**
- ✅ Ticket → TicketResponse includes comments
- ✅ Comments ordered correctly
- ✅ Timestamps formatted as ISO 8601
- ✅ CreateTicketRequest → Ticket has OPEN status by default
- ✅ UUIDs converted to strings in responses

**Files Created:**
- `src/main/java/com/ticketmanagement/mapper/TicketMapper.java`
- `src/main/java/com/ticketmanagement/mapper/CommentMapper.java`

**Estimated Time:** 30 min  
**Complexity:** Low

---

### Task 3.3: State Machine Validator

**Objective:** Implement ticket status transition validation logic

**Dependencies:** Task 3.1 (Exceptions)

**Deliverables:**
- `TicketStatusValidator` class or component with:
  - Static map of allowed transitions
  - Method: `isValidTransition(from, to)` → boolean
  - Throw `InvalidStatusTransitionException` for invalid transitions

**Allowed Transitions:**
```
OPEN → IN_PROGRESS, CANCELLED
IN_PROGRESS → RESOLVED, CANCELLED
RESOLVED → CLOSED
CLOSED → (none)
CANCELLED → (none)
```

**Testing:**
- ✅ All 5 valid transitions pass validation
- ✅ Invalid transitions throw exception
- ✅ Terminal states (CLOSED, CANCELLED) cannot transition
- ✅ Exception message includes from/to status

**Files Created:**
- `src/main/java/com/ticketmanagement/validator/TicketStatusValidator.java`

**Estimated Time:** 20 min  
**Complexity:** Low

---

### Task 3.4: TicketService (CRUD Operations)

**Objective:** Implement core business logic for ticket operations

**Dependencies:** Task 2.2 (Entities), Task 2.4 (Repositories), Task 3.1 (Exceptions), Task 3.2 (Mappers)

**Deliverables:**
- `TicketService` @Service with @Transactional:
  - `createTicket(CreateTicketRequest)` → TicketResponse
  - `getTicket(UUID id)` → TicketResponse
  - `listTickets(Pageable)` → Page<TicketResponse>
  - `updateTicket(UUID id, UpdateTicketRequest)` → TicketResponse

**Details:**
- Create: Save entity, set status to OPEN, return response
- Get: Find by ID or throw TicketNotFoundException
- List: Return paginated results, default sort by createdAt DESC
- Update: Validate ticket exists, update fields, set updatedAt, return response

**Testing:**
- ✅ Create with valid data returns OPEN ticket
- ✅ Create validation errors thrown
- ✅ Get non-existent ticket throws TicketNotFoundException
- ✅ List returns paginated results
- ✅ Update existing ticket updates fields
- ✅ Update non-existent ticket throws exception
- ✅ Transaction commits: data persists

**Files Created:**
- `src/main/java/com/ticketmanagement/service/TicketService.java`

**Unit Tests:**
- `src/test/java/com/ticketmanagement/unit/service/TicketServiceTest.java`

**Estimated Time:** 60 min  
**Complexity:** Medium-High

---

### Task 3.5: TicketService (Status Updates + State Machine)

**Objective:** Implement status update with state machine validation

**Dependencies:** Task 3.3 (State Machine Validator), Task 3.4 (TicketService)

**Deliverables:**
- Add to TicketService:
  - `updateStatus(UUID id, TicketStatus newStatus)` → TicketResponse
  - Validates transition using TicketStatusValidator
  - Throws InvalidStatusTransitionException if invalid
  - Updates status and updatedAt, saves to database

**Testing:**
- ✅ All 5 valid transitions succeed
- ✅ All invalid transitions throw exception
- ✅ CLOSED and CANCELLED cannot transition further
- ✅ Status persists to database
- ✅ Exception message is informative

**Files Modified:**
- `src/main/java/com/ticketmanagement/service/TicketService.java`

**Unit Tests (add to existing TicketServiceTest):**
- Valid transition tests (5)
- Invalid transition tests (minimum 3)

**Estimated Time:** 30 min  
**Complexity:** Medium

---

### Task 3.6: CommentService

**Objective:** Implement comment operations (add, retrieve)

**Dependencies:** Task 2.4 (Repositories), Task 3.1 (Exceptions), Task 3.2 (Mappers)

**Deliverables:**
- `CommentService` @Service with @Transactional:
  - `addComment(UUID ticketId, AddCommentRequest)` → CommentResponse
  - `getComments(UUID ticketId)` → List<CommentResponse>

**Details:**
- Add comment: Verify ticket exists, create comment, save, return response
- Get comments: Find all comments for ticket, order by createdAt ASC

**Testing:**
- ✅ Add comment to existing ticket succeeds
- ✅ Add comment to non-existent ticket throws TicketNotFoundException
- ✅ Get comments returns all comments ordered oldest first
- ✅ Empty comments returns empty list
- ✅ Comment persists to database

**Files Created:**
- `src/main/java/com/ticketmanagement/service/CommentService.java`

**Unit Tests:**
- `src/test/java/com/ticketmanagement/unit/service/CommentServiceTest.java`

**Estimated Time:** 30 min  
**Complexity:** Medium

---

## Phase 4: REST API

### Task 4.1: TicketController (CRUD Endpoints)

**Objective:** Implement REST endpoints for ticket CRUD operations

**Dependencies:** Task 3.4 (TicketService CRUD)

**Deliverables:**
- `TicketController` @RestController @RequestMapping("/api/tickets"):
  - `POST /api/tickets` — createTicket (201 Created)
  - `GET /api/tickets` — listTickets (200 OK)
  - `GET /api/tickets/{id}` — getTicket (200 OK)
  - `PATCH /api/tickets/{id}` — updateTicket (200 OK)

**Details:**
- Use @Valid for request validation
- Return ResponseEntity with appropriate status codes
- Service calls delegate to TicketService
- Exceptions caught by GlobalExceptionHandler

**Testing:**
- ✅ POST with valid data returns 201
- ✅ POST with invalid data returns 400 with field errors
- ✅ GET /api/tickets returns 200 with list
- ✅ GET /api/tickets/{id} with valid ID returns 200
- ✅ GET /api/tickets/{id} with invalid ID returns 404
- ✅ PATCH updates fields and returns 200

**Files Created:**
- `src/main/java/com/ticketmanagement/controller/TicketController.java`

**Integration Tests:**
- `src/test/java/com/ticketmanagement/integration/controller/TicketControllerTest.java`

**Estimated Time:** 45 min  
**Complexity:** Medium

---

### Task 4.2: TicketController (Status Update Endpoint)

**Objective:** Implement PATCH /api/tickets/{id}/status with state machine validation

**Dependencies:** Task 3.5 (TicketService status update), Task 4.1 (TicketController)

**Deliverables:**
- Add to TicketController:
  - `PATCH /api/tickets/{id}/status` — updateStatus (200 OK or 409 Conflict)

**Testing:**
- ✅ Valid transitions return 200
- ✅ Invalid transitions return 409 with error message
- ✅ Invalid status value returns 400
- ✅ Non-existent ticket returns 404

**Files Modified:**
- `src/main/java/com/ticketmanagement/controller/TicketController.java`

**Integration Tests (add to existing TicketControllerTest):**
- All 5 valid transitions
- All invalid transitions
- Error cases

**Estimated Time:** 25 min  
**Complexity:** Medium

---

### Task 4.3: CommentController

**Objective:** Implement REST endpoints for comments

**Dependencies:** Task 3.6 (CommentService)

**Deliverables:**
- `CommentController` @RestController @RequestMapping("/api/tickets/{id}/comments"):
  - `POST /api/tickets/{id}/comments` — addComment (201 Created)
  - `GET /api/tickets/{id}/comments` — getComments (200 OK)

**Testing:**
- ✅ POST with valid text returns 201
- ✅ POST with blank text returns 400
- ✅ POST with long text returns 400
- ✅ POST with non-existent ticket returns 404
- ✅ GET returns all comments ordered oldest first
- ✅ GET non-existent ticket returns 404

**Files Created:**
- `src/main/java/com/ticketmanagement/controller/CommentController.java`

**Integration Tests:**
- `src/test/java/com/ticketmanagement/integration/controller/CommentControllerTest.java`

**Estimated Time:** 30 min  
**Complexity:** Medium

---

## Phase 5: Advanced Features

### Task 5.1: Search Endpoint

**Objective:** Implement ticket search by keyword (title + description)

**Dependencies:** Task 2.4 (Repositories with search query), Task 4.1 (TicketController)

**Deliverables:**
- Add to TicketRepository:
  - `searchByKeyword(String keyword, Pageable)` — JPQL with LOWER() for case-insensitive
- Add to TicketService:
  - `searchTickets(String keyword, Pageable)` — call repository, map results
- Add to TicketController:
  - `GET /api/tickets/search?keyword=...` — searchTickets (200 OK)

**Validation:**
- Keyword must not be blank (handled in controller validation)

**Testing:**
- ✅ Search returns matching tickets (title and description)
- ✅ Search is case-insensitive
- ✅ Pagination works
- ✅ Blank keyword returns 400
- ✅ No matches returns 200 with empty array

**Files Modified:**
- `src/main/java/com/ticketmanagement/repository/TicketRepository.java`
- `src/main/java/com/ticketmanagement/service/TicketService.java`
- `src/main/java/com/ticketmanagement/controller/TicketController.java`

**Integration Tests (add to TicketControllerTest):**
- Search scenarios

**Estimated Time:** 35 min  
**Complexity:** Medium

---

### Task 5.2: Filter by Status + Pagination

**Objective:** Implement status filtering and ensure pagination works everywhere

**Dependencies:** Task 4.1 (TicketController list endpoint)

**Deliverables:**
- Add query parameter handling to TicketController:
  - `GET /api/tickets?status=OPEN&page=1&pageSize=20&sortBy=createdAt&sortDir=desc`
  - Validate status enum value
  - Pass Pageable to service
- TicketService already supports pagination (listTickets uses Pageable)

**Details:**
- Add `@RequestParam(required = false)` for status, sortBy, sortDir
- Validate status is valid enum
- Build Pageable with sort directions
- Return ListTicketsResponse with pagination metadata

**Testing:**
- ✅ Filter by status returns only tickets with that status
- ✅ Invalid status returns 400
- ✅ Pagination parameters work (page, pageSize)
- ✅ Sorting works (sortBy title, createdAt, priority; sortDir asc/desc)
- ✅ Response includes total count and pagination info

**Files Modified:**
- `src/main/java/com/ticketmanagement/controller/TicketController.java`

**Integration Tests:**
- Filter scenarios
- Pagination scenarios
- Sorting scenarios

**Estimated Time:** 40 min  
**Complexity:** Medium

---

### Task 5.3: Input Validation + Meaningful Error Messages

**Objective:** Ensure all validation rules are enforced and error messages are clear

**Dependencies:** Task 2.3 (DTOs with validation), Task 3.1 (GlobalExceptionHandler)

**Deliverables:**
- Review all DTOs and ensure complete validation:
  - Title: 1-200 chars, non-blank
  - Description: 1-2000 chars, non-blank
  - Priority: valid enum
  - Assignee: 0-100 chars, optional
  - Comment text: 1-2000 chars, non-blank
- GlobalExceptionHandler extracts field-level errors
- Error response includes field-level details

**Testing:**
- ✅ Each validation rule enforced at DTO level
- ✅ Validation errors return 400 with field names and messages
- ✅ Error messages are user-friendly
- ✅ All fields have appropriate constraints

**Files to Review/Modify:**
- `src/main/java/com/ticketmanagement/dto/*`
- `src/main/java/com/ticketmanagement/exception/GlobalExceptionHandler.java`

**Testing:**
- Integration tests for each validation rule

**Estimated Time:** 30 min  
**Complexity:** Medium

---

## Phase 6: State Machine Integration Tests

### Task 6.1: State Machine Integration Tests

**Objective:** Comprehensive testing of all ticket status transitions via API

**Dependencies:** Task 4.2 (Status update endpoint)

**Deliverables:**
- `TicketStatusTransitionIntegrationTest` tests:
  - All 5 valid transitions (each test creates ticket in initial state, updates status, verifies success)
  - Invalid transitions: CLOSED → anything, RESOLVED → OPEN, RESOLVED → IN_PROGRESS, CANCELLED → anything, etc.
  - Error responses include 409 status and clear message

**Test Structure:**
- Setup: Create ticket with specific status
- Act: Call PATCH /api/tickets/{id}/status
- Assert: Verify status changed or exception thrown

**Testing:**
- ✅ Valid transitions via API succeed
- ✅ Invalid transitions return 409 Conflict
- ✅ Exception message is clear
- ✅ Status persists to database
- ✅ Terminal states prevent further transitions

**Files Created:**
- `src/test/java/com/ticketmanagement/integration/state_machine/TicketStatusTransitionIntegrationTest.java`

**Estimated Time:** 45 min  
**Complexity:** Medium

---

## Phase 7: End-to-End Testing

### Task 7.1: Full API Scenario Tests

**Objective:** Test complete user workflows via API

**Dependencies:** All backend tasks completed

**Deliverables:**
- Integration tests for full scenarios:
  1. Create ticket → view it → update fields → add comments → view comments
  2. Create ticket → change status through workflow → verify transitions
  3. Create multiple tickets → search → filter → paginate
  4. Verify all error paths (404s, 409s, 400s)

**Testing:**
- ✅ Complete workflows work end-to-end
- ✅ State persists between API calls
- ✅ All error paths handled gracefully

**Files Created:**
- `src/test/java/com/ticketmanagement/integration/FullWorkflowTest.java`

**Estimated Time:** 45 min  
**Complexity:** Medium

---

## Phase 8: Frontend

### Task 8.1: React/Next.js Frontend Setup

**Objective:** Create React application that consumes the REST API

**Dependencies:** All backend APIs complete and tested

**Deliverables:**
- New React/Next.js project (separate or `/frontend/` directory)
- Basic project structure with components
- API client/fetch wrapper

**Do Not Implement Yet** — placeholder only

**Estimated Time:** 30 min (setup only)  
**Complexity:** Low

---

### Task 8.2: Frontend Pages (List, Details, Create, Edit)

**Objective:** Implement UI pages that match UI flow spec

**Dependencies:** Task 8.1 (Frontend setup), all backend complete

**Deliverables:**
- List Tickets page with pagination, sorting, filtering, search
- View Ticket Details page with comments and status update dropdown
- Create Ticket form
- Edit Ticket form
- Error handling and toasts

**Testing:**
- ✅ Pages load and display data
- ✅ Forms submit and call API
- ✅ Error messages display
- ✅ Status dropdown only shows valid transitions

**Do Not Implement Yet** — plan only

**Estimated Time:** 120-180 min  
**Complexity:** Medium

---

## Phase 9: Integration & Final Review

### Task 9.1: Full Stack Testing

**Objective:** Test backend and frontend together

**Dependencies:** All tasks completed

**Deliverables:**
- Manual testing checklist (or E2E tests with Cypress/Playwright)
- Verify: Create → Read → Update → Delete workflows
- Verify: Search, filter, pagination
- Verify: Status transitions (valid and invalid)
- Verify: Comments
- Verify: Error handling
- Verify: Data persistence

**Estimated Time:** 90 min  
**Complexity:** Medium

---

### Task 9.2: Code Review & Cleanup

**Objective:** Review code against standards, clean up

**Dependencies:** Task 9.1 (Full stack testing)

**Deliverables:**
- Code review checklist (per steering/code-review.md)
- Verify no hardcoded secrets
- Verify proper error handling everywhere
- Verify test coverage targets met
- Verify documentation complete

**Estimated Time:** 60 min  
**Complexity:** Low

---

### Task 9.3: Documentation + Deployment Prep

**Objective:** Finalize documentation and prepare for deployment

**Dependencies:** All tasks

**Deliverables:**
- `docs/API.md` — Complete API reference
- `docs/DEVELOPMENT.md` — Setup and run locally
- `docs/DEPLOYMENT.md` — Production deployment steps
- `CHANGELOG.md` — Version 1.0.0 release notes
- `README.md` — Project overview

**Estimated Time:** 60 min  
**Complexity:** Low

---

## Summary by Phase

| Phase | Tasks | Dependencies | Estimated Time |
|-------|-------|---|---|
| 1. Setup | 1 task | None | 30 min |
| 2. Data Layer | 4 tasks | Sequential | 2 hours |
| 3. Business Logic | 6 tasks | Sequential | 3 hours |
| 4. REST API | 3 tasks | Sequential | 1.5 hours |
| 5. Advanced Features | 3 tasks | Sequential | 1.5 hours |
| 6. State Machine Tests | 1 task | All APIs done | 45 min |
| 7. End-to-End Tests | 1 task | All APIs done | 45 min |
| 8. Frontend | 2 tasks | All backend done | 4-5 hours |
| 9. Final Review | 3 tasks | All complete | 3 hours |

**Total Backend:** ~9 hours  
**Total Frontend:** ~4-5 hours  
**Total Testing:** ~2.5 hours  
**Total Review:** ~3 hours  
**Grand Total:** ~18-20 hours (rough estimate)

---

## Implementation Order (Recommended)

1. **Task 1.1** — Maven setup (enables all other tasks)
2. **Task 2.1-2.4** — Data layer (needed by services)
3. **Task 3.1-3.6** — Business logic (services)
4. **Task 4.1-4.3** — REST API (expose services)
5. **Task 5.1-5.3** — Advanced features (complete API)
6. **Task 6.1** — State machine integration tests (critical path)
7. **Task 7.1** — Full API scenario tests (confidence)
8. **Task 8.1-8.2** — Frontend (depends on stable API)
9. **Task 9.1-9.3** — Integration, review, docs

---

## What to Test at Each Stage

**After Task 2:** Database connectivity, entity creation  
**After Task 3:** Services can create, update, query tickets (unit tests)  
**After Task 4:** API endpoints return correct HTTP status and payloads (integration tests)  
**After Task 5:** Search, filter, pagination work end-to-end  
**After Task 6:** All state transitions validated  
**After Task 7:** Complete workflows pass (create → edit → transition → comment)  
**After Task 8:** Frontend pages load data from API  
**After Task 9:** Full stack workflows work, docs complete
