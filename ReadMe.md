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

## Tests
To run the unit and controller tests:

```powershell
mvn test
```

This runs Spring Boot tests for:
- Basic application context loading
- Login and home controllers
- Expense service and expense REST API behaviour.

## Known limitations
- Authentication still uses in-memory users; there is no registration UI.
- Validation is basic (simple checks on amount, name, category, and date).
- UI is desktop-focused and has not been optimized for mobile.
