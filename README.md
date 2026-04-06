# Finance Data Processing and Access Control Backend

**Submitted by:** Naziya Banu  
**Assignment:** Zorvyn Finance Backend Intern  
**Email:** nazinaziyabanu@gmail.com

---

## What is this project?

This is a backend REST API I built for a Finance Dashboard system. It lets three types of users — Admin, Analyst, and Viewer — access and manage financial data based on what they're allowed to do.

Basically:
- **Admin** can do everything. Create users, manage transactions, view all data.
- **Analyst** can add and edit transactions but can't delete them or manage users.
- **Viewer** can only view things. No changes allowed.

There's also a dashboard that shows things like total income, total expenses, net balance, and month-by-month trends — the kind of data a frontend chart would display.

---

## Why these technologies?

I used **Java with Spring Boot** because Java is what I've been learning and Spring Boot is the most commonly used framework for building REST APIs in Java. It handles a lot of the boilerplate for you — setting up the server, connecting to the database, handling security — so you can focus on actual business logic.

For **authentication**, I went with JWT (JSON Web Tokens). The idea is simple: when you log in, the server gives you a signed token. You send that token with every request after that. The server just checks the signature — it doesn't need to store any session. This is stateless and scales well.

For the **database**, I used H2 which is an embedded database that runs inside the app itself. No installation needed. Data is saved to a file (`finance_db.mv.db`) so it persists between restarts. In a real project I'd use PostgreSQL, but H2 keeps the setup simple for an assignment.

One thing I want to mention — I used **BigDecimal** for storing money amounts instead of `double`. This is important because floating point numbers can't represent certain decimal values precisely (`0.1 + 0.2 = 0.30000000000000004` in many languages), which is obviously not acceptable when you're dealing with money.

---

## How the code is organized

I followed a standard layered architecture that most backend projects use:

```
HTTP Request
     ↓
Controller   — receives the request, validates input, sends back the response
     ↓
Service      — this is where all the logic happens (business rules, calculations)
     ↓
Repository   — talks to the database (fetch, save, update)
     ↓
Database
```

Each layer has one responsibility. This makes it much easier to find bugs, make changes, and understand what's happening.

```
finance-backend/
├── pom.xml                                  ← lists all dependencies (like package.json in Node)
├── README.md
│
└── src/main/java/com/finance/backend/
    ├── FinanceBackendApplication.java        ← main class, entry point
    │
    ├── config/
    │   ├── SecurityConfig.java              ← who can access which URLs
    │   └── DataSeeder.java                  ← seeds default users + sample data on first run
    │
    ├── controller/
    │   ├── AuthController.java              ← /api/auth endpoints
    │   ├── UserController.java              ← /api/users endpoints
    │   ├── TransactionController.java       ← /api/transactions endpoints
    │   ├── DashboardController.java         ← /api/dashboard endpoints
    │   └── HealthController.java            ← /health (quick check if server is up)
    │
    ├── service/
    │   ├── AuthService.java
    │   ├── UserService.java
    │   ├── TransactionService.java
    │   └── DashboardService.java
    │
    ├── repository/
    │   ├── UserRepository.java
    │   └── TransactionRepository.java
    │
    ├── model/                               ← these map directly to database tables
    │   ├── User.java
    │   ├── Transaction.java
    │   ├── Role.java                        ← enum: ADMIN, ANALYST, VIEWER
    │   └── TransactionType.java             ← enum: INCOME, EXPENSE
    │
    ├── dto/                                 ← shapes of request/response data
    │   ├── ApiResponse.java                 ← all responses go through this
    │   ├── LoginRequest.java / LoginResponse.java
    │   ├── RegisterRequest.java
    │   ├── UserDto.java / UserUpdateRequest.java
    │   ├── TransactionRequest.java / TransactionDto.java
    │   ├── DashboardSummaryDto.java
    │   ├── CategoryTotalDto.java
    │   └── MonthlyTrendDto.java
    │
    ├── security/
    │   ├── JwtUtil.java                     ← creates and validates JWT tokens
    │   ├── JwtAuthFilter.java               ← runs before every request, checks the token
    │   └── UserDetailsServiceImpl.java      ← loads user from DB for Spring Security
    │
    └── exception/
        ├── AppException.java                ← custom exception that carries an HTTP status
        └── GlobalExceptionHandler.java      ← catches all errors, returns clean JSON

└── src/main/resources/
    └── application.properties               ← port, DB config, JWT secret
```

---

## Database tables

### users

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | auto-generated |
| username | TEXT | unique |
| email | TEXT | used for login, unique |
| password_hash | TEXT | BCrypt hashed, never plain text |
| role | TEXT | ADMIN / ANALYST / VIEWER |
| status | TEXT | ACTIVE or INACTIVE |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### transactions

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | auto-generated |
| amount | DECIMAL | e.g. 5000.00 |
| type | TEXT | INCOME or EXPENSE |
| category | TEXT | Salary, Rent, Food, etc. |
| date | DATE | |
| notes | TEXT | optional |
| user_id | INTEGER | who created it (FK to users) |
| deleted | BOOLEAN | soft delete flag |
| created_at | DATETIME | |
| updated_at | DATETIME | |

I used **soft delete** for transactions — instead of actually removing a record, I just set `deleted = true`. The record stays in the database but won't show up in any queries. This keeps the history intact, which matters in finance.

---

## How login works

```
1. You send POST /api/auth/login with email + password
2. Server finds your account and checks the password against the BCrypt hash
3. If it matches → server creates a JWT token (signed with a secret key) and sends it back
4. You save that token and include it in every future request:
   Authorization: Bearer <your_token>
5. For each request, the JwtAuthFilter runs first, reads the token, verifies the signature
6. If valid → the request goes through. If expired or tampered → 401.
```

The token contains your user ID, role, and email inside it (encoded, not encrypted). The server doesn't store sessions anywhere — it just trusts the token's signature.

---

## Role permissions

| Action             | Viewer (Read-only access) | Analyst (Limited access) | Admin (Full access)         |
| ------------------ | ------------------------- | ------------------------ | --------------------------- |
| Login              | Can log in                | Can log in               | Can log in                  |
| View own profile   | Can view profile          | Can view profile         | Can view profile            |
| View transactions  | Can view transactions     | Can view transactions    | Can view transactions       |
| View dashboard     | Can view dashboard        | Can view dashboard       | Can view dashboard          |
| Create transaction | Not allowed               | Can create transactions  | Can create transactions     |
| Edit transaction   | Not allowed               | Can edit transactions    | Can edit transactions       |
| Delete transaction | Not allowed               | Not allowed              | Can delete transactions     |
| View all users     | Not allowed               | Not allowed              | Can view all users          |
| Create/edit users  | Not allowed               | Not allowed              | Can create and manage users |

This is enforced in two places at the URL level in `SecurityConfig.java`, and at the business logic level in the service layer for more specific rules (like: an admin can't deactivate their own account).

---

## Setup and running

You'll need Java 17 and Maven installed. To check:
```bash
java -version
mvn -version
```

Then from inside the `finance-backend` folder:
```bash
mvn spring-boot:run
```

First run takes 2-3 minutes to download dependencies. After that it starts in a few seconds.

To confirm it's working, open: **http://localhost:3000/health**

You should see:
```json
{
  "status": "UP",
  "message": "Finance API is running",
  "version": "1.0.0"
}
```

### Default accounts

The app creates these automatically on the first run (via `DataSeeder.java`) along with 12 sample transactions:

| Email | Password | Role |
|---|---|---|
| admin@finance.com | admin123 | ADMIN |
| alice@finance.com | alice123 | ANALYST |
| bob@finance.com | bob12345 | VIEWER |

### H2 database browser

You can view the actual database tables at **http://localhost:3000/h2-console**  
JDBC URL: `jdbc:h2:file:./finance_db` | Username: `sa` | Password: *(blank)*

---

## API Reference

All responses follow this format:
```json
{ "success": true, "message": "...", "data": { ... } }
```

After logging in, add this to every request header:
```
Authorization: Bearer <token>
```

---

### Auth

**POST /api/auth/login** — no token needed

```json
{ "email": "admin@finance.com", "password": "admin123" }
```

Response:
```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

Note: wrong email and wrong password both return the same message ("Invalid email or password"). This is intentional — if we said "email not found", anyone could use that to figure out who has accounts in the system.

---

**POST /api/auth/register** — Admin only

```json
{
  "username": "john_doe",
  "email": "john@finance.com",
  "password": "john1234",
  "role": "ANALYST"
}
```
`role` defaults to `VIEWER` if not provided.

---

**GET /api/auth/profile** — any logged-in user

Returns your own profile info.

---

### Users — Admin only

**GET /api/users**

Optional filters: `?role=ADMIN`, `?status=ACTIVE`, `?search=alice`, `?page=0&size=10`

**GET /api/users/{id}**

**PUT /api/users/{id}**

All fields are optional — only send what you want to change:
```json
{
  "username": "new_name",
  "role": "ANALYST",
  "status": "INACTIVE"
}
```

**DELETE /api/users/{id}**

Sets the user's status to INACTIVE. Doesn't actually delete the record.

---

### Transactions

**POST /api/transactions** — Admin or Analyst

```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-05",
  "notes": "April salary"
}
```

**GET /api/transactions** — all roles

Lots of optional filters:
```
?type=INCOME or EXPENSE
?category=Salary
?startDate=2026-01-01
?endDate=2026-04-30
?minAmount=1000
?maxAmount=50000
?page=0&size=10
?sortBy=date&sortDir=desc
```

**GET /api/transactions/{id}** — all roles

**PUT /api/transactions/{id}** — Admin or Analyst

**DELETE /api/transactions/{id}** — Admin only (soft delete)

---

### Dashboard — all roles

**GET /api/dashboard/summary** — `?startDate=...&endDate=...` optional

```json
{
  "totalIncome": 157000.00,
  "totalExpenses": 49000.00,
  "netBalance": 108000.00,
  "transactionCount": 12,
  "incomeCount": 5,
  "expenseCount": 7
}
```

**GET /api/dashboard/categories** — totals grouped by category and type

**GET /api/dashboard/monthly-trends** — `?months=6` (default 12)

Shows income vs expenses per month — useful for rendering line/bar charts on the frontend.

**GET /api/dashboard/recent** — `?limit=5` (default 10)

---

## Error responses

```json
{ "success": false, "message": "What went wrong." }
```

For validation errors:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "Amount must be greater than 0",
    "email": "Please provide a valid email address"
  }
}
```

| Status | Meaning |
|-----|----|
| 200 | OK |
| 201 | Created |
| 400 | Bad request (wrong input) |
| 401 | Not logged in or token invalid |
| 403 | Logged in but not allowed |
| 404 | Not found |
| 409 | Conflict (email taken, etc.) |
| 500 | Something crashed on the server |

---

## Testing with Postman

**Step 1 — Login:**
- POST `http://localhost:3000/api/auth/login`
- Body → raw → JSON: `{ "email": "admin@finance.com", "password": "admin123" }`
- Copy the token from the response

**Step 2 — Use the token:**
- In every other request, go to the Authorization tab → Bearer Token → paste the token

**Test the role restrictions:**

Login as Alice (Analyst):
- `GET /api/transactions` → works
- `POST /api/transactions` → works
- `DELETE /api/transactions/1` → should get **403**
- `GET /api/users` → should get **403**

Login as Bob (Viewer):
- `GET /api/dashboard/summary` → works
- `POST /api/transactions` → should get **403**

---

## Decisions I made

A few things weren't specified in the assignment so I made my own calls:

**User creation is admin-only.** No self-registration. In a real finance system, you wouldn't want just anyone signing up — access needs to be granted by someone with authority.

**Soft deletes everywhere.** Neither users nor transactions ever get permanently removed. You can't really "undo" a DELETE in a financial context, so I chose to keep the data and just mark it as deleted/inactive.

**JWT expires in 7 days.** Shorter is more secure but means more frequent logins. 7 days felt like a reasonable tradeoff for an internal tool.

**PUT works like PATCH.** If you only send one field in an update request, only that field changes. I prefer this over requiring the full object on every update.

---

## What I found challenging

Honestly, Spring Security was the hardest part. Understanding how it intercepts requests, where the JWT filter fits into the chain, and why certain things need to be done in a specific order and that took the most time. I had to read the documentation carefully and experiment a lot.

JPA Specifications for dynamic filtering (where any combination of filters might be applied) was also new to me. The pattern of building predicates programmatically is a bit different from just writing SQL.

---

## What I'd add with more time

- Unit tests with JUnit and MockMvc — I know this matters a lot in production
- Swagger/OpenAPI so the endpoints are browsable at `/swagger-ui`
- An audit log table (who changed what and when)
- Rate limiting
- Password reset via email
- Swap H2 for PostgreSQL and add a Docker setup

---

*I genuinely enjoyed building this. It's the first full backend I've built from scratch with proper security and role management. Happy to walk through any part of the code if that would be helpful.*

**— Naziya Banu**
