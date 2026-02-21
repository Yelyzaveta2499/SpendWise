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

## Tests
To run the unit and controller tests:

```powershell
mvn test
```

This runs Spring Boot tests for:
- Basic application context loading
- Login and home controllers
- Expense service and expense REST API behaviour.
