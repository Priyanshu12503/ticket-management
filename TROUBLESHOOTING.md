# Troubleshooting Guide

## "An unexpected error occurred" on Frontend

### 1. Check if Backend is Running

**Option A: Test from browser**
```
Visit: http://localhost:8080/api/health
You should see: {"status":"UP","service":"Support Ticket Management System","version":"1.0.0"}
```

**Option B: Check console**
- Open browser DevTools (F12)
- Check the Console tab for error messages
- Look for network errors (Network tab)

### 2. Backend Not Responding

If the backend health check fails:

1. **Verify Spring Boot is running**
   - In your IDE, run the main class: `TicketManagementApplication`
   - You should see: `Tomcat started on port(s): 8080`

2. **Check for startup errors**
   - Look for stack traces in the IDE console
   - Common issues:
     - Schema validation errors → Already fixed with H2
     - Port 8080 already in use → Kill process or change port

3. **Verify H2 Database**
   - Backend should auto-create schema on startup
   - Access H2 console: http://localhost:8080/h2-console
   - Connection URL: `jdbc:h2:mem:ticket_management`
   - Username: `sa`
   - Password: (leave blank)

### 3. CORS Issues

If you see CORS errors in browser console:
- Solution is already deployed in `CorsConfig.java`
- Restart the backend for changes to take effect
- Clear browser cache (Ctrl+Shift+Delete)

### 4. Frontend Not Connecting

If frontend is running but can't reach backend:

1. **Check API URL**
   ```
   frontend/src/api/ticketService.js
   Line 3: const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api'
   ```

2. **Check if backend is accessible**
   ```bash
   # Windows PowerShell
   curl http://localhost:8080/api/health
   ```

3. **Restart frontend**
   - Stop `npm start`
   - Clear `frontend/node_modules/.cache`
   - Run `npm start` again

### 5. Database Schema Issues

If you see database errors:

1. **H2 auto-creates schema** with `spring.jpa.hibernate.ddl-auto=create-drop`
2. **For PostgreSQL** (if switching):
   - Update `application.properties`:
     ```
     spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_management
     spring.datasource.driver-class-name=org.postgresql.Driver
     spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
     ```
   - Create database: `createdb ticket_management`
   - Restart backend

### 6. Quick Start Checklist

- [ ] Backend running on port 8080
- [ ] Frontend running on port 3000
- [ ] Health check passes: http://localhost:8080/api/health
- [ ] Browser console has no CORS errors
- [ ] H2 console accessible: http://localhost:8080/h2-console

### 7. Logs to Check

**Backend (IDE Console)**
```
Looking for:
- "Tomcat started on port(s): 8080"
- "Spring Framework" version
- No error stack traces
```

**Frontend (Browser Console - F12)**
```
Looking for:
- Network requests to http://localhost:8080/api/tickets
- 200 OK responses (not 0 or error status)
- No CORS errors
```

## Still Not Working?

1. **Restart everything**
   - Stop backend
   - Stop frontend
   - Wait 5 seconds
   - Start backend first
   - Wait for "Tomcat started..." message
   - Start frontend

2. **Clear all caches**
   - Browser: Ctrl+Shift+Delete
   - Node: `cd frontend && rm -r node_modules && npm install`

3. **Check firewall**
   - Windows Defender may block ports
   - Add exceptions for ports 8080 and 3000

4. **Check if ports are in use**
   ```powershell
   netstat -ano | findstr :8080
   netstat -ano | findstr :3000
   ```
   - If something is using the ports, either kill it or change the port in application.properties

