# SpendWise

SpendWise is a comprehensive Spring Boot web application designed for tracking personal and business expenses. It provides a user-friendly interface for managing budgets, tracking savings goals, and analyzing expenses with advanced tagging and analytics features.

## Tech Stack
- **Java 21**
- **Spring Boot**: Web, Security, Data JPA
- **Thymeleaf**: Template engine for dynamic HTML rendering
- **Vanilla JavaScript**: Frontend interactivity
- **MySQL**: Relational database

## Features

### 1. Dashboard Financial Overview
- **Quick Summary**: Total Balance, Income, Expenses, Savings Rate
- **Charts**: Income vs Expenses (last 6 months)
- **Recent Transactions**: Latest financial activities
- **Period Selector**: Refresh data for different time periods

### 2. Expense Management
- **Add Expenses**: Name, Category, Amount, Date
- **Search & Filter**: By text or category
- **REST API**: `GET /api/expenses`, `POST /api/expenses`

### 3. Budget Management
- **Per Category Budgets**: Monthly and yearly tracking
- **CRUD Operations**: Create, Update, Delete budgets
- **Validation Rules**: Unique budgets per category/month/year

### 4. Savings Goals
- **Define Goals**: Name, Target Amount, Deadline, Icon, Color
- **Track Progress**: Visual progress bars, percentage calculations
- **Contributions**: Add savings contributions with notes
- **Summary Dashboard**: Total saved, total target, active goals

### 5. Business Analytics
- **Tag-Based Tracking**: Create and manage custom tags
- **Expense Tagging**: Assign multiple tags to expenses
- **Analytics Dashboard**: Spending by tag, category breakdown, recent tagged expenses

## Prerequisites
- **Java 21** or newer installed
- **Maven** installed and on PATH
- **MySQL** instance running
  - Default configuration (modifiable in `application.properties`):
    - URL: `jdbc:mysql://localhost:3306/spendwisedb?createDatabaseIfNotExist=true`
    - Username: `root`
    - Password: `root`

## How to Run
1. Clone the repository.
2. Navigate to the project root.
3. Run the application:
   ```powershell
   mvn spring-boot:run
   ```
4. Access the app at: [http://localhost:1111](http://localhost:1111)

## Testing
- **Run All Tests**:
  ```powershell
  mvn test
  ```
- **Specific Tests**:
  ```powershell
  mvn test -Dtest=BusinessTagServiceIntegrationTest
  ```

## Database Seeding
- **On Startup**:
  - `data.sql` seeds roles and users.
  - `ExpenseDataInitializer` adds placeholder expenses for new users.

## API Endpoints (examples)

- **Dashboard**:
  - `GET /api/dashboard/overview`: Fetches an overview of the dashboard for a specific period.
  - `GET /api/dashboard/total-wealth`: Retrieves the total wealth of the authenticated user.

- **Expenses**:
  - `GET /api/expenses`: Lists expenses for the authenticated user, with optional filtering by tag ID or tag name.
  - `GET /api/expenses/{id}/tags`: Retrieves tags associated with a specific expense.
  - `POST /api/expenses/{id}/tags/{tagId}`: Adds a tag to a specific expense.

- **Budgets**:
  - `GET /api/budgets`: Lists all budgets for the authenticated user.
  - `POST /api/budgets`: Creates a new budget for the authenticated user.
  - `DELETE /api/budgets/{id}`: Deletes a specific budget for the authenticated user.

## Contributors
- **Developers**: Yelyzaveta Bezusa-Hlushych
- **Contact**: glushichliza@gmail.com
