-- Return database to its initial state, right after schema.sql was executed.

-- Remove all inserted data
DELETE FROM INVENTORY;
DELETE FROM ORDERS;
DELETE FROM STAFF;
DELETE FROM DELIVERIES;
DELETE FROM ORDER_PRODUCTS;
DELETE FROM COLLECTIONS;
DELETE FROM STAFF_ORDERS;

-- Begin all ID-generating sequences at initial value
ALTER SEQUENCE seq_inv RESTART;
ALTER SEQUENCE seq_ord RESTART;
ALTER SEQUENCE seq_staff RESTART;

