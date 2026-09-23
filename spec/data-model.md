# Data Model

## Entities

### Ticket

```java
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    @Size(min = 1, max = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Column(length = 100)
    @Size(max = 100)
    private String assignee;
    
    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
```

### Comment

```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    @Size(min = 1, max = 2000)
    private String text;
    
    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
```

### Enums

```java
public enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
}

public enum Priority {
    HIGH, MEDIUM, LOW
}
```

## Database Schema

### tickets table

```sql
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL,
    assignee VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_status ON tickets(status);
CREATE INDEX idx_created_at ON tickets(created_at DESC);
```

### comments table

```sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ticket_id ON comments(ticket_id);
```

## Field Constraints

### Ticket

| Field | Type | Constraints | Required |
|-------|------|-----------|----------|
| id | UUID | Auto-generated | Yes |
| title | String | 1-200 chars, non-blank | Yes |
| description | String | 1-2000 chars, non-blank | Yes |
| status | Enum | OPEN\|IN_PROGRESS\|RESOLVED\|CLOSED\|CANCELLED | Yes |
| priority | Enum | HIGH\|MEDIUM\|LOW | Yes |
| assignee | String | 0-100 chars | No |
| createdAt | OffsetDateTime | UTC, immutable | Yes |
| updatedAt | OffsetDateTime | UTC | Yes |

### Comment

| Field | Type | Constraints | Required |
|-------|------|-----------|----------|
| id | UUID | Auto-generated | Yes |
| ticketId | UUID | FK to Ticket | Yes |
| text | String | 1-2000 chars, non-blank | Yes |
| createdAt | OffsetDateTime | UTC, immutable | Yes |

## Data Transfer Objects

### Request DTOs

```java
public record CreateTicketRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull Priority priority,
    String assignee
) { }

public record UpdateTicketRequest(
    String title,
    String description,
    Priority priority,
    String assignee
) { }

public record UpdateStatusRequest(
    @NotNull TicketStatus status
) { }

public record AddCommentRequest(
    @NotBlank String text
) { }
```

### Response DTOs

```java
public record TicketResponse(
    String id,
    String title,
    String description,
    TicketStatus status,
    Priority priority,
    String assignee,
    List<CommentResponse> comments,
    String createdAt,
    String updatedAt
) { }

public record CommentResponse(
    String id,
    String text,
    String createdAt
) { }

public record ListTicketsResponse(
    List<TicketResponse> data,
    int total,
    int page,
    int pageSize
) { }
```

## Relationships

- One Ticket has many Comments (1:N)
- Deleting a ticket cascades to delete comments
- Comments are ordered by creation time (oldest first)

## Timestamps

- All timestamps stored as `OffsetDateTime` in UTC
- Never use local time
- JSON serialization: ISO 8601 format (e.g., `2026-09-22T10:30:45Z`)
- Immutable creation timestamp, mutable update timestamp
