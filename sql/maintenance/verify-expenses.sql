-- Quick verification script to check for duplicates in expenses table
-- Run this anytime to ensure your data is clean

USE spendwisedb;

-- Check for any duplicates (should return 0 rows)
SELECT name, category, expense_date, user_id, COUNT(*) as duplicate_count
FROM expenses
GROUP BY name, category, expense_date, user_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Show all current expenses for 'indiv' user
SELECT
    e.id,
    e.name,
    e.category,
    e.amount,
    DATE_FORMAT(e.expense_date, '%b %d') as date_formatted,
    e.expense_date
FROM expenses e
JOIN users u ON e.user_id = u.id
WHERE u.username = 'indiv'
ORDER BY e.expense_date DESC, e.id DESC;

-- Summary count by category
SELECT
    category,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM expenses e
JOIN users u ON e.user_id = u.id
WHERE u.username = 'indiv'
GROUP BY category
ORDER BY count DESC;

