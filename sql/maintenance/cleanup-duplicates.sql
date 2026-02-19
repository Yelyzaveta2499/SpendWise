-- SQL script to remove duplicate expenses and keep only one of each
-- Run this in your MySQL database to clean up duplicate "Groceries" and other entries

USE spendwisedb;

-- First, let's see what duplicates we have
SELECT name, category, expense_date, COUNT(*) as count
FROM expenses
GROUP BY name, category, expense_date
HAVING count > 1;

-- Delete duplicates, keeping the one with the lowest ID
DELETE e1 FROM expenses e1
INNER JOIN expenses e2
WHERE e1.id > e2.id
  AND e1.name = e2.name
  AND e1.category = e2.category
  AND e1.expense_date = e2.expense_date
  AND e1.user_id = e2.user_id;

-- Verify duplicates are gone
SELECT name, category, expense_date, COUNT(*) as count
FROM expenses
GROUP BY name, category, expense_date
HAVING count > 1;

-- Show remaining expenses
SELECT id, name, category, amount, expense_date, user_id
FROM expenses
ORDER BY expense_date DESC, id DESC;

