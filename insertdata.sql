-- INVENTORY: ProductID ProductDesc ProductPrice ProductStockAmount
INSERT INTO INVENTORY VALUES (0, 'eggs', 4, 100);
INSERT INTO INVENTORY VALUES (1, 'bread', 3.27, 10);
INSERT INTO INVENTORY VALUES (2, 'cabbage', 2, 100);
INSERT INTO INVENTORY VALUES (3, 'cucumber', 5, 7);
INSERT INTO INVENTORY VALUES (4, 'truffles', 2000, 200);
INSERT INTO INVENTORY VALUES (5, 'watermelon', 1000, 300);
-- OPTION4 must be shown.
INSERT INTO INVENTORY VALUES (6, 'ahncellabol', 1, 1000);


-- STAFF: StaffID FName LName
INSERT INTO STAFF VALUES (0, 'Ned', 'Stark');
INSERT INTO STAFF VALUES (1, 'Grey', 'Worm');
INSERT INTO STAFF VALUES (2, 'Tyrion', 'Lannister');
INSERT INTO STAFF VALUES (3, 'Euron', 'Greyjoy');

-- ORDERS: OrderID OrderType OrderCompleted OrderPlaced
INSERT INTO ORDERS VALUES (0, 'InStore', 1, TO_DATE('2018-11-24', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (1, 'Collection', 0, TO_DATE('2019-11-25', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (2, 'Delivery', 0, TO_DATE('2019-11-10', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (3, 'InStore', 1, TO_DATE('2019-11-23', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (4, 'InStore', 1, TO_DATE('2019-11-1', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (5, 'InStore', 1, TO_DATE('2019-11-2', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (6, 'InStore', 1, TO_DATE('2019-11-3', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (7, 'InStore', 1, TO_DATE('2019-11-4', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (8, 'InStore', 1, TO_DATE('2019-11-5', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (9, 'InStore', 1, TO_DATE('2018-11-5', 'YYYY-MM-DD'));
-- do not remove at option 5 01-dec-19
INSERT INTO ORDERS VALUES (10, 'Collection', 0, TO_DATE('2018-11-24', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (11, 'Collection', 1, TO_DATE('2017-11-24', 'YYYY-MM-DD'));
-- OPTION 5 must be removed
INSERT INTO ORDERS VALUES (12, 'Collection', 0, TO_DATE('2019-11-01', 'YYYY-MM-DD'));
INSERT INTO ORDERS VALUES (13, 'Collection', 0, TO_DATE('2019-11-02', 'YYYY-MM-DD'));

-- ORDER_PRODUCTS: OrderID ProductID ProductQuantity
INSERT INTO ORDER_PRODUCTS VALUES(0,0,10);
INSERT INTO ORDER_PRODUCTS VALUES(0,1,5);
INSERT INTO ORDER_PRODUCTS VALUES(0,5,1);
INSERT INTO ORDER_PRODUCTS VALUES(1,0,1);
INSERT INTO ORDER_PRODUCTS VALUES(2,2,3);
INSERT INTO ORDER_PRODUCTS VALUES(3,0,5);
INSERT INTO ORDER_PRODUCTS VALUES(4,1,3);
INSERT INTO ORDER_PRODUCTS VALUES(5,4,15);
INSERT INTO ORDER_PRODUCTS VALUES(6,5,7);
INSERT INTO ORDER_PRODUCTS VALUES(7,5,15);
INSERT INTO ORDER_PRODUCTS VALUES(8,5,2);
INSERT INTO ORDER_PRODUCTS VALUES(9,3,7);
INSERT INTO ORDER_PRODUCTS VALUES(10,3,9);
INSERT INTO ORDER_PRODUCTS VALUES(11,4,25);
INSERT INTO ORDER_PRODUCTS VALUES(12,3,7);
INSERT INTO ORDER_PRODUCTS VALUES(13,6,1);

-- DELIVERIES: OrderID FName LName House Street City DeliveryDate
INSERT INTO DELIVERIES VALUES (2, 'John', 'Doe', '1', 'St', 'Neverland', TO_DATE('2020-01-02', 'YYYY-MM-DD'));

-- COLLECTIONS: OrderID FName LName CollectionDate
INSERT INTO COLLECTIONS VALUES (1, 'somename', 'somelname', TO_DATE('2019-11-30', 'YYYY-MM-DD'));
INSERT INTO COLLECTIONS VALUES (11, 'colfname2', 'collname2', TO_DATE('2019-11-30', 'YYYY-MM-DD'));
INSERT INTO COLLECTIONS VALUES (10, 'colfname3', 'collnae3', TO_DATE('2019-11-30', 'YYYY-MM-DD'));
INSERT INTO COLLECTIONS VALUES (12, 'colfname4', 'collnae4', TO_DATE('2019-11-02', 'YYYY-MM-DD'));
INSERT INTO COLLECTIONS VALUES (13, 'colfname5', 'collnae5', TO_DATE('2019-11-03', 'YYYY-MM-DD'));

-- STAFF_ORDERS: StaffID OrderID
INSERT INTO STAFF_ORDERS VALUES(0, 0);
INSERT INTO STAFF_ORDERS VALUES(0, 1);
INSERT INTO STAFF_ORDERS VALUES(0, 2);
INSERT INTO STAFF_ORDERS VALUES(1, 3);
INSERT INTO STAFF_ORDERS VALUES(1, 4);
INSERT INTO STAFF_ORDERS VALUES(1, 5);
INSERT INTO STAFF_ORDERS VALUES(2, 6);
INSERT INTO STAFF_ORDERS VALUES(2, 7);
INSERT INTO STAFF_ORDERS VALUES(2, 8);
INSERT INTO STAFF_ORDERS VALUES(2, 9);
INSERT INTO STAFF_ORDERS VALUES(2, 10);
INSERT INTO STAFF_ORDERS VALUES(3, 11);
INSERT INTO STAFF_ORDERS VALUES(3, 12);
INSERT INTO STAFF_ORDERS VALUES(0, 13);
