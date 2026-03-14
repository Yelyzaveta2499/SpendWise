-- Use INSERT IGNORE so repeated app starts don't fail on unique constraints
INSERT IGNORE INTO roles (name) VALUES ('INDIVIDUAL');
INSERT IGNORE INTO roles (name) VALUES ('BUSINESS');
INSERT IGNORE INTO roles (name) VALUES ('KIDS');

-- Seed default users with BCrypt-hashed password "password".
-- This keeps existing login credentials the same while storing encrypted passwords in DB.
INSERT IGNORE INTO users (id, username, password, role_id)
VALUES (
    1,
    'indiv',
    '$2a$10$pb63mjbSxcq3N0YUubEA0uknwMSIXi8FGYzCBDpTnccvaYZNMrPGS',
    (SELECT id FROM roles WHERE name = 'INDIVIDUAL' LIMIT 1)
);

INSERT IGNORE INTO users (id, username, password, role_id)
VALUES (
    2,
    'business',
    '$2a$10$pb63mjbSxcq3N0YUubEA0uknwMSIXi8FGYzCBDpTnccvaYZNMrPGS',
    (SELECT id FROM roles WHERE name = 'BUSINESS' LIMIT 1)
);

-- One-time migration: if old rows contain plain text passwords, convert them to BCrypt.
UPDATE users
SET password = '$2a$10$pb63mjbSxcq3N0YUubEA0uknwMSIXi8FGYzCBDpTnccvaYZNMrPGS'
WHERE username IN ('indiv', 'business')
  AND password NOT LIKE '$2a$%'
  AND password NOT LIKE '$2b$%'
  AND password NOT LIKE '$2y$%';

-- Zero-amount placeholder expenses for 'indiv' so they are visible in the UI.
-- Assumes the 'indiv' user has id = 1. INSERT IGNORE prevents duplicates on restart.
-- INSERT IGNORE INTO expenses (user_id, name, category, amount, expense_date, created_at, updated_at) VALUES
--(1, 'Grocery Store', 'Food & Dining', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Monthly Salary', 'Income', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Coffee Shop', 'Coffee', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Rent Payment', 'Housing', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Gas Station', 'Transportation', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Phone Bill', 'Utilities', 0.00, CURRENT_DATE, NOW(), NOW()),
  --(1, 'Amazon Purchase', 'Shopping', 0.00, CURRENT_DATE, NOW(), NOW());

-- Seed tags for 'business' user (user_id = 2)
INSERT IGNORE INTO tags (user_id, name, color, description, created_at, updated_at) VALUES
(2, 'Client A', '#0ea5e9', 'Expenses related to Client A projects', NOW(), NOW()),
(2, 'Project X', '#a855f7', 'Project X specific expenses', NOW(), NOW()),
(2, 'Q1 2024', '#f59e0b', 'First quarter 2024 expenses', NOW(), NOW()),
(2, 'Marketing', '#ec4899', 'Marketing and advertising expenses', NOW(), NOW()),
(2, 'Operations', '#10b981', 'Operational expenses', NOW(), NOW()),
(2, 'Vendor B', '#06b6d4', 'Expenses from Vendor B', NOW(), NOW()),
(2, 'Recurring', '#10b981', 'Recurring monthly expenses', NOW(), NOW()),
(2, 'Tax Deductible', '#eab308', 'Tax deductible business expenses', NOW(), NOW());

-- Seed tags for 'indiv' user (user_id = 1)
INSERT IGNORE INTO tags (user_id, name, color, description, created_at, updated_at) VALUES
(1, 'Essential', '#10b981', 'Essential expenses', NOW(), NOW()),
(1, 'Savings Goal', '#3b82f6', 'Related to savings goals', NOW(), NOW()),
(1, 'Vacation', '#f59e0b', 'Vacation-related expenses', NOW(), NOW()),
(1, 'Health', '#ec4899', 'Health and wellness', NOW(), NOW());

