# SQL Maintenance Scripts

This folder contains utility SQL scripts for database maintenance and verification.

## Scripts

### `cleanup-duplicates.sql`
Removes duplicate expense entries from the database.

**When to use:**
- If you accidentally create duplicate expenses
- After importing data from external sources
- When testing creates unwanted duplicates

**How to run:**
```bash
mysql -u root -proot spendwisedb < cleanup-duplicates.sql
```

**What it does:**
1. Shows current duplicates
2. Deletes duplicates (keeps the one with lowest ID)
3. Verifies duplicates are gone
4. Shows remaining expenses

---

### `verify-expenses.sql`
Checks database integrity and shows current expenses.

**When to use:**
- Quick health check of your expenses data
- Before/after database operations
- Regular maintenance verification

**How to run:**
```bash
mysql -u root -proot spendwisedb < verify-expenses.sql
```

**What it shows:**
1. Any duplicate entries found
2. All expenses for 'indiv' user
3. Summary count by category

---

## Notes

- These scripts target the `spendwisedb` database
- They are safe to run multiple times
- Always backup your data before running cleanup operations
- Created: Feb 19, 2026

