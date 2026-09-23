# Documentation Standards

## Project Documentation Structure

```
docs/
├── API.md                    # API documentation (auto-generated reference)
├── ARCHITECTURE.md           # System design and architecture
├── DEVELOPMENT.md            # How to set up and run locally
├── DEPLOYMENT.md             # Production deployment guide
├── DATABASE.md               # Schema and data model
├── TESTING.md                # Testing strategy and coverage
└── TROUBLESHOOTING.md        # Common issues and solutions
```

## README.md

Root level README should include:
- Project description
- Quick start (clone, build, run)
- Key technologies and versions
- Project structure overview
- Contributing guidelines
- License

## API Documentation

### Location
- `docs/API.md` — Complete API reference
- OpenAPI/Swagger spec optional (auto-generate from code comments)

### Contents for Each Endpoint
1. **Method & Path**: `POST /api/tickets`
2. **Description**: What it does
3. **Request Body**: Schema with field descriptions
4. **Response**: Status codes and schemas
5. **Error Cases**: Possible errors and meanings
6. **Example**: cURL or JSON example

Example:
```markdown
### Create Ticket

Creates a new support ticket.

**Endpoint:** `POST /api/tickets`

**Request Body:**
```json
{
  "title": "Database connection failing",
  "description": "App crashes on startup",
  "priority": "HIGH",
  "assignee": "john@company.com"
}
```

**Response (201 Created):**
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Database connection failing",
  "status": "OPEN",
  ...
}
```

**Errors:**
- 400: Validation failed (see error details)
- 500: Server error
```

## Architecture Documentation

### ARCHITECTURE.md

Describe:
1. **High-level design**: Components and data flow
2. **Ticket State Machine**: Valid transitions and rules
3. **Database schema**: Entity relationships
4. **Error handling strategy**: How exceptions flow to clients
5. **Authentication/Authorization**: (None for MVP, but document this)

Example sections:
- System Overview
- Component Diagram (ASCII or reference to diagram file)
- Request Flow (example: Create Ticket flow)
- State Machine Diagram

## Code Comments

### JavaDoc

For public classes and methods:
```java
/**
 * Service for managing ticket operations.
 * Handles ticket creation, updates, and status transitions
 * with full state machine validation.
 */
@Service
public class TicketService {

    /**
     * Creates a new ticket with the given details.
     * Initial status is always OPEN.
     *
     * @param request the ticket creation request
     * @return the created ticket response
     * @throws IllegalArgumentException if request is invalid
     */
    public TicketResponse createTicket(CreateTicketRequest request) {
        // ...
    }
}
```

### Inline Comments

Use sparingly — code should be self-documenting. Comment:
- Complex business logic
- Non-obvious decisions
- Workarounds or hacks

```java
// State machine validation: only these transitions are allowed
if (!isValidTransition(currentStatus, newStatus)) {
    throw new InvalidStatusTransitionException(currentStatus, newStatus);
}
```

## Changelog

Maintain `CHANGELOG.md` with:
- Version number and date
- Breaking changes (bold)
- New features
- Bug fixes
- Known issues

Format:
```markdown
## [1.0.0] - 2026-09-22

### Added
- Initial release with ticket CRUD
- Status transition state machine
- Comment system
- Search and filtering

### Breaking Changes
- N/A (initial release)

### Known Issues
- None
```

## Development Guide

### DEVELOPMENT.md

Include:
1. Prerequisites (Java 21, Maven, Git, Docker optional)
2. Clone and setup
3. Building the project
4. Running tests
5. Running the application
6. IDE setup (VS Code, IntelliJ shortcuts)
7. Database setup
8. Common development tasks

Example:
```markdown
## Prerequisites
- Java 21 or later
- Maven 3.8.0+
- PostgreSQL 13+ (or use H2 for local development)

## Setup
1. Clone: git clone ...
2. Navigate: cd ticket-management-v2
3. Build: mvn clean install
4. Run: mvn spring-boot:run

## Running Tests
mvn test

## Database
- Local: H2 in-memory (default)
- Production: PostgreSQL connection string in application-prod.yml
```

## Deployment Guide

### DEPLOYMENT.md

Include:
1. Prerequisite environment setup
2. Configuration management (secrets, environment variables)
3. Database migration steps
4. Running the application in production
5. Health checks
6. Monitoring and logging
7. Rollback procedures

## Schema Documentation

### DATABASE.md

Include:
1. Entity Relationship Diagram (ASCII or reference)
2. Table descriptions
3. Column definitions
4. Indexes
5. Constraints

Example:
```markdown
## Tables

### tickets
- `id` (UUID, PK)
- `title` (VARCHAR 200, NOT NULL)
- `description` (TEXT, NOT NULL)
- `status` (ENUM: OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)
- `priority` (ENUM: HIGH, MEDIUM, LOW)
- `assignee` (VARCHAR 100, nullable)
- `created_at` (TIMESTAMP WITH TZ)
- `updated_at` (TIMESTAMP WITH TZ)

**Indexes:**
- status (for filtering)
- created_at (for sorting)

### comments
- `id` (UUID, PK)
- `ticket_id` (UUID, FK to tickets)
- `text` (TEXT, NOT NULL)
- `created_at` (TIMESTAMP WITH TZ)

**Indexes:**
- ticket_id (for joining)
```

## Version Control

### Commit Messages

Format:
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types: feat, fix, docs, style, refactor, test, chore
Scope: area of code (e.g., ticket-service, api, database)
Subject: Imperative, present tense, no period

Example:
```
feat(ticket-service): add status transition validation

Implement state machine validation to prevent invalid
ticket status transitions. Maintain audit trail of why
transitions were rejected.

Closes #42
```

## Documentation Standards

- Markdown format for all docs
- Clear headings and subheadings
- Code blocks with syntax highlighting
- Links to related docs
- Keep up to date with code changes
- No marketing fluff — be technical and precise
