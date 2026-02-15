INSERT INTO roles (name) VALUES ('INDIVIDUAL');
INSERT INTO roles (name) VALUES ('BUSINESS');
INSERT INTO roles (name) VALUES ('KIDS');

-- Seed users matching in-memory security users. Assumes role IDs 1,2,3 in insertion order.
INSERT INTO users (username, password, role_id) VALUES ('indiv', 'dummy', 1);
INSERT INTO users (username, password, role_id) VALUES ('business', 'dummy', 2);
INSERT INTO users (username, password, role_id) VALUES ('kid', 'dummy', 3);
