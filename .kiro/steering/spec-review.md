# Spec Review Standards

## Purpose

Spec review ensures clarity, completeness, and feasibility before implementation. A well-written spec prevents rework and misalignment.

## Spec Structure

```
spec/
├── requirements.md        # User-facing requirements
├── design.md             # Technical design decisions
└── tasks.md              # Implementation tasks (generated during Spec workflow)
```

## Requirements.md Review Checklist

### Clarity
- [ ] Each requirement is written in plain language
- [ ] No ambiguous wording ("should", "might", "could")
- [ ] Acceptance criteria are specific and testable
- [ ] Example data provided where helpful

Good: "When a ticket is created, its status must be set to OPEN and the creation timestamp must be recorded."
Bad: "The ticket status should be initialized appropriately."

### Completeness
- [ ] All features listed in the goal are included
- [ ] Edge cases documented (empty searches, boundary values)
- [ ] Error scenarios described
- [ ] Data validation rules explicit
- [ ] Constraints stated (max length, required fields, etc.)

### Technical Feasibility
- [ ] Requirements match technology stack (Java 21, Spring Boot, PostgreSQL)
- [ ] No impossible asks (e.g., "instant response with 1M records")
- [ ] Dependencies clear (e.g., state machine rules)
- [ ] Performance expectations reasonable

### State Machine Specification

For ticket status transitions, the spec must define:
- [ ] All valid transitions explicitly listed
- [ ] All invalid transitions explicitly listed
- [ ] Entry and exit conditions
- [ ] Transitions triggered by (API call, user action, system event)
- [ ] Side effects (e.g., notifications, audit log)

Example:
```
Valid Transitions:
- OPEN → IN_PROGRESS (manual, any user)
- IN_PROGRESS → RESOLVED (manual, assigned user)
- RESOLVED → CLOSED (manual, any user)
- OPEN → CANCELLED (manual, any user)
- IN_PROGRESS → CANCELLED (manual, any user)

Invalid Transitions (must reject with error):
- CLOSED → anything
- RESOLVED → OPEN
- CANCELLED → anything except back to OPEN (if allowing reopen)
```

### API Contract
- [ ] All endpoints listed with HTTP method
- [ ] Request/response schemas documented
- [ ] Status codes and error cases clear
- [ ] Query parameters and filters described
- [ ] Pagination rules specified (if applicable)

### Data Model
- [ ] All entities described
- [ ] Fields with types and constraints
- [ ] Relationships defined
- [ ] Timestamps (creation, update)

## Design.md Review Checklist

### Architecture
- [ ] Component relationships clear
- [ ] Data flow described
- [ ] Layering justified (controller → service → repository)
- [ ] No circular dependencies mentioned

### Database Design
- [ ] Tables and columns defined
- [ ] Primary keys and foreign keys
- [ ] Indexes for performance
- [ ] Normalization decisions explained

### Error Handling Strategy
- [ ] Custom exceptions listed
- [ ] Global exception handler approach
- [ ] HTTP status code mapping
- [ ] Error response format

### State Machine Implementation
- [ ] Approach described (service method, state object, etc.)
- [ ] Validation centralized in one place
- [ ] Easy to add new transitions
- [ ] Invalid transitions prevented at service layer

### Testing Strategy
- [ ] Unit test scope defined
- [ ] Integration test scope defined
- [ ] State machine test scenarios listed
- [ ] Test data strategy (fixtures, builders)

## Common Issues to Catch

### Vague Requirements
```
❌ "Search should work"
✅ "Search endpoint accepts keyword parameter and searches title and description fields, case-insensitive"
```

### Missing Error Cases
```
❌ Only happy path tested
✅ Specify: "If assignee field is > 100 chars, return 400 with validation error"
```

### Incomplete State Machine
```
❌ "Ticket can be resolved"
✅ "OPEN → IN_PROGRESS ✓, IN_PROGRESS → RESOLVED ✓, RESOLVED → CLOSED ✓, OPEN → CANCELLED ✓, 
   IN_PROGRESS → CANCELLED ✓. All other transitions must throw InvalidStatusTransitionException."
```

### Missing Data Constraints
```
❌ "Ticket has a title"
✅ "Title must be 1-200 characters, non-blank"
```

### Unclear Testing Requirements
```
❌ "Test state machine"
✅ "Write state machine tests covering all 5 valid transitions and 
   at least 3 invalid transitions (CLOSED→OPEN, RESOLVED→OPEN, CANCELLED→OPEN)"
```

## Review Workflow

1. **Author**: Writes requirements.md and design.md
2. **Reviewer**: Checks against checklist
3. **Discussion**: Clarifies ambiguities, questions feasibility
4. **Revision**: Author updates spec
5. **Approval**: Marked ready for implementation
6. **Task Generation**: Spec task generator creates implementation tasks

## Sign-Off Criteria

Spec is ready for implementation when:
- [ ] All requirements are clear and testable
- [ ] State machine transitions fully specified (valid and invalid)
- [ ] API contract defined completely
- [ ] Data model documented
- [ ] Design decisions captured
- [ ] Team agrees implementation is feasible
- [ ] No blockers or unknowns remain

## Red Flags

Stop and clarify if:
- "This should work like the old system" (which system?)
- "Make it fast" (define acceptable latency)
- "Handle edge cases" (list them)
- "User-friendly" (how measured?)
- No mention of error cases
- State transitions assumed but not documented
- API response formats left to implementer

## Example Spec Review Notes

```markdown
### Requirements Review

✅ Clear: "Ticket creation requires title, description, and priority"
⚠️ Clarify: "Add comment" — can users only add their own comments?
✅ Complete: Validation rules specified for all fields
❌ Missing: What happens if ticket ID doesn't exist when updating?
   Response: 404 Not Found

### Design Review

✅ Good: Service layer handles all state machine logic
❌ Missing: Error response format. Should include field-level errors?
   Response: Yes, see api-standards.md for format

### State Machine

✅ Valid transitions clearly listed
✅ Invalid transitions with examples
❌ Missing: Can RESOLVED tickets be reopened?
   Response: No, only CLOSED → OPEN not allowed in initial spec
```
