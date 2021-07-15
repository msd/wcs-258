-- DELETE ANY OBJECTS IF THEY EXIST --
START deleteschema;

-- SEQUENCES FOR UNIQUE IDS --

-- INVENTORY(ProductID) generator
CREATE SEQUENCE seq_inv
	MINVALUE 0
	START WITH 0
	INCREMENT BY 1
	CACHE 10;
	
-- ORDERS(OrderID) generator
CREATE SEQUENCE seq_ord
	MINVALUE 0
	START WITH 0
	INCREMENT BY 1
	CACHE 10;

-- STAFF(StaffID) generator
CREATE SEQUENCE seq_staff
	MINVALUE 0
	START WITH 0
	INCREMENT BY 1
	CACHE 10;

-- CREATE TABLES --
CREATE TABLE INVENTORY (
	ProductID INTEGER PRIMARY KEY,
	ProductDesc VARCHAR(30) NOT NULL,
	ProductPrice NUMERIC(8,2) NOT NULL, 
	ProductStockAmount INTEGER NOT NULL,
	CONSTRAINT CHK_Product_Price CHECK (ProductPrice >= 0),
	CONSTRAINT CHK_Product_Stock CHECK (ProductStockAmount >= 0)
);

CREATE TABLE ORDERS (
	OrderID INTEGER PRIMARY KEY,
	OrderType VARCHAR(30) NOT NULL,
	OrderCompleted INTEGER NOT NULL,
	OrderPlaced DATE DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT CHK_Orders_Type CHECK (OrderType IN ('InStore','Collection','Delivery')),
	CONSTRAINT CHK_Orders_Completed CHECK (OrderCompleted BETWEEN 0 AND 1)
);

CREATE TABLE ORDER_PRODUCTS (
	OrderID INTEGER,
	ProductID INTEGER,
	ProductQuantity INTEGER NOT NULL,
	PRIMARY KEY (OrderID, ProductID),
	FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
		ON DELETE CASCADE,
	FOREIGN KEY (ProductID) REFERENCES INVENTORY(ProductID)
		ON DELETE CASCADE,
	CONSTRAINT CHK_Order_Product_Quantity CHECK (ProductQuantity > 0)
);

CREATE TABLE DELIVERIES (
	OrderID INTEGER PRIMARY KEY,
	FName VARCHAR(30) NOT NULL,
	LName VARCHAR(30) NOT NULL,
	House VARCHAR(30) NOT NULL,
	Street VARCHAR(30) NOT NULL,
	City VARCHAR(30) NOT NULL, 
	DeliveryDate DATE DEFAULT CURRENT_TIMESTAMP NOT NULL,
	FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
		ON DELETE CASCADE
);

CREATE TABLE COLLECTIONS (
	OrderID INTEGER PRIMARY KEY,
	FName VARCHAR(30) NOT NULL,
	LName VARCHAR(30) NOT NULL,
	CollectionDate DATE DEFAULT CURRENT_TIMESTAMP NOT NULL,
	FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
		ON DELETE CASCADE
);

CREATE TABLE STAFF (
	StaffID INTEGER PRIMARY KEY,
	FName VARCHAR(30) NOT NULL,
	LName VARCHAR(30) NOT NULL,
	FOREIGN KEY (StaffID) REFERENCES STAFF(StaffID)
		ON DELETE CASCADE
);

CREATE TABLE STAFF_ORDERS (
	StaffID INTEGER,
	OrderID INTEGER,
	PRIMARY KEY (StaffID, OrderID),
	FOREIGN KEY (StaffID) REFERENCES STAFF(StaffID)
		ON DELETE CASCADE,
	FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
		ON DELETE CASCADE
);

-- OPTION 4

CREATE VIEW VIEW_PRODUCT_SALES AS
	SELECT ProductID, SUM(ProductQuantity) ProductTimesBought
	FROM ORDER_PRODUCTS
	GROUP BY ProductID;

CREATE VIEW VIEW_REVENUE_PER_PRODUCT AS
	SELECT ProductID, ProductTimesBought * ProductPrice AS ProductRevenue
	FROM VIEW_PRODUCT_SALES INNER JOIN INVENTORY USING (ProductID);

CREATE VIEW v_opt4 AS
	SELECT ProductID, ProductDesc, NVL(ProductRevenue, 0) AS ProductRevenue
	FROM INVENTORY LEFT JOIN VIEW_REVENUE_PER_PRODUCT USING (ProductID)
	ORDER BY ProductRevenue DESC;

-- OPTION 6

CREATE VIEW VIEW_ORDER_TOTALS AS
	SELECT OrderID, SUM(ProductQuantity * ProductPrice) AS OrderTotal
	FROM ORDER_PRODUCTS INNER JOIN INVENTORY USING (ProductID)
	GROUP BY OrderID;

CREATE VIEW VIEW_ORDER_TOTAL_PER_STAFF AS	
	SELECT StaffID, SUM(OrderTotal) AS StaffSales
	FROM STAFF_ORDERS INNER JOIN VIEW_ORDER_TOTALS USING (OrderID)
	GROUP BY StaffID;

CREATE VIEW VIEW_STAFF_SALES AS
	SELECT StaffID, FName, LName, StaffSales
	FROM STAFF INNER JOIN VIEW_ORDER_TOTAL_PER_STAFF USING (StaffID);

CREATE VIEW v_opt6 AS
	SELECT Fname, Lname, StaffSales
	FROM VIEW_STAFF_SALES 
	WHERE StaffSales >= 5000
	ORDER BY StaffSales DESC;

-- OPTION 7

CREATE VIEW VIEW_BEST_SELLING AS
	SELECT ProductID, ProductRevenue
	FROM VIEW_REVENUE_PER_PRODUCT
	WHERE ProductRevenue >= 20000;

CREATE VIEW VIEW_PRODUCT_COUNT_PER_STAFF AS
	SELECT StaffID, ProductID, SUM(ProductQuantity) AS ProductSoldCount
	FROM STAFF_ORDERS INNER JOIN ORDER_PRODUCTS USING (OrderID)
	GROUP BY StaffID, ProductID;

CREATE VIEW VIEW_BEST_SELLING_SELLERS AS
	SELECT FName, LName, StaffID, ProductID, NVL(ProductSoldCount, 0) AS ProductSoldCount
	FROM
		(
			SELECT FName, LName, StaffID, ProductID
			FROM 
				(SELECT FName, LName, StaffID FROM VIEW_STAFF_SALES ORDER BY StaffSales DESC)
				CROSS JOIN
				(SELECT ProductID FROM VIEW_BEST_SELLING)
		)
		LEFT JOIN
		VIEW_PRODUCT_COUNT_PER_STAFF
		USING (StaffID, ProductID);

CREATE VIEW v_opt7 AS
	SELECT FName, LName, StaffID, ProductID, ProductSoldCount
	FROM VIEW_BEST_SELLING_SELLERS;

-- OPTION 8

-- SALES PER STAFF PER YEAR
CREATE VIEW v_yearly_staff_sales AS 
	SELECT OrderYear, StaffID, SUM(OrderTotal) AS StaffYearlySales
	FROM 
		(
			SELECT OrderID, EXTRACT(YEAR FROM OrderPlaced) OrderYear, OrderTotal
			FROM
				VIEW_ORDER_TOTALS
				INNER JOIN
				ORDERS
				USING (OrderID)
		)
		INNER JOIN
		STAFF_ORDERS
		USING (OrderID)
	GROUP BY OrderYear, StaffID;

-- COUNT OF SALES PER PRODUCT PER YEAR
CREATE VIEW v_yearly_product_sales AS
	SELECT ProductID, SUM(ProductQuantity) AS ProductTimesBought, OrderYear
	FROM 
		(
			SELECT ProductID, ProductQuantity, EXTRACT(YEAR FROM OrderPlaced) AS OrderYear 
			FROM 
				ORDERS
				INNER JOIN
				ORDER_PRODUCTS
				USING (OrderID)
		)
	GROUP BY ProductID, OrderYear;

-- SALES PER PRODUCT PER YEAR
CREATE VIEW v_yearly_product_revenue AS
	SELECT OrderYear, ProductID, ProductTimesBought * ProductPrice AS ProductYearlyRevenue
	FROM
		v_yearly_product_sales
		INNER JOIN
		INVENTORY
		USING (ProductID);

-- PRODUCTS THAT HAVE SOLD AT LEAST 20k IN A YEAR
CREATE VIEW v_yearly_best_selling AS
	SELECT DISTINCT ProductID, OrderYear
	FROM v_yearly_product_revenue
	WHERE ProductYearlyRevenue >= 20000;

-- STAFF THAT HAVE SOLD AT LEAST 30k IN A YEAR
CREATE VIEW v_yearly_best_staff AS
	SELECT StaffID, OrderYear
	FROM v_yearly_staff_sales
	WHERE StaffYearlySales >= 30000;

CREATE VIEW v_products_sold_per_staff_per_year AS
	SELECT DISTINCT StaffID, ProductID, EXTRACT(YEAR FROM OrderPlaced) AS OrderYear
	FROM ORDERS INNER JOIN ORDER_PRODUCTS USING (OrderID) INNER JOIN STAFF_ORDERS USING (OrderID);

-- For all staff lists all years that were promotion-eligible years.
-- Promotion years are defined as having the properties described in option 8.
-- i.e. For every staff member M:
-- M made at least 30k$ sales 
-- AND M sold at least one of all the products that sold 20k$ in that year.
CREATE VIEW v_promotion_year_per_staff AS
	SELECT StaffID, OrderYear 
	FROM v_yearly_best_staff ybs
	WHERE NOT EXISTS (
		SELECT *
		FROM
			(
				SELECT ybs2.ProductID
				FROM v_yearly_best_selling ybs2
				WHERE ybs2.OrderYear = ybs.OrderYear
			) MINUS (
				SELECT pspspy.ProductID
				FROM v_products_sold_per_staff_per_year pspspy
				WHERE pspspy.StaffID = ybs.StaffID AND pspspy.OrderYear = ybs.OrderYear
			)
	);

CREATE VIEW v_sub_opt8 AS
	SELECT Fname, Lname, OrderYear
	FROM
		v_promotion_year_per_staff
		INNER JOIN
		STAFF
		USING (StaffID);