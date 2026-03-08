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

## Business Section – Tag-Based Expense Tracking & Analytics

### Overview
The Business section enables users to create custom tags for categorizing expenses, track business-related spending, and view detailed analytics. Both individual and business account types can use these features to gain insights into tagged expenses.

### Features

#### 1. Business Tags Management
- **Create Tags**: Define custom tags with name and color (e.g., "Essential" #FF5722, "Health" #4CAF50)
- **Tag Expenses**: Assign one or multiple tags to expenses for better organization
- **Update Tags**: Modify tag names and colors
- **Delete Tags**: Remove unused tags (tag associations are cleaned up automatically)
- **Tag Statistics**: View usage count for each tag

#### 2. Expense Tagging
- **Tag During Creation**: Add tags when creating a new expense
- **Tag During Edit**: Update expense tags when editing
- **Multiple Tags**: Assign multiple tags to a single expense
- **Tag Filtering**: Filter expenses by tag to view specific categories
- **Visual Indicators**: Tagged expenses display 💼 BIZ badge and business corner ribbon

#### 3. Business Analytics Dashboard
- **Total Expenses**: Sum of all expenses with business tags
- **Active Tags**: Count of tags currently in use
- **Total Transactions**: Count of tagged expenses
- **Spending by Tag**: Bar chart showing spending distribution across tags
- **Category Breakdown**: Expense totals grouped by category with tag associations
- **Recent Tagged Expenses**: List of latest expenses with their tags
- **Monthly Tag Report**: Time-series data of tag usage
- **Tag Usage Stats**: Count of expenses per tag

### API Endpoints (REST)

#### Tags Management
- `GET /api/tags` – List all tags for authenticated user
- `GET /api/tags/{id}` – Get a specific tag
- `POST /api/tags` – Create a new tag
  - Body: `{ name, color }`
  - Example: `{ "name": "Essential", "color": "#FF5722" }`
- `PUT /api/tags/{id}` – Update a tag
- `DELETE /api/tags/{id}` – Delete a tag
- `GET /api/tags/{id}/stats` – Get tag usage statistics

#### Expense-Tag Associations
- `GET /api/expenses/{id}/tags` – Get tags for a specific expense
- `POST /api/expenses/{id}/tags/{tagId}` – Add a tag to an expense
- `DELETE /api/expenses/{id}/tags/{tagId}` – Remove a tag from an expense

#### Filtering
- `GET /api/expenses?tagId={id}` – Filter expenses by tag ID
- `GET /api/expenses?tagName={name}` – Filter expenses by tag name

#### Analytics
- `GET /api/business/analytics` – Get comprehensive business analytics
  - Returns: 
    - `stats`: { totalExpenses, activeTags, totalTransactions }
    - `expenseTags`: Array of tags with usage counts
    - `spendingByTag`: Spending totals per tag
    - `categoryData`: Expenses grouped by category with tags
    - `recentExpenses`: Latest tagged expenses
    - `monthlyTagData`: Time-series tag usage
    - `incomeExpensesData`: Income vs expense trends

### Data Model

#### Tag Entity
```javascript
{
  id: Long,
  user: UserEntity,
  name: String,        // "Essential", "Health", etc.
  color: String,       // "#FF5722", "#4CAF50", etc.
  createdAt: DateTime
}
```

#### Expense-Tag Association
```javascript
{
  id: Long,
  expense: ExpenseEntity,
  tag: TagEntity,
  createdAt: DateTime
}
```

**Unique Constraint:** One expense can have a specific tag only once (prevents duplicates)

### Tag Update Workflow (Critical Feature)

**Problem Solved:** Editing an expense with existing tags previously caused duplicate key constraint violations.

**Solution:** When updating expense tags:
1. Delete all existing tag associations from database
2. Clear in-memory collection
3. Add new tags from the update request

**Example:**
```javascript
// Create expense with "Essential" tag
POST /api/expenses
{ 
  name: "Office Supplies",
  category: "Business",
  amount: 150.00,
  tags: [{ id: 1 }]  // Essential tag
}

// Update to use "Health" tag instead
PUT /api/expenses/110
{ 
  name: "Office Supplies",
  category: "Business",
  amount: 150.00,
  tags: [{ id: 2 }]  // Health tag
}
// Result: Essential tag removed, Health tag added, no duplicates!

// Update again with same tag (edge case that was failing)
PUT /api/expenses/110
{ 
  name: "Office Supplies",
  category: "Business",
  amount: 150.00,
  tags: [{ id: 2 }]  // Health tag again
}
// Result: No duplicate error! Tag properly replaced.
```

### Validation Rules

#### Tag Creation
- `name` is required (max 100 chars)
- `color` is required (hex color format recommended)
- `userId` auto-set from authenticated user
- Tag names can be duplicated (different users can have same tag names)

#### Expense Tagging
- Tag must belong to the same user as the expense
- Cannot tag someone else's expense with your tags
- Cannot use someone else's tags on your expenses
- Multiple tags allowed per expense
- **No duplicate tags** per expense (enforced by database constraint)

#### Tag Deletion
- Deleting a tag removes all expense-tag associations
- Expenses remain intact (only the tag link is removed)

### User Isolation & Security

**Data Privacy:**
- Users can only see their own tags
- Users can only tag their own expenses
- Business user's tags are invisible to individual users
- Analytics show only the authenticated user's data

**Example:**
```javascript
// Business user creates tag
POST /api/tags (as business user)
{ "name": "Business Tag", "color": "#2196F3" }

// Individual user creates tag
POST /api/tags (as indiv user)
{ "name": "Personal Tag", "color": "#4CAF50" }

// Business user lists tags
GET /api/tags (as business user)
// Returns: ["Business Tag"]  ✅

// Individual user lists tags
GET /api/tags (as indiv user)
// Returns: ["Personal Tag"]  ✅

// Individual user tries to access business user's tag
GET /api/tags/1 (as indiv, tag belongs to business)
// Returns: 403 Forbidden  ✅
```

### UI Features

#### Expense Form – Business Tags Section
- **Tag Selection**: Checkboxes for available tags
- **Visual Feedback**: Tags shown with their custom colors
- **Optional**: Tags are optional when creating/editing expenses
- **Multiple Selection**: Check multiple tags for one expense

#### Expense List – Visual Indicators
- **💼 BIZ Badge**: Appears next to expense name if tagged
- **Business Corner Ribbon**: 💼 icon in top-right corner of tagged expenses
- **Tag Count Badge**: Shows number of tags (e.g., "2 tags")
- **Special CSS**: `.business-expense` class for styling

#### Business Analytics Page
- **Summary Cards**: Total expenses, active tags, transaction count
- **Tag List**: All tags with usage count and color preview
- **Spending Chart**: Horizontal bar chart showing spending by tag
- **Category Breakdown**: Categories with associated tags
- **Recent Transactions**: Latest expenses with tag badges
- **Monthly Report**: Time-series visualization of tag usage

### Error Handling

**Common Errors:**
- **Tag not found** → 400: "Tag not found: {id}"
- **Expense not found** → 400: "Expense not found: {id}"
- **Unauthorized tag access** → 403: "Cannot access tag that does not belong to user"
- **Unauthorized expense access** → 403: "Cannot tag expense that does not belong to user"
- **Duplicate tag on expense** → Prevented by automatic cleanup during update
- **Invalid tag data** → 400: "Tag name is required"

### Example Workflows

#### Complete Business Tagging Workflow
```javascript
// 1. Create business tags
POST /api/tags
{ "name": "Essential", "color": "#FF5722" }
// Returns: { id: 1, name: "Essential", color: "#FF5722" }

POST /api/tags
{ "name": "Marketing", "color": "#2196F3" }
// Returns: { id: 2, name: "Marketing", color: "#2196F3" }

// 2. Create expense with tags
POST /api/expenses
{
  name: "Ad Campaign",
  category: "Marketing",
  amount: 500.00,
  date: "2026-03-08",
  tags: [
    { id: 1 },  // Essential
    { id: 2 }   // Marketing
  ]
}

// 3. Filter expenses by tag
GET /api/expenses?tagId=2
// Returns: All expenses tagged with "Marketing"

// 4. View analytics
GET /api/business/analytics
// Returns: Complete analytics dashboard data

// 5. Update expense tags
PUT /api/expenses/110
{
  name: "Ad Campaign",
  category: "Marketing",
  amount: 500.00,
  date: "2026-03-08",
  tags: [{ id: 2 }]  // Keep only Marketing tag
}
// Essential tag removed, Marketing tag kept

// 6. Get tag statistics
GET /api/tags/2/stats
// Returns: { tagId: 2, tagName: "Marketing", usageCount: 1 }
```

### Tests

The business section has comprehensive test coverage with **21 tests** across 3 test classes:

#### BusinessControllerTest (4 tests)
- Unit tests for business analytics API endpoint
- Tests analytics retrieval for business and individual users
- Validates empty data handling
- Verifies all required fields are returned

#### BusinessAnalyticsServiceTest (9 tests)
- Unit tests for analytics calculations
- Tests totals, aggregations, and data grouping
- Validates error handling for non-existent users
- Tests spending by tag, category data, recent expenses
- Verifies correct calculations (e.g., $350 = $150 + $200)

#### BusinessTagServiceIntegrationTest (8 tests)
- **Integration tests with real database and Spring context**
- **Tests the duplicate tag bug fix** (critical feature)
- Tests complete workflows end-to-end:
  - ✅ Create tags and tag expenses
  - ✅ Update expense tags (replace existing tags)
  - ✅ **Update with same tags multiple times (no duplicates!)**
  - ✅ Business analytics generation
  - ✅ Delete expenses with tags (cascade cleanup)
  - ✅ Filter expenses by tag
  - ✅ User data isolation (business vs individual)

**Key Test:** `updateExpenseWithMultipleTags_shouldNotCreateDuplicates()`
- Creates expense with 2 tags
- Updates with same 2 tags again
- Verifies no "Duplicate entry" error
- **This test validates the critical bug fix**

### Running Business Tests
```powershell
# Run all business tests
mvn test -Dtest=BusinessControllerTest,BusinessAnalyticsServiceTest,BusinessTagServiceIntegrationTest

# Run just integration tests
mvn test -Dtest=BusinessTagServiceIntegrationTest

# Run specific test (the duplicate fix test)
mvn test -Dtest=BusinessTagServiceIntegrationTest#updateExpenseWithMultipleTags_shouldNotCreateDuplicates
```

**Expected Output:**
```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
✅ BUILD SUCCESS
```

### Technical Implementation

**Service Layer:**
- `TagService`: CRUD operations for tags
- `ExpenseService`: Enhanced with tag management methods
- `BusinessAnalyticsService`: Analytics calculations and data aggregation

**Repository Layer:**
- `TagRepository`: JPA repository for tags
- `ExpenseTagRepository`: Join table repository with custom queries
- Custom query methods: `findByUserAndName`, `findExpensesByTag`, etc.

**Key Method (Bug Fix):**
```java
// ExpenseService.updateExpenseTags()
private void updateExpenseTags(ExpenseEntity expense, List<?> tagsList, UserEntity user) {
    // Delete all existing tag associations
    expenseTagRepository.deleteByExpense(expense);
    
    // Clear in-memory collection
    expense.getExpenseTags().clear();
    
    // Add new tags
    for (Long tagId : desiredTagIds) {
        TagEntity tag = tagRepository.findById(tagId).orElse(null);
        if (tag != null && tag.getUser().getId().equals(user.getId())) {
            expense.addTag(tag);
        }
    }
}
```

**Annotations Used:**
- `@Transactional`: Ensures atomic tag updates
- `@UniqueConstraint`: Database-level duplicate prevention
- `@OneToMany`: Expense to ExpenseTag relationship
- `@ManyToOne`: ExpenseTag to Tag relationship

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
