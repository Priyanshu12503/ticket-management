# Java Spring Boot Development Standards

## Project Setup

This project uses:
- **Java 21** with Spring Boot 3.x
- **Maven** for dependency management
- **PostgreSQL** for production; **H2** for testing
- **Spring Data JPA** for ORM
- **Spring MVC** for REST APIs
- **Jakarta EE** (Jakarta Bean Validation, Jakarta Persistence)

## Code Organization

```
src/main/java/com/ticketmanagement/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Database access (Spring Data JPA)
├── model/           # JPA entities
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions
└── config/          # Spring configuration

src/main/resources/
├── application.yml  # Application configuration
└── db/migration/    # Flyway migrations (if used)

src/test/java/      # Unit and integration tests
```

## Naming Conventions

- **Classes**: PascalCase (e.g., `TicketController`, `TicketService`)
- **Methods**: camelCase (e.g., `createTicket()`, `updateStatus()`)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase (e.g., `com.ticketmanagement.controller`)

## Spring Boot Patterns

### Controllers
- Use `@RestController` and `@RequestMapping`
- Return `ResponseEntity<T>` for flexible HTTP responses
- Use `@Valid` for input validation
- Prefix with `/api/` in base paths

### Services
- `@Service` annotated business logic
- Transactional operations use `@Transactional`
- Throw meaningful exceptions, not null returns

### Repositories
- Extend `JpaRepository<Entity, ID>`
- Use `@Repository` (optional, implied by JpaRepository)
- Add custom query methods with `@Query` when needed

### Entities
- Use `@Entity` and `@Table`
- Include `@Id` and generation strategy
- Use `@Column` for field mappings
- Include `@CreationTimestamp` and `@UpdateTimestamp` from Hibernate
- Use `@Enumerated(EnumType.STRING)` for enums

### DTOs
- Separate models for request/response
- Use records or classes with `@Getter`, `@Setter` from Lombok
- Immutable when possible

## Validation

- Use **Jakarta Bean Validation** annotations: `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, etc.
- Custom validators via `@Validated` at class level or method parameter
- Validation errors automatically mapped to HTTP 400

## Error Handling

- Create custom exceptions extending `RuntimeException`
- Use `@RestControllerAdvice` for global exception handling
- Return structured error responses with:
  - `status` (HTTP status code)
  - `message` (user-friendly message)
  - `details` (technical details if applicable)

## Database

- Use JPA `@Entity` for model mapping
- Leverage Hibernate features: `@CreationTimestamp`, `@UpdateTimestamp`
- Use `@Enumerated(EnumType.STRING)` for status fields
- Add indexes for frequently queried columns via `@Index`

## Configuration

- Use `application.yml` (YAML preferred over properties)
- Environment-specific configs: `application-dev.yml`, `application-prod.yml`
- Profiles: `@Profile("dev")`, `@Profile("prod")`

## Testing Strategy

- Unit tests with **JUnit 5** and **Mockito**
- Integration tests with `@SpringBootTest` and `@DataJpaTest`
- Use H2 database for tests (configured in `application-test.yml`)
- Test naming: `test<Method><Scenario>` (e.g., `testCreateTicketWithValidData()`)

## Build and Run

```bash
# Build
mvn clean package

# Run tests
mvn test

# Run application
mvn spring-boot:run

# With specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## Dependencies to Avoid

- Don't mix JAX-RS with Spring MVC
- Don't use legacy `javax.*` packages (use `jakarta.*`)
- Avoid mixing JPA with raw JDBC unless absolutely necessary
