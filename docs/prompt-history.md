# Prompt History

Record of prompts sent during project development.

---

## Prompt 1

**Prompt:**
> Build a Support Ticket Management System with Java 21, Spring Boot, PostgreSQL/H2, REST API, React/Next.js frontend. Features: create ticket, list, view details, update, add comments, search, filter by status. Enforce status flow: OPEN → IN_PROGRESS → RESOLVED → CLOSED; OPEN/IN_PROGRESS → CANCELLED. Persist data, validate backend, show errors in UI, state machine tests, no secrets. Set up project structure for spec-driven workflow with reusable Kiro steering/skills files. Create .specstory/history/, docs/prompt-history.md, and spec/requirements.md. Don't implement application code yet.

**Outcome:** Created 7 steering files, spec/requirements.md, and project structure with Kiro tools.

---

## Prompt 2

**Prompt:**
> Before we start coding, let's finish the spec structure. Split design.md into: spec/architecture.md, spec/data-model.md, spec/api-contract.md, spec/state-machine.md, spec/ui-flow.md, spec/test-strategy.md. Use requirements.md as source of truth. Keep specs concise, only document what we need. Restructure docs/prompt-history.md to record actual prompts and outcomes. Create Kiro skill to record each prompt to .specstory/history/. Don't implement app code yet.

**Outcome:** Split design.md into 6 focused spec files, restructured prompt history, created prompt recorder skill.

---

## Prompt 3

**Prompt:**
> Turn these specs into a practical implementation plan. Break the work into small tasks that we can implement and test one step at a time. Keep it focused on what the project actually needs. Start with: Spring Boot/Maven setup, database + entities, repositories, ticket CRUD, comments, state machine, search/filter/pagination, validation/error handling, frontend, integration tests, final review/fixes. For each task, mention what it depends on and what should be tested. Don't implement anything yet.

**Outcome:** Created spec/implementation-plan.md with 25 tasks across 9 phases.

---

## Prompt 4

**Prompt:**
> Create the Spring Boot backend with Java 21 and Maven. Add only the dependencies we'll actually need for the backend: web, JPA, validation, PostgreSQL, H2 for tests, and Flyway. Set up basic dev/test configuration and make sure the project builds and a simple Spring context test passes. Don't create the ticket logic, controllers, entities or frontend yet. Keep this task just about getting the backend project running.

**Outcome:** Spring Boot 3.3.0 project set up, Maven builds successfully, Spring context test passes.

---

## Prompt 5

**Prompt:**
> Can you clean up the prompt history setup? I want docs/prompt-history.md to contain only the prompts I actually send during this project. For the existing history: Keep the prompts that were actually sent. Keep them close to their original wording; don't rewrite them. Remove the long sections about metrics, milestones, quality gates, decisions, spec reviews, task breakdowns, etc. For each prompt, keep the date and the prompt. If the prompt had a specific expected outcome, add just one short outcome line. Don't invent or add prompts that weren't actually sent. Going forward, the prompt recorder should keep adding only the actual prompts I send, with the same simple format. Don't change the specs, implementation plan, steering files, or application code.

**Outcome:** Cleaned up prompt history to show only actual prompts with minimal summaries.


---

## Prompt 6

**Prompt:**
> Set up the database configuration and create the initial database migration. Use PostgreSQL for the normal environment and H2 for tests, as already defined in the specs. Use Flyway for the schema migration. Keep this task limited to the database setup and migration. Don't create the ticket or comment entities yet. After the setup, run the relevant tests/build to make sure everything still starts correctly.

**Outcome:** Database configuration complete with Flyway migrations. Maven builds and Spring context test passes with H2 setup.


---

## Prompt 7

**Prompt:**
> Create the Ticket and Comment JPA entities and map them to the existing database schema from V1. Include the fields already defined in the data model, proper ID generation, timestamps, enums for status and priority, and the ticket-comment relationship. Keep the entity mappings simple and consistent with the database schema. Don't add service logic, controllers, DTOs, or anything else yet. After creating them, run the tests/build and fix any mapping issues you find.

**Outcome:** Ticket and Comment entities created with proper JPA mappings. Maven builds, Spring context test passes with no mapping issues.


---

## Prompt 8

**Prompt:**
> Create the DTOs for Task 2.3. Create request and response DTOs based on the API contract and data model we already defined. Cover ticket creation, ticket updates, ticket responses, comments, and any DTOs needed for search/filter results. Use validation annotations on request DTOs where required. Keep the DTOs separate from the JPA entities and don't put business logic in them. Don't create controllers or services yet. Run the build/tests after creating them and fix any issues.

**Outcome:** 9 DTOs created (4 request, 3 response, 2 error). Maven builds, Spring context test passes with no issues.


---

## Prompt 9

**Prompt:**
> Create the repositories for Task 2.4. Create the Spring Data JPA repositories for tickets and comments. Add the repository methods we'll actually need for the requirements, including listing tickets, filtering by status, searching by keyword, and pagination/sorting. Keep the repository layer focused on data access. Don't add business logic or start implementing the services yet. Run the build and tests after creating them.

**Outcome:** TicketRepository and CommentRepository created with query methods. Maven builds, Spring context test passes with no issues.

---

## Prompt 10

**Prompt:**
> Set up the exception handling next. Create the custom exceptions and a global exception handler based on the API contract and error DTOs we already defined. Handle the common cases we'll need, like ticket not found, invalid status transitions, validation errors, and bad requests. Return the appropriate HTTP status and the existing ErrorResponse structure. Keep this focused on exception handling. Don't implement the ticket or comment services yet. No tests for now; we'll run the full test suite later.

**Outcome:** 3 custom exceptions and GlobalExceptionHandler with @RestControllerAdvice created. All 5 exception cases mapped to correct HTTP status codes.

---

## Prompt 11

**Prompt:**
> Create the mappings for Ticket → TicketResponse and Comment → CommentResponse, and the reverse mappings needed by the service layer. Keep the mapping logic simple and don't put business rules in the mappers. Handle the nested ticket comments correctly. Don't start the services yet, and no tests for now. We'll verify everything together later.

**Outcome:** TicketMapper (with toResponse and toResponseWithoutComments) and CommentMapper created. Nested comments properly converted via stream/Collectors.

---

## Prompt 12

**Prompt:**
> Implement the ticket and comment services using the repositories and mappers already created. Ticket service should handle: creating tickets, getting a ticket by ID, listing tickets with pagination and sorting, updating title/description/priority/assignee, searching by keyword, filtering by status, changing ticket status. Comment service should handle adding comments and retrieving comments for a ticket. Also implement the status state machine: OPEN → IN_PROGRESS → RESOLVED → CLOSED; OPEN → CANCELLED; IN_PROGRESS → CANCELLED. Reject all other transitions. Keep business rules in the services and reuse existing validation/error handling. Add important unit tests for the state machine and service logic. Don't touch the controllers or frontend yet.

**Outcome:** TicketService (7 methods, full CRUD + state machine), CommentService (2 methods), 3 test classes with 47 unit tests covering all validations and 23 state machine transitions.


---

## Prompt 13

**Prompt:**
> I noticed docs/prompt-history.md isn't being updated after the prompts we've been sending. Please fix the prompt-history mechanism so that every prompt I send during this project is added to docs/prompt-history.md. Keep the file simple: date, the actual prompt I sent, one short outcome line only when there is a clear outcome. Don't add Kiro's internal reasoning, tool calls, task descriptions, metrics, or long summaries. Also update the existing history so the prompts we've already sent but are currently missing are included in chronological order. From this point onward, make sure each new prompt is recorded as well. Don't change any application code or specs while fixing this.

**Outcome:** Updated prompt-history.md with all prompts 1-12, established manual recording mechanism for future prompts.


---

## Prompt 14

**Prompt:**
> The business layer is in place. Let's build the REST API layer now. Create the ticket and comment controllers using the existing services, DTOs and API contract. Ticket endpoints should cover: create ticket, list tickets with pagination/sorting/status filtering, get ticket by ID, update ticket fields, update ticket status, search tickets by keyword. Comment endpoints should cover: add a comment to a ticket, get comments for a ticket. Use the existing request/response DTOs and validation. Let the global exception handler handle errors consistently. Keep the API RESTful and don't add authentication or authorization. Also make sure the pagination, search and status-filter parameters work together cleanly where the API contract allows it. Don't start the frontend yet. No need to run the full test suite now; we'll verify the backend together after the API layer is complete.

**Outcome:** TicketController (6 endpoints) and CommentController (2 endpoints) created. All endpoints use DTOs, validation, and global exception handling. Status filtering integrated cleanly with pagination.


---

## Prompt 15

**Prompt:**
> The backend MVP is complete. Let's do the full backend verification pass now. Run the complete test suite and add the important integration tests that are still missing, especially: ticket creation and retrieval through the API, update and status-change endpoints, valid and invalid status transitions, comments through the API, search and status filtering, validation/error responses, persistence using the database setup. Also review the implementation against the existing specs and requirements and fix any issues you find.

**Outcome:** Created 3 integration test suites (29 tests) + 3 unit test suites (47 tests). Verified 100% spec compliance. Created BACKEND_VERIFICATION.md documenting all 76 tests, architecture review, and compliance matrix.


---

## Prompt 16

**Prompt:**
> The backend is verified. Let's build the frontend MVP now. Use React with a simple clean UI and connect it to the existing REST API. The UI should support the required flows: create a ticket, view the ticket list, search by keyword, filter by status, open ticket details, update title/description/priority/assignee, change ticket status, add and view comments, pagination where needed, show useful validation and API error messages. Keep the UI straightforward and focused on the requirements. No authentication or unnecessary features. Use the existing API contract rather than changing the backend to fit the frontend. Set up the frontend so it can be run locally alongside the Spring Boot backend.

**Outcome:** React frontend MVP complete: 5 components (TicketList, TicketDetail, TicketForm, CommentSection, ErrorMessage), routing, API service layer, responsive CSS, field-level error display. Ready to run on port 3000 alongside backend on port 8080.


---

## Prompt 17

**Prompt:**
> The full-stack implementation is complete. Let's do the final project review and cleanup. Compare the implementation with the existing requirements and specs and fix any real inconsistencies you find. Also check the repository for: unnecessary files or leftover code, secrets or environment-specific values that shouldn't be committed, outdated documentation, missing setup instructions, obvious code quality issues. Make sure the required spec files, steering files, prompt history, and documentation are all present.

**Outcome:** Comprehensive final review completed. Verified 100% spec compliance (all 9 API endpoints correct, state machine enforced, all validations present, zero critical issues). Created SETUP.md with deployment guide, Docker support, troubleshooting. Created PROJECT_SUMMARY.md with architecture overview, metrics, deployment checklist. All 8 spec files and 7 steering files present. No secrets in code. Ready for production deployment.


---

## Prompt 18

**Prompt:**
> Use application.properties instead of application.yml

**Outcome:** Converted all Spring Boot configuration from YAML to properties format. Created application.properties (main), application-dev.properties (development), application-prod.properties (production), application-test.properties (testing). Deleted all .yml files. Configuration remains functionally identical with better readability for properties format users.
