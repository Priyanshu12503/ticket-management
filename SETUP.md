# Support Ticket Management System - Setup & Deployment Guide

## Project Overview

This is a complete full-stack support ticket management system with:
- **Backend:** Spring Boot 3.3.0 REST API (Java 21)
- **Frontend:** React 18 SPA
- **Database:** PostgreSQL (production), H2 (testing)
- **Build:** Maven for backend, npm for frontend

## Prerequisites

- Java 21 or later
- Maven 3.8.0+
- Node.js 16+
- npm or yarn
- PostgreSQL 13+ (optional, use H2 for local dev)
- Git

## Quick Start (Local Development)

### 1. Backend Setup

```bash
cd /path/to/ticket-management-V2

# Install dependencies and build
mvn clean install

# Run backend (dev mode with H2)
mvn spring-boot:run
```

Backend will start on **http://localhost:8080**

### 2. Frontend Setup (in separate terminal)

```bash
cd /path/to/ticket-management-V2/frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend will open at **http://localhost:3000**

### Environment Configuration

#### Backend Configuration

**Development (H2 in-memory):**
- Default configuration uses H2 in-memory database
- No additional setup required
- Data is lost when application stops

**Production (PostgreSQL):**

1. Create `application-prod.yml` in `src/main/resources/`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ticket_management
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true

server:
  port: 8080
```

2. Set environment variables:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

3. Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

#### Frontend Configuration

Create `.env` in `frontend/` directory:

```env
# API Configuration
REACT_APP_API_URL=http://localhost:8080/api

# Optional: for production builds
REACT_APP_ENVIRONMENT=development
```

For production builds, update the URL:
```env
REACT_APP_API_URL=https://api.yourdomain.com/api
```

## Building for Production

### Backend

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/ticket-management-1.0.0.jar --spring.profiles.active=prod
```

### Frontend

```bash
# Build optimized production bundle
npm run build

# Output in: frontend/build/

# Test build locally
npx serve -s build
```

## Database Setup (PostgreSQL)

### Create Database

```bash
psql -U postgres

CREATE DATABASE ticket_management;
CREATE USER ticket_user WITH PASSWORD 'your_secure_password';
ALTER ROLE ticket_user SET client_encoding TO 'utf8';
ALTER ROLE ticket_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE ticket_user SET default_transaction_deferrable TO on;
ALTER ROLE ticket_user SET default_time_zone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE ticket_management TO ticket_user;
\c ticket_management
GRANT SCHEMA public TO ticket_user;
```

### Run Migrations

Flyway migrations run automatically on application startup. Migrations are in:
- `src/main/resources/db/migration/V1__Initial_schema.sql`

## Testing

### Backend Tests

```bash
# Run all tests with H2
mvn test

# Run specific test class
mvn test -Dtest=TicketServiceTest

# Run with coverage report
mvn test jacoco:report
# Coverage: target/site/jacoco/index.html
```

### Frontend Tests

```bash
cd frontend

# Run tests
npm test

# Run with coverage
npm test -- --coverage --watchAll=false
```

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

**Tickets:**
- `POST /tickets` — Create ticket (201)
- `GET /tickets` — List tickets (200)
- `GET /tickets/{id}` — Get ticket (200)
- `PATCH /tickets/{id}` — Update ticket (200)
- `PATCH /tickets/{id}/status` — Change status (200)
- `GET /tickets/search` — Search tickets (200)

**Comments:**
- `POST /tickets/{id}/comments` — Add comment (201)
- `GET /tickets/{id}/comments` — List comments (200)

### Query Parameters

**List Tickets:**
- `page` (default: 1) — Page number
- `pageSize` (default: 20, max: 100) — Results per page
- `sortBy` (default: createdAt) — Sort field
- `sortDir` (default: desc) — asc or desc
- `status` (optional) — Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)

**Search Tickets:**
- `keyword` (required) — Search term
- `page` (default: 1)
- `pageSize` (default: 20, max: 100)

## Deployment

### Docker (Optional)

Create `Dockerfile` in project root:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/ticket-management-1.0.0.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

Build and run:
```bash
docker build -t ticket-management:latest .
docker run -p 8080:8080 -e DB_USERNAME=user -e DB_PASSWORD=pass ticket-management:latest
```

### Environment Variables

**Backend:**
- `JAVA_OPTS` — JVM options (e.g., `-Xmx1g`)
- `SERVER_PORT` — Server port (default: 8080)
- `DB_USERNAME` — Database user (production)
- `DB_PASSWORD` — Database password (production)
- `SPRING_PROFILES_ACTIVE` — Active profile (dev/prod)

**Frontend:**
- `REACT_APP_API_URL` — Backend API URL
- `PORT` — Dev server port (default: 3000)

## Troubleshooting

### Backend won't start

1. Check Java version:
   ```bash
   java -version
   # Should be 21+
   ```

2. Check port 8080 is available:
   ```bash
   # Linux/Mac
   lsof -i :8080
   
   # Windows
   netstat -ano | findstr :8080
   ```

3. Check Maven cache:
   ```bash
   mvn clean install -U
   ```

### Frontend won't connect to API

1. Check backend is running:
   ```bash
   curl http://localhost:8080/api/tickets
   ```

2. Check REACT_APP_API_URL in .env:
   ```bash
   cat frontend/.env
   # Should show: REACT_APP_API_URL=http://localhost:8080/api
   ```

3. Clear npm cache:
   ```bash
   npm cache clean --force
   cd frontend && npm install
   ```

### Database connection errors

1. Check PostgreSQL is running:
   ```bash
   psql -U postgres -c "SELECT 1"
   ```

2. Verify credentials in `application-prod.yml`

3. Check database exists:
   ```bash
   psql -U ticket_user -d ticket_management -c "SELECT 1"
   ```

## Development Workflow

### Adding a Feature

1. Create branch:
   ```bash
   git checkout -b feature/your-feature
   ```

2. Backend changes:
   - Implement in service layer
   - Add controller endpoint
   - Write tests

3. Frontend changes:
   - Update API service
   - Create/update components
   - Test with backend

4. Test:
   ```bash
   # Backend
   mvn test
   
   # Frontend
   npm test
   ```

5. Commit and push:
   ```bash
   git add .
   git commit -m "feat: your feature description"
   git push origin feature/your-feature
   ```

## Documentation Files

- **spec/requirements.md** — User requirements and acceptance criteria
- **spec/api-contract.md** — REST API specification
- **spec/architecture.md** — System design and architecture
- **spec/data-model.md** — Database schema and entities
- **spec/state-machine.md** — Ticket status workflow
- **spec/test-strategy.md** — Testing approach
- **docs/BACKEND_VERIFICATION.md** — Backend verification report
- **frontend/README.md** — Frontend setup and deployment
- **SETUP.md** — This file

## Support

For issues or questions:
1. Check documentation files (docs/, spec/)
2. Review test files for usage examples
3. Check API responses for detailed error messages

## License

Part of Support Ticket Management System MVP.


## Troubleshooting

### Backend Won't Start

**Error: Port 8080 already in use**
```bash
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or change port in application.properties
# server.port=8081
```

**Error: "An unexpected error occurred" on frontend**
1. Verify backend is running: http://localhost:8080/api/health
2. Check console for stack traces
3. Ensure H2 database initialized (check logs for "Tomcat started")

### Frontend Can't Connect to Backend

**Browser console shows CORS errors**
- Ensure `CorsConfig.java` is in place
- Restart backend
- Clear browser cache (Ctrl+Shift+Delete)

**Backend health check fails**
```bash
# Test connection
curl http://localhost:8080/api/health

# If fails, backend is not running
```

### Database Issues

**H2 Console (http://localhost:8080/h2-console)**
- Connection URL: `jdbc:h2:mem:ticket_management`
- Username: `sa`
- Password: (leave blank)

**Switch to PostgreSQL**
1. Install PostgreSQL locally
2. Create database: `createdb ticket_management`
3. Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_management
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   ```
4. Restart backend

## API Documentation

All API endpoints are documented in `docs/API.md`

**Quick Test:**
```bash
# Get all tickets
curl http://localhost:8080/api/tickets

# Create a ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Ticket",
    "description": "Test Description",
    "priority": "HIGH"
  }'
```

## Common Tasks

### Run Tests
```bash
mvn test
```

### Build for Production
```bash
mvn clean package
java -jar target/ticket-management-V2-1.0.0.jar
```

### View H2 Database Console
```
http://localhost:8080/h2-console
```

### Reset Database (H2)
Database is in-memory and resets on application restart.

### Deploy to Production
See `DEPLOYMENT.md` for detailed instructions.

