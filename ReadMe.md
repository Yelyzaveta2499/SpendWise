# SpendWise

Simple Spring Boot web app for tracking personal and business expenses.

## Tech stack
- Java 21
- Spring Boot (web, security, data JPA)
- Thymeleaf + vanilla JS frontend
- MySQL database

## Prerequisites
- Java 21 or newer installed
- Maven installed and on PATH
- Local MySQL instance running
  - Default config (can be changed in `src/main/resources/application.properties`):
    - URL: `jdbc:mysql://localhost:3306/spendwisedb?createDatabaseIfNotExist=true`
    - Username: `root`
    - Password: `root`

## How to run
From the project root:

```powershell
mvn spring-boot:run
```

The app will start on:

- http://localhost:1111

If the port is busy, stop the other process or change `server.port` in `application.properties`.

## Login
Users are currently in-memory (configured in `SecurityConfig`) and mirrored in the database for ownership checks.

Example accounts:
- `indiv` / `password`  (individual user)
- `business` / `password`  (business user)
- `kid` / `password`  (kids account)

After successful login you are redirected to the main dashboard.

## US4 – Dashboard Financial Overview
The dashboard provides a quick financial summary without opening other pages.

What you can see:
- Total Balance (income − expenses)
- Income total for the selected period
- Expenses total for the selected period
- Savings Rate
- Income vs Expenses chart (last 6 months)
- Recent transactions

Behaviour:
- On load: totals + chart + recent list are fetched and displayed.
- Period selector: changing the time period refreshes the data.
- Empty state: if no financial data exists for the user/period, an empty state message is shown.

Backend API:
- `GET /api/dashboard/overview?period=this_month|last_month|last_30|this_year`

Tests:
- `DashboardServiceTest` (calculation logic)
- `DashboardControllerTest` (controller wiring + error handling)

## Database seeding
On startup:
- `data.sql` ensures roles (`INDIVIDUAL`, `BUSINESS`, `KIDS`) and users (`indiv`, `business`, `kid`) exist.
- `ExpenseDataInitializer` seeds a small set of **zero-amount** placeholder expenses for `indiv` *only if* that user has no expenses yet.

This means:
- First run: you see one clean list of example expenses for `indiv`.
- Later runs: real and placeholder expenses are preserved and not duplicated.

## Expenses screen
- Open **Expenses** from the sidebar after login.
- The table is loaded from the REST API: `GET /api/expenses` for the logged-in user.
- You can:
  - Search by text
  - Filter by category
  - Add a new expense using the **Add Expense** button

Add Expense dialog:
- **Name** – dropdown with predefined options (e.g. *Grocery Store*, *Monthly Salary*).
- **Category** – dropdown with existing categories (e.g. *Food & Dining*, *Income*).
- **Amount** – numeric input; backend validates for positive values.
- **Date** – required; defaults to today if not provided.

Submitted expenses are stored in the `expenses` table and immediately shown in the list.

## US3 – Manage Budgets by Category
- Open **Budgets** from the sidebar after login.
- Budgets are stored per **category + month + year** for the logged-in user.

API (REST):
- `GET /api/budgets` – list budgets for current user
- `POST /api/budgets` – create budget
- `PUT /api/budgets/{id}` – update budget
- `DELETE /api/budgets/{id}` – delete budget

Rules:
- `amount` must be > 0
- only one budget per `(category, month, year)` per user
- users can only update/delete their own budgets

Tests:
- `BudgetControllerTest`
- `BudgetServiceTest`

## US5 – Savings Goals Management
Track savings goals by defining target amounts and monitoring progress over time to see how close you are to reaching your financial targets.

### Features
- **Create Goals**: Define savings goals with name, target amount, deadline (optional), icon, and color
- **Track Progress**: Visual progress bars and percentage calculations show your progress
- **Add Contributions**: Record savings contributions with optional notes
- **Edit & Delete**: Modify goal details or remove goals using hover icons
- **Summary Dashboard**: View total saved, total target, and active goals count

### API Endpoints (REST)

**Goals Management:**
- `GET /api/goals` – List all goals for authenticated user
- `GET /api/goals/{id}` – Get a specific goal
- `POST /api/goals` – Create a new goal
  - Body: `{ name, targetAmount, deadline?, icon?, color? }`
- `PUT /api/goals/{id}` – Update a goal
- `DELETE /api/goals/{id}` – Delete a goal

**Contributions:**
- `POST /api/goals/{id}/contributions` – Add contribution
  - Body: `{ amount, note? }`
- `GET /api/goals/{id}/contributions` – List contributions for a goal

**Summary:**
- `GET /api/goals/summary` – Get statistics
  - Returns: `{ totalSaved, totalTarget, activeGoals }`

### Progress Calculations

```
Progress % = (currentAmount / targetAmount) × 100
Remaining = targetAmount - currentAmount
```

**Examples:**
- $0 of $10,000 → 0% progress, $10,000 remaining
- $2,500 of $10,000 → 25% progress, $7,500 remaining
- $10,000 of $10,000 → 100% progress, $0 remaining

**Contributions accumulate:**
- Add $500 → currentAmount increases by $500
- Multiple contributions: $1,000 + $500 + $750 = $2,250 total

### Validation Rules

**Goal Creation:**
- `name` is required (max 50 chars)
- `targetAmount` must be > 0
- `userId` auto-set from authenticated user
- `currentAmount` starts at 0.0

**Contributions:**
- `amount` must be > 0
- Each contribution creates separate record with timestamp

**Errors:**
- Invalid target → 400: "Target amount must be greater than zero"
- Empty name → 400: "Goal name is required"
- Invalid contribution → 400: "Contribution amount must be greater than zero"
- Goal not found → 400: "Goal not found"

### UI Components

**Create Goal Modal:**
- Goal Name, Target Amount, Deadline, Icon (10 options), Color (8 options)

**Edit Goal Modal:**
- Opens with current data pre-filled, edit any field

**Add Contribution Modal:**
- Amount + optional note (e.g., "Bonus from work")

**Goal Cards:**
- Show icon, name, percentage, deadline, progress bar
- Hover to see edit (✎) and delete (🗑) icons
- "Add Contribution" button

### Tests
- `GoalServiceTest` – 30+ tests covering service logic, progress calculations, validations
- `GoalControllerTest` – 15+ tests covering API endpoints, authentication, error handling

### Example Flow

```json
// Create goal
POST /api/goals
{ "name": "Emergency Fund", "targetAmount": 10000, "icon": "🛡️", "color": "#10b981" }

// Add contributions
POST /api/goals/1/contributions
{ "amount": 500, "note": "January savings" }

// Check summary
GET /api/goals/summary
{ "totalSaved": 500, "totalTarget": 10000, "activeGoals": 1 }
```

## Tests
To run the unit and controller tests:

```powershell
mvn test
```

This runs Spring Boot tests for:
- Basic application context loading
- Login and home controllers
- Expense service and expense REST API behaviour
- Budget service and budget management (creation, updates, validation)
- **Goal service with progress calculations** (create, update, delete, contributions)
- **Goal controller with API endpoints** (CRUD operations, summary statistics)
- **Contribution tracking and accumulation** (multiple contributions, edge cases)

### Test Coverage
- **Expenses**: Service + Controller tests
- **Budgets**: Service + Controller tests  
- **Dashboard**: Service + Controller tests
- **Goals**: 30+ service tests + 15+ controller tests
  - Progress calculation accuracy (0%, 25%, 50%, 100%, >100%)
  - Contribution accumulation and validation
  - Summary statistics (totalSaved, totalTarget, activeGoals)
  - Error handling and edge cases
