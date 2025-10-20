-- ========================================
-- USERS
-- ========================================
INSERT INTO USERS (ID, NAME, EMAIL, OAUTH_ID, ROLE) VALUES (1, 'Alice', 'alice@example.com', 'alice_google_id', 'USER');
INSERT INTO USERS (ID, NAME, EMAIL, OAUTH_ID, ROLE) VALUES (2, 'Bob', 'bob@example.com', 'bob_google_id', 'ADMIN');
INSERT INTO USERS (ID, NAME, EMAIL, OAUTH_ID, ROLE) VALUES (3, 'Charlie', 'charlie@example.com', 'charlie_google_id', 'USER');
INSERT INTO USERS (ID, NAME, EMAIL, OAUTH_ID, ROLE) VALUES (4, 'David', 'david@example.com', 'david_google_id', 'USER');

-- ========================================
-- GROUPS
-- ========================================
INSERT INTO GROUPS (ID, NAME) VALUES (1, 'Friends Trip');
INSERT INTO GROUPS (ID, NAME) VALUES (2, 'Office Lunch');

-- ========================================
-- GROUP MEMBERS
-- ========================================
-- Group 1 members
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (1, 1);
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (1, 2);
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (1, 3);
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (1, 4);

-- Group 2 members
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (2, 2);
INSERT INTO GROUP_MEMBERS (GROUP_ID, USER_ID) VALUES (2, 3);

-- ========================================
-- EXPENSES
-- ========================================
-- Equal split example
INSERT INTO EXPENSES (ID, DESCRIPTION, GROUP_ID, PAID_BY_ID, TOTAL_AMOUNT, IS_EQUAL_SPLIT, CREATED_ON) 
VALUES (1, 'Hotel Booking', 1, 2, 120.00, true, CURRENT_TIMESTAMP);

-- Custom split example
INSERT INTO EXPENSES (ID, DESCRIPTION, GROUP_ID, PAID_BY_ID, TOTAL_AMOUNT, IS_EQUAL_SPLIT, CREATED_ON) 
VALUES (2, 'Dinner', 1, 2, 100.00, false, CURRENT_TIMESTAMP);

-- ========================================
-- EXPENSE SPLITS (for custom split expense)
-- ========================================
INSERT INTO EXPENSE_SPLITS (EXPENSE_ID, USER_ID, AMOUNT) VALUES (2, 1, 30.00);
INSERT INTO EXPENSE_SPLITS (EXPENSE_ID, USER_ID, AMOUNT) VALUES (2, 2, 20.00);
INSERT INTO EXPENSE_SPLITS (EXPENSE_ID, USER_ID, AMOUNT) VALUES (2, 3, 50.00);

-- ========================================
-- SETTLEMENTS
-- ========================================
-- Alice pays Bob 30 for Dinner
INSERT INTO SETTLEMENTS (ID, EXPENSE_ID, PAYER_ID, RECEIVER_ID, AMOUNT, CREATED_ON) 
VALUES (1, 2, 1, 2, 30.00, CURRENT_TIMESTAMP);
