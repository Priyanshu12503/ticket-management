# Environment Configuration Guide

This guide explains how to configure the Support Ticket Management System for different environments using environment variables.

## 🔧 Quick Setup

### 1. Choose Your Environment

**For Development (H2 Database):**
```bash
# Copy the development template
cp .env.development .env
```

**For Production (PostgreSQL):**
```bash
# Copy the production template  
cp .env.production .env
# Edit .env with your actual database credentials
```

### 2. Edit Configuration
Open `.env` and update with your values:
```bash
# Example for PostgreSQL
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password
```

### 3. Run Application
```bash
# The application automatically reads .env file
mvn spring-boot:run
```

## 🗃️ Database Options

### Option 1: H2 Database (Default)
✅ **Best for:** Development, testing, demos  
✅ **Pros:** No setup required, data persists locally  
✅ **Configuration:** Use defaults (no .env needed)

```bash
# No configuration needed - just run:
mvn spring-boot:run

# Access H2 console: http://localhost:8080/h2-console
# Connection: jdbc:h2:file:./data/ticket_management
# Username: sa
# Password: (leave blank)
```

### Option 2: PostgreSQL Database  
✅ **Best for:** Production, team development  
✅ **Pros:** Production-grade, concurrent access  
✅ **Setup Required:** PostgreSQL server + database

```bash
# 1. Install PostgreSQL
# 2. Create database
createdb ticket_management

# 3. Configure environment
cp .env.production .env
# Edit DB_USERNAME, DB_PASSWORD in .env

# 4. Run application  
mvn spring-boot:run
```

## 📝 Environment Variables Reference

### Database Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | H2 file | Database connection URL |
| `DB_DRIVER` | H2Driver | JDBC driver class |
| `DB_USERNAME` | sa | Database username |
| `DB_PASSWORD` | (empty) | Database password |
| `JPA_DIALECT` | H2Dialect | Hibernate dialect |

### JPA/Hibernate Settings

| Variable | Default | Description |
|----------|---------|-------------|
| `JPA_DDL_AUTO` | update | Schema management: update/validate/create-drop |
| `JPA_SHOW_SQL` | false | Log SQL queries |

### Security Settings

| Variable | Default | Description |
|----------|---------|-------------|
| `H2_CONSOLE_ENABLED` | true | Enable H2 web console (DISABLE in prod) |

### Performance Settings

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_POOL_MAX_SIZE` | 10 | Maximum database connections |
| `DB_POOL_MIN_IDLE` | 2 | Minimum idle connections |

### Application Settings

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | HTTP server port |
| `LOG_LEVEL_APP` | DEBUG | Application logging level |

## 🔒 Security Best Practices

### 1. Protect Secrets
```bash
# ✅ DO: Use environment variables
DB_PASSWORD=secure_password

# ❌ DON'T: Hardcode in application.properties  
# spring.datasource.password=secret123
```

### 2. Environment-Specific Settings
```bash
# Development
JPA_SHOW_SQL=true           # Debug SQL queries
H2_CONSOLE_ENABLED=true     # Enable web console

# Production  
JPA_SHOW_SQL=false          # Don't log SQL in production
H2_CONSOLE_ENABLED=false    # Disable for security
LOG_LEVEL_APP=INFO          # Reduce log verbosity
```

### 3. Database Security
```bash
# Use dedicated database user (not postgres superuser)
DB_USERNAME=ticket_app      # Limited permissions
DB_PASSWORD=complex_random_password

# Use connection pooling limits
DB_POOL_MAX_SIZE=20         # Prevent connection exhaustion
```

## 🚀 Environment Examples

### Local Development
```bash
# .env (or no file - uses defaults)
# Uses H2 database, full logging, H2 console enabled
```

### Team Development (Shared PostgreSQL)
```bash
# .env
DB_URL=jdbc:postgresql://dev-db-server:5432/ticket_management_dev
DB_USERNAME=dev_user  
DB_PASSWORD=dev_password
JPA_DDL_AUTO=update
H2_CONSOLE_ENABLED=false
```

### Production
```bash
# .env (or environment variables in deployment)
DB_URL=jdbc:postgresql://prod-db-server:5432/ticket_management
DB_USERNAME=ticket_app
DB_PASSWORD=${PROD_DB_PASSWORD}  # Injected by deployment system
JPA_DDL_AUTO=validate            # Don't auto-modify prod schema
JPA_SHOW_SQL=false               # No SQL logging in prod
H2_CONSOLE_ENABLED=false         # Security: disable dev tools
FLYWAY_ENABLED=true              # Use migrations for schema changes
LOG_LEVEL_APP=INFO               # Production logging level
```

### Docker Deployment
```bash
# docker-compose.yml or Kubernetes ConfigMap
DB_URL=jdbc:postgresql://postgres-service:5432/ticket_management
DB_USERNAME=ticket_user
DB_PASSWORD=${POSTGRES_PASSWORD}  # From secrets
SERVER_PORT=8080
```

## 🛠️ Troubleshooting

### Connection Issues
```bash
# Test database connection
# For PostgreSQL:
psql -h localhost -p 5432 -U your_username -d ticket_management

# Check application.properties is reading .env
# Look for: "Loaded environment variables" in startup logs
```

### Schema Issues  
```bash
# Reset H2 database (deletes all data)
rm -rf ./data/

# Reset PostgreSQL database
psql -c "DROP DATABASE ticket_management; CREATE DATABASE ticket_management;"
```

### Environment Loading Issues
```bash
# Verify .env file exists and has correct permissions
ls -la .env

# Check for syntax errors (no spaces around =)
# ✅ CORRECT: DB_PASSWORD=secret
# ❌ WRONG:   DB_PASSWORD = secret
```

## 📚 Additional Resources

- **PostgreSQL Setup:** [Official PostgreSQL Installation](https://www.postgresql.org/download/)
- **H2 Database:** [H2 Documentation](http://www.h2database.com/html/main.html)
- **Spring Boot Config:** [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- **Environment Variables:** [12-Factor App Config](https://12factor.net/config)