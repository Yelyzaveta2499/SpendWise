-- Use INSERT IGNORE so repeated app starts don't fail on unique constraints
INSERT IGNORE INTO roles (name) VALUES ('INDIVIDUAL');
INSERT IGNORE INTO roles (name) VALUES ('BUSINESS');
INSERT IGNORE INTO roles (name) VALUES ('KIDS');

-- Seed users matching in-memory security users. Assumes role IDs 1,2,3 in insertion order.
INSERT IGNORE INTO users (username, password, role_id) VALUES ('indiv', 'dummy', 1);
INSERT IGNORE INTO users (username, password, role_id) VALUES ('business', 'dummy', 2);
INSERT IGNORE INTO users (username, password, role_id) VALUES ('kid', 'dummy', 3);

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
