import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class representing the rows of STAFF table and any operations that can be
 * performed on it.
 */
class Staff
{
	/**
	 * @param 1
	 *            StaffID: INTEGER
	 */
	private static PreparedStatement select_stmnt;

	/**
	 * Select a row from the STAFF table.
	 * 
	 * @param staffid
	 *            The id of the row to select.
	 * @return Whether or not the staff with the given id exists.
	 */
	public static boolean exists(int staffid)
	{
		try
		{
			select_stmnt.setInt(1, staffid);
			ResultSet r = select_stmnt.executeQuery();
			return r.next();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "SELECT * FROM STAFF WHERE StaffID=?";
		select_stmnt = conn.prepareStatement(sql);
	}
}

/**
 * A class representing the link between the ORDER and STAFF tables.
 */
class StaffOrders
{
	/**
	 * @param 1
	 *            StaffID: INTEGER
	 * @param 2
	 *            OrderID: INTEGER
	 */
	private static PreparedStatement select_stmnt;
	/**
	 * @param 1
	 *            StaffID: INTEGER
	 * @param 2
	 *            OrderID: INTEGER
	 */
	private static PreparedStatement insert_stmnt;

	private static boolean areLinked(int staffid, int ordid) throws SQLException
	{
		select_stmnt.setInt(1, staffid);
		select_stmnt.setInt(2, ordid);
		ResultSet r = select_stmnt.executeQuery();
		return r.next();
	}

	public static void link(int staffid, int ordid)
	{
		try
		{
			if (areLinked(staffid, ordid))
			{
				System.err.println("Order " + ordid + " is already linked with staff " + staffid);
				return;
			}
			insert_stmnt.setInt(1, staffid);
			insert_stmnt.setInt(2, ordid);
			insert_stmnt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "SELECT * FROM STAFF_ORDERS WHERE StaffID=? AND OrderID=?";
		StaffOrders.select_stmnt = conn.prepareStatement(sql);

		sql = "INSERT INTO STAFF_ORDERS VALUES (?, ?)";
		StaffOrders.insert_stmnt = conn.prepareStatement(sql);
	}
}

/**
 * A class representing the table ORDER_PRODUCTS which is the link between the
 * tables ORDER and INVENTORY.
 */
class OrderItems
{
	/**
	 * Adds a new line to table ORDER_PRODUCTS
	 * 
	 * @param 1
	 *            OrderID: INTEGER
	 * @param 2
	 *            ProductID: INTEGER
	 * @param 3
	 *            ProductQuantity: INTEGER
	 */
	private static PreparedStatement insert_stmnt;
	/**
	 * Select all products from a particular order
	 * 
	 * @param 1
	 *            OrderID: INTEGER
	 */
	private static PreparedStatement select_of_order_stmnt;
	/**
	 * Delete one row of this table
	 * 
	 * @param 1
	 *            OrderID: INTEGER
	 * @param 2
	 *            ProductID: INTEGER
	 */
	private static PreparedStatement delete_stmnt;

	/**
	 * Select all ProductIDs and ProductQuanitites for a particular order
	 * 
	 * @param ord
	 *            The order whose products will be selected
	 * @return A mapping of ProductID->ProductQuantity of the particular order;
	 */
	public static Map<Integer, Integer> ofOrder(Order ord)
	{
		if (ord == null)
		{
			return null;
		}
		Map<Integer, Integer> tbr = new HashMap<>();
		try
		{
			select_of_order_stmnt.setInt(1, ord.id);
			ResultSet r = select_of_order_stmnt.executeQuery();
			while (r.next())
			{
				tbr.put(r.getInt(2), r.getInt(3));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
		return tbr;
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "INSERT INTO ORDER_PRODUCTS VALUES (?, ?, ?)";
		insert_stmnt = conn.prepareStatement(sql);

		sql = "SELECT * FROM ORDER_PRODUCTS WHERE OrderID = ?";
		select_of_order_stmnt = conn.prepareStatement(sql);

		sql = "DELETE FROM ORDER_PRODUCTS WHERE OrderID = ? AND ProductID = ?";
		delete_stmnt = conn.prepareStatement(sql);
	}

	/**
	 * Delete a row from the ORDER_PRODUCTS table.
	 * 
	 * @return True if the delete operation deleted one row
	 */
	public static boolean delete(int ordid, int prodid)
	{
		try
		{
			delete_stmnt.setInt(1, ordid);
			delete_stmnt.setInt(2, prodid);
			return delete_stmnt.executeUpdate() > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Add a new product to an order. Inserts a new row. Does not make any
	 * changes to other tables.
	 * 
	 * @param ordid
	 *            Order to add product to.
	 * @param prodid
	 *            The product to be added.
	 * @param amount
	 *            The quantity of the product to be added.
	 * @return True if successfuly inserted one row.
	 */
	public static boolean insert(int ordid, int prodid, int amount)
	{
		try
		{
			insert_stmnt.setInt(1, ordid);
			insert_stmnt.setInt(2, prodid);
			insert_stmnt.setInt(3, amount);
			return insert_stmnt.executeUpdate() > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}

/**
 * Class to handle conversions of {@link String} to and from
 * {@link java.sql.Date}.
 */
class OracleDateFormat implements VarConstraints<String>
{
	private static final DateTimeFormatter fromformat = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("d-MMM-yy").toFormatter();
	private static final DateFormat toformat = new SimpleDateFormat("d-MMM-yy");

	public static Date stringToDate(String s)
	{
		if (s == null || !isValid(s))
		{
			return null;
		}
		LocalDate ldate = LocalDate.parse(s, fromformat);
		return Date.valueOf(ldate);
	}

	public static String dateToString(Date d)
	{
		return d == null ? null : toformat.format(d);
	}

	private static final String oracle_date_regex = "^([0-9]{1,2})-([a-zA-Z]{3,3})-[0-9]{2,2}";
	private static final Pattern test_pattern = Pattern.compile(oracle_date_regex);
	private static final IntRangeCheck day_check = new IntRangeCheck(1, 32);
	private static final StringOptionsNoCase month_check = new StringOptionsNoCase("jan", "feb", "mar", "arp", "may",
			"jun", "jul", "aug", "sep", "oct", "nov", "dec");

	/**
	 * Check for validity of a string. For a string to be valid it must: <br/>
	 * a) Conform to the regular expression
	 * <code>[0-9]{1,2}-[a-zA-Z]{3,3}-[0-9]{2,2}</code> (the "default oracle
	 * DD-MON-YY format") <br/>
	 * b) 0 < DD < 32 <br/>
	 * c) MON is a valid month string (case insensitive).
	 * 
	 * @param s
	 *            The string to check if it is a valid date string.
	 * @return True iff all above conditions specified above have been met.
	 */
	public static boolean isValid(String s)
	{
		Matcher m = test_pattern.matcher(s);
		if (!m.matches())
		{
			return false;
		}
		int day = Integer.parseInt(m.group(1));
		String month = m.group(2);
		return m.matches() && month_check.verify(month) && day_check.verify(day);
	}

	/** Implemented statically */
	@Override
	public boolean verify(String s)
	{
		return isValid(s);
	}
}

/**
 * A class representing the table DELIVERIES and any operations that are
 * performed on it.
 */
class Delivery
{
	/**
	 * Inserts a new row in the table.
	 * 
	 * @param 1
	 *            ORDERID: INTEGER
	 * @param 2
	 *            FNAME:VARCHAR(30)
	 * @param 3
	 *            LNAME:VARCHAR(30)
	 * @param 4
	 *            HOUSE:VARCHAR(30)
	 * @param 5
	 *            STREET:VARCHAR(30)
	 * @param 6
	 *            CITY:VARCHAR(30)
	 * @param 7
	 *            DELIVERYDATE:DATE
	 */
	private static PreparedStatement insert_stmnt;

	/**
	 * Insert a new row in the table.
	 * 
	 * @param ord
	 *            Order to add delivery to
	 * @param fname
	 *            First name of recipient
	 * @param lname
	 *            Last name of recipient
	 * @param house
	 *            House No/Name of recipient
	 * @param street
	 *            Street name of recipient
	 * @param city
	 *            City of recipient
	 * @param date
	 *            Date of delivery
	 * @return True if successfully inserted one row.
	 */
	public static boolean insert(Order ord, String fname, String lname, String house, String street, String city,
			String date)
	{
		try
		{
			insert_stmnt.setInt(1, ord.id);
			insert_stmnt.setString(2, fname);
			insert_stmnt.setString(3, lname);
			insert_stmnt.setString(4, house);
			insert_stmnt.setString(5, street);
			insert_stmnt.setString(6, city);
			insert_stmnt.setDate(7, OracleDateFormat.stringToDate(date));
			int n = insert_stmnt.executeUpdate();
			return n > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "INSERT INTO DELIVERIES VALUES (?, ?, ?, ?, ?, ?, ?)";
		insert_stmnt = conn.prepareStatement(sql);
	}
}

/**
 * A class representing the COLLECTIONS table and any operations that can be
 * performed on it.
 */
class Collection
{
	/**
	 * Inserts a new row in the table.
	 * 
	 * @param 1
	 *            OrderID: INTEGER
	 * @param 2
	 *            FName: VARCHAR(30)
	 * @param 3
	 *            LName: VARCHAR(30)
	 * @param 4
	 *            CollectionDate: DATE
	 */
	private static PreparedStatement insert_stmnt;

	/**
	 * Make a new record for a new collection.
	 * 
	 * @param ord
	 *            The order to add collection to.
	 * @param fname
	 *            First name of person collecting
	 * @param lname
	 *            Last name of person collecting
	 * @param date
	 *            Date of collection.
	 * @return True if successfully added new row in COLLECTIONS table.
	 */
	public static boolean insert(Order ord, String fname, String lname, String date)
	{
		if (ord.type != "Collection")
		{
			System.out.println("[WARN] Adding a collection record for order " + ord.id + " that has type " + ord.type);
		}
		try
		{
			insert_stmnt.setInt(1, ord.id);
			insert_stmnt.setString(2, fname);
			insert_stmnt.setString(3, lname);
			insert_stmnt.setDate(4, OracleDateFormat.stringToDate(date));
			int n = insert_stmnt.executeUpdate();
			return n > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "INSERT INTO COLLECTIONS VALUES (?, ?, ?, ?)";
		insert_stmnt = conn.prepareStatement(sql);
	}
}

/**
 * A class representing the ORDERS table and any operations that can be
 * performed on the table and the orders.
 */
class Order
{
	private static final OracleDateFormat oracle_date_check = new OracleDateFormat();
	private static final IntRangeCheck completed_check = new IntRangeCheck(0, 2);
	private static final StringOptions type_check = new StringOptions("InStore", "Collection", "Delivery");

	/**
	 * Select one row from ORDERS table, based on OrderID.
	 * 
	 * @param 1
	 *            OrderID to select
	 */
	private static PreparedStatement select_stmnt;
	/**
	 * Insert a new row in the ORDERS table.
	 * 
	 * @param 1
	 *            OrderID: INTEGER (unique)
	 * @param 2
	 *            OrderType: VARCHAR(30) one of the following "InStore",
	 *            "Collection", "Delivery".
	 * @param 3
	 *            OrderCompleted: INTEGER either 1 or 0.
	 * @param 4
	 *            OrderPlaced: DATE the date that the order was placed on .
	 */
	private static PreparedStatement insert_stmnt;
	/** Get a new OrderID from the SQL SEQUENCE seq_ord */
	private static PreparedStatement getnewid_stmnt;
	/**
	 * Delete one row of the ORDERS table.
	 * 
	 * @param 1
	 *            OrderID: INTEGER the id of the order that will be deleted.
	 */
	private static PreparedStatement delete_stmnt;

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "SELECT * FROM ORDERS WHERE OrderID = ?";
		Order.select_stmnt = conn.prepareStatement(sql);

		sql = "INSERT INTO ORDERS VALUES (?, ?, ?, ?)";
		Order.insert_stmnt = conn.prepareStatement(sql);

		sql = "SELECT seq_ord.nextval FROM DUAL";
		Order.getnewid_stmnt = conn.prepareStatement(sql);

		sql = "DELETE FROM ORDERS WHERE OrderID=?";
		Order.delete_stmnt = conn.prepareStatement(sql);
	}

	private static int getNewId()
	{
		try
		{
			ResultSet r = getnewid_stmnt.executeQuery();
			r.next();
			return r.getInt(1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	private boolean was_deleted = false;
	public final int id;
	public final String type;
	public final int completed;
	public final Date date;
	private Map<Integer, Integer> products;

	private Order(int orderid, String type, int completed, Date date)
	{
		this.id = orderid;
		this.type = type;
		this.completed = completed;
		this.date = date;
	}

	private void initProducts()
	{
		this.products = OrderItems.ofOrder(this);
		if (this.products == null)
		{
			this.products = new HashMap<>();
		}
	}

	public Product addProduct(int prodid, int amount)
	{
		if (was_deleted)
		{
			throw new IllegalStateException("Order was deleted.");
		}
		if (this.hasProduct(prodid))
		{
			System.err.println("Attempted to add product " + prodid + " to order " + id + " that already has it.");
			return null;
		}
		Product p = Product.select(prodid);
		if (p == null)
		{
			System.err.println("[ERROR] Failed to selct product " + prodid + " when attempting to add to order " + id);
			return null;
		}
		if (amount > p.getStock())
		{
			System.err.printf(
					"[ERROR] Attempted to add quantity %d of product %d to order %d which is greater than product stock %d\n",
					amount, p.id, this.id, p.getStock());
			return null;
		}
		this.products.put(prodid, amount);
		if (!OrderItems.insert(this.id, prodid, amount))
		{
			System.err.println("[ERROR] Failed to add product " + prodid + " to order " + id);
			return null;
		}
		if (!p.updateStock(p.getStock() - amount))
		{
			System.err.println("[ERROR] Failed to decrease stock of product " + id + " when adding to order " + id);
			return null;
		}
		return p;
	}

	public boolean hasProduct(int prodid)
	{
		if (was_deleted)
		{
			throw new IllegalStateException("Order was deleted.");
		}
		if (this.products == null)
		{
			this.initProducts();
		}
		return this.products.containsKey(prodid);
	}

	/**
	 * Create a new instance given a ResultSet that was received from performing
	 * SELECT * FROM ORDERS query.
	 * 
	 * @param r
	 *            ResultSet from performing a query on ORDERS (or a table with
	 *            equal schema)
	 * @return A new instance of Order representing a row of ORDER table
	 */
	private static Order fromResultSet(ResultSet r)
	{
		try
		{
			int id = r.getInt(1);
			String type = r.getString(2);
			int completed = r.getInt(3);
			Date date = r.getDate(4);
			return new Order(id, type, completed, date);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Select a single row from the ORDERS table.
	 * 
	 * @param orderid
	 *            The id of the row to be selected (ProductID)
	 * @return A new instance of Order representing the row that was selected.
	 */
	public static Order select(int orderid)
	{
		try
		{
			select_stmnt.setInt(1, orderid);
			ResultSet r = select_stmnt.executeQuery();
			if (r.next())
			{
				return Order.fromResultSet(r);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Order insertInStoreOrder(String date)
	{
		return insert("InStore", 1, date);
	}

	public static Order insertCollectionOrder(String date)
	{
		return insert("Collection", 0, date);
	}

	public static Order insertDeliveryOrder(String date)
	{
		return insert("Delivery", 0, date);
	}

	/**
	 * Inserts a new row in the ORDERS table.
	 * 
	 * @param type
	 *            The type of the order to be inserted.
	 * @param completed
	 *            0 for incomplete order, 1 for completed.
	 * @param date
	 *            The date the order was placed on.
	 * @return An instance representing the order that was inserted.
	 */
	public static Order insert(String type, int completed, String date)
	{
		if (!(type_check.verify(type) && oracle_date_check.verify(date) && completed_check.verify(completed)))
		{
			return null;
		}
		final int newid = Order.getNewId();
		try
		{
			Order.insert_stmnt.setInt(1, newid);
			Order.insert_stmnt.setString(2, type);
			Order.insert_stmnt.setInt(3, completed);
			Order.insert_stmnt.setDate(4, OracleDateFormat.stringToDate(date));
			int n = Order.insert_stmnt.executeUpdate();
			if (n == 0)
			{
				return null;
			}
			return new Order(newid, type, completed, OracleDateFormat.stringToDate(date));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Delete the row corresponding to this instance in the ORDERS table. This
	 * instance should not be referenced after deletion.
	 * 
	 * @return True if one row was deleted.
	 */
	public boolean delete()
	{
		if (was_deleted)
		{
			throw new IllegalStateException("Order is already deleted.");
		}
		try
		{
			Order.delete_stmnt.setInt(1, this.id);
			int n = delete_stmnt.executeUpdate();
			if (n > 0)
			{
				return true;
			}
			System.err.println("[WARNING] Could not delete order " + this.id);
			was_deleted = true;
			return false;
		}
		catch (SQLException e)
		{
			System.err.println("[ERROR] Failed to delete order " + this.id);
			e.printStackTrace();
			was_deleted = true;
			return false;
		}
	}

	/**
	 * Delete a row from the ORDERS table.
	 * 
	 * @param orderid
	 *            The id of the order to be deleted.
	 * @return True if one row was deleted.
	 */
	public static boolean delete(int orderid)
	{
		int n = 0;
		try
		{
			delete_stmnt.setInt(1, orderid);
			n = delete_stmnt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return n > 0;
	}

	/**
	 * Cancels this order by updating stocks of all products of this order and
	 * then deletes the row of this order. References to this order are cascade
	 * deleted.
	 * 
	 * @return True if successful else false
	 */
	public boolean cancelOrder()
	{
		if (was_deleted)
		{
			throw new IllegalStateException("Order was already deleted.");
		}
		if (this.products == null)
		{
			this.initProducts();
		}
		for (Map.Entry<Integer, Integer> entry : this.products.entrySet())
		{
			Product prod = Product.select(entry.getKey());
			int quantity = entry.getValue();
			if (!prod.updateStock(prod.getStock() + quantity))
			{
				System.err.println(
						"[ERROR] Failed to update stock of product " + prod.id + " while canceling order " + id);
				return false;
			}
		}
		return this.delete();
	}
}

class Product
{
	/**
	 * Select a row from INVENTORY table
	 * 
	 * @param 1
	 *            ProductID: INTEGER
	 */
	private static PreparedStatement select_stmnt;
	/**
	 * Change the ProductStockAmount of a single product.
	 * 
	 * @param 1
	 *            ProductID: INTEGER
	 * @param 2
	 *            NewAmount: NUMBER(8,2)
	 */
	private static PreparedStatement update_stmnt;
	/** ProductID INTEGER */
	public final int id;
	/** ProductDesc VARCHAR(30) */
	public final String desc;
	/** ProductPrice NUMERIC(8,2) */
	public final double price;
	/** ProductStockAmount INTEGER */
	private int stockamount;

	private Product(int productid, String desc, double price, int stockamount)
	{
		this.id = productid;
		this.desc = desc;
		this.price = price;
		this.stockamount = stockamount;
	}

	public int getStock()
	{
		return stockamount;
	}

	private static Product fromResultSet(ResultSet r)
	{
		try
		{
			int productid = r.getInt(1);
			String desc = r.getString(2);
			double price = r.getDouble(3);
			int stockamount = r.getInt(4);
			return new Product(productid, desc, price, stockamount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns all information about a product if that exists in the database,
	 * null if it doesn't.
	 * 
	 * @param conn
	 * @param pid
	 *            The product id that will search for in the database
	 * @return A fully instantiated object if it exists in the database or a
	 *         null value if it does not exist
	 */
	public static Product select(int pid)
	{
		try
		{
			select_stmnt.setInt(1, pid);
			ResultSet r = select_stmnt.executeQuery();
			if (r.next())
			{
				return Product.fromResultSet(r);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public boolean updateStock(int newamount)
	{
		try
		{
			update_stmnt.setInt(1, newamount);
			update_stmnt.setInt(2, this.id);
			if (update_stmnt.executeUpdate() == 0)
			{
				System.err.println("[ERROR] Failed to update product (ID:" + this.id + ")");
				return false;
			}
			this.stockamount = newamount;
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void prepareStatements(Connection conn) throws SQLException
	{
		String sql = "SELECT * FROM INVENTORY WHERE ProductID=?";
		Product.select_stmnt = conn.prepareStatement(sql);

		sql = "UPDATE INVENTORY SET ProductStockAmount=? WHERE ProductID=?";
		Product.update_stmnt = conn.prepareStatement(sql);
	}
}

class StringOptionsNoCase implements VarConstraints<String>
{
	private HashSet<String> options = new HashSet<>();

	public StringOptionsNoCase(String... options)
	{
		for (String o : options)
		{
			this.options.add(o.toLowerCase());
		}
	}

	@Override
	public boolean verify(String t)
	{
		return options.contains(t.toLowerCase());
	}

}

/**
 * Constraint a String to be one of the allowed options. Allow only valid
 * options. Equality is case sensitive. For the case insensitive version see
 * {@link StringOptionsNoCase}
 */
class StringOptions implements VarConstraints<String>
{
	private Set<String> options = new HashSet<>();

	public StringOptions(String... options)
	{
		for (String o : options)
		{
			this.options.add(o);
		}
	}

	@Override
	public boolean verify(String s)
	{
		return options.contains(s);
	}
}

class IntRangeCheck implements VarConstraints<Integer>
{
	private final int min, max;

	IntRangeCheck(int minInclusive, int maxExclusive)
	{
		this.min = minInclusive;
		this.max = maxExclusive;
	}

	@Override
	public boolean verify(Integer x)
	{
		return x >= this.min && x < this.max;
	}
}

class LengthCheck implements VarConstraints<String>
{
	private final VarConstraints<Integer> len_check;

	public LengthCheck(int minInclusive, int maxExclusive)
	{
		len_check = new IntRangeCheck(minInclusive, maxExclusive);
	}

	@Override
	public boolean verify(String t)
	{
		return len_check.verify(t.length());
	}
}

/**
 * Verify that a variable is within the allowed constraints.
 * 
 * @param <T>
 *            The type of the variable to be constrained.
 */
interface VarConstraints<T>
{
	abstract boolean verify(T t);
}

class Assignment
{
	// @formatter:off
	private static final String menu = "MENU:\n"
			+ "(1) In-Store Purchases\n"
			+ "(2) Collection\n"
			+ "(3) Delivery\n"
			+ "(4) Biggest Sellers\n"
			+ "(5) Reserved Stock\n"
			+ "(6) Staff Life-Time Success\n"
			+ "(7) Staff Contribution\n"
			+ "(8) Employees of the Year\n"
			+ "(0) Quit";
	// @formatter:on

	/**
	 * @param conn
	 *            An open database connection
	 * @param productIDs
	 *            An array of productIDs associated with an order
	 * @param quantities
	 *            An array of quantities of a product. The index of a quantity
	 *            correspeonds with an index in productIDs
	 * @param orderDate
	 *            A string in the form of 'DD-Mon-YY' that represents the date
	 *            the order was made
	 * @param staffID
	 *            The id of the staff member who sold the order
	 */
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID)
	{
		Order ord = Order.insertInStoreOrder(orderDate);
		if (ord == null)
		{
			System.err.println("Failed to insert new order");
			return;
		}
		for (int i = 0; i < productIDs.length; ++i)
		{
			int pid = productIDs[i];
			int amount = quantities[i];
			Product p = ord.addProduct(pid, amount);
			if (p == null)
			{
				continue;
			}
			System.out.printf("Product ID %d stock is now %d.\n", p.id, p.getStock());
		}
		StaffOrders.link(staffID, ord.id);
	}

	/**
	 * @param conn
	 *            An open database connection
	 * @param productIDs
	 *            An array of productIDs associated with an order
	 * @param quantities
	 *            An array of quantities of a product. The index of a quantity
	 *            correspeonds with an index in productIDs
	 * @param orderDate
	 *            A string in the form of 'DD-Mon-YY' that represents the date
	 *            the order was made
	 * @param collectionDate
	 *            A string in the form of 'DD-Mon-YY' that represents the date
	 *            the order will be collected
	 * @param fName
	 *            The first name of the customer who will collect the order
	 * @param LName
	 *            The last name of the customer who will collect the order
	 * @param staffID
	 *            The id of the staff member who sold the order
	 */
	public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate,
			String collectionDate, String fName, String LName, int staffID)
	{

		Order ord = Order.insertCollectionOrder(orderDate);
		if (ord == null)
		{
			System.err.println("Failed to insert new order");
			return;
		}
		for (int i = 0; i < productIDs.length; ++i)
		{
			int pid = productIDs[i];
			int amount = quantities[i];
			Product p = ord.addProduct(pid, amount);
			if (p == null)
			{
				continue;
			}
			System.out.printf("Product ID %d stock is now %d.\n", p.id, p.getStock());
		}
		Collection.insert(ord, fName, LName, collectionDate);
		StaffOrders.link(staffID, ord.id);
	}

	/**
	 * @param conn
	 *            An open database connection
	 * @param productIDs
	 *            An array of productIDs associated with an order
	 * @param quantities
	 *            An array of quantities of a product. The index of a quantity
	 *            correspeonds with an index in productIDs
	 * @param orderDate
	 *            A string in the form of 'DD-Mon-YY' that represents the date
	 *            the order was made
	 * @param deliveryDate
	 *            A string in the form of 'DD-Mon-YY' that represents the date
	 *            the order will be delivered
	 * @param fName
	 *            The first name of the customer who will receive the order
	 * @param LName
	 *            The last name of the customer who will receive the order
	 * @param house
	 *            The house name or number of the delivery address
	 * @param street
	 *            The street name of the delivery address
	 * @param city
	 *            The city name of the delivery address
	 * @param staffID
	 *            The id of the staff member who sold the order
	 */
	public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate,
			String deliveryDate, String fName, String LName, String house, String street, String city, int staffID)
	{
		Order ord = Order.insertDeliveryOrder(orderDate);
		if (ord == null)
		{
			System.err.println("[ERROR] Failed to insert new order.");
			return;
		}
		for (int i = 0; i < productIDs.length; ++i)
		{
			int pid = productIDs[i];
			int amount = quantities[i];
			Product p = ord.addProduct(pid, amount);
			if (p == null)
			{
				continue;
			}
			System.out.printf("Product ID %d stock is now %d.\n", p.id, p.getStock());
		}
		Delivery.insert(ord, fName, LName, house, street, city, deliveryDate);
		StaffOrders.link(staffID, ord.id);
	}

	/**
	 * Prepend GBP symbol and show two fixed decimals
	 * 
	 * @param x
	 *            The number to be formatted to
	 * @return Formatted string
	 */
	private static String gbpFormat(double x)
	{
		return "$" + String.format("%.2f", x);
	}

	/**
	 * @param conn
	 *            An open database connection
	 */
	public static void option4(Connection conn)
	{
		final String fstr = "%-10s %-30s %10s\n";
		System.out.printf(fstr, "ProductID,", "ProductDesc,", "TotalValueSold");
		try
		{
			ResultSet r = op4_select.executeQuery();
			while (r.next())
			{
				System.out.printf(fstr, r.getInt("ProductID") + ",", r.getString("ProductDesc") + ',',
						gbpFormat(r.getDouble("ProductRevenue")));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Select all from view VIEW_REVENUE_PER_PRODUCT.
	 */
	private static PreparedStatement op4_select;
	/**
	 * Get all orders of type Collection that have yet to be completed and are
	 * at least 8 days old.
	 * 
	 * @param 1
	 *            DATE The date relative to which all orders will be checked.
	 */
	private static PreparedStatement op5_select, op6_select, op7_select, op8_select;

	private static boolean prepareStatements(Connection conn)
	{
		try
		{
			Order.prepareStatements(conn);
			OrderItems.prepareStatements(conn);
			StaffOrders.prepareStatements(conn);
			Staff.prepareStatements(conn);
			Collection.prepareStatements(conn);
			Product.prepareStatements(conn);
			Delivery.prepareStatements(conn);

			String sql = "SELECT * FROM v_opt4";
			op4_select = conn.prepareStatement(sql);

			//@formatter:off
			sql = "SELECT OrderID, CollectionDate"
					+ " FROM (SELECT * FROM ORDERS WHERE OrderType = 'Collection' AND OrderCompleted = 0)"
						+ " INNER JOIN Collections USING (OrderID)"
					+ " WHERE ? - CollectionDate >= 8";
			//@formatter:on
			op5_select = conn.prepareStatement(sql);

			sql = "SELECT * FROM v_opt6";
			op6_select = conn.prepareStatement(sql);

			sql = "SELECT * FROM v_opt7";
			op7_select = conn.prepareStatement(sql);

			//@formatter:off
			sql = "SELECT FName, LName"
					+ " FROM v_sub_opt8"
					+ " WHERE OrderYear = ?";
			//@formatter:on
			op8_select = conn.prepareStatement(sql);
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param conn
	 *            An open database connection
	 * @param date
	 *            The target date to test collection deliveries against
	 */
	public static void option5(Connection conn, String date)
	{
		try
		{
			op5_select.setDate(1, OracleDateFormat.stringToDate(date));
			ResultSet r = op5_select.executeQuery();
			while (r.next())
			{
				int orderid = r.getInt("OrderID");
				Order ord = Order.select(orderid);
				if (ord == null)
				{
					System.err.println("Failed select order with id " + orderid);
					continue;
				}
				if (ord.cancelOrder())
				{
					System.out.println("Order " + orderid + " has been cancelled (date:"
							+ OracleDateFormat.dateToString(r.getDate("CollectionDate")) + ")");
				}
				else
				{
					System.out.println("Failed to cancel order " + ord.id);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param conn
	 *            An open database connection
	 */
	public static void option6(Connection conn)
	{
		final String fstr = "%-30s %10s\n";
		try
		{
			ResultSet r = op6_select.executeQuery();
			if (r.next())
			{
				System.out.printf(fstr, "EmployeeName,", "TotalValueSold");
				do
				{
					String name = r.getString("FName") + ' ' + r.getString("LName") + ',';
					System.out.printf(fstr, name, gbpFormat(r.getDouble("StaffSales")));
				} while (r.next());
			}
			else
			{
				System.out.println("[INFO] No staff that match the criteria.");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param conn
	 *            An open database connection
	 */
	public static void option7(Connection conn)
	{
		List<Integer> staffs = new ArrayList<>(), products = new ArrayList<>();
		Map<Integer, String> names = new HashMap<>();
		Map<Integer, List<Integer>> q = new HashMap<>();
		try
		{
			ResultSet r = op7_select.executeQuery();
			while (r.next())
			{
				int staffid = r.getInt("StaffID");
				if (!staffs.contains(staffid))
				{
					staffs.add(staffid);
					names.put(staffid, r.getString("FName") + ' ' + r.getString("LName"));
					q.put(staffid, new ArrayList<Integer>());
				}
				int prodid = r.getInt("ProductID");
				if (!products.contains(prodid))
				{
					products.add(prodid);
				}
				int quantity = r.getInt("ProductSoldCount");
				q.get(staffid).add(quantity);
			}
			if (products.isEmpty())
			{
				System.out.println("[INFO] No best selling products.");
				return;
			}
			final String fstr__1 = "%-60s";
			final String fstr__2 = " %-20s";
			System.out.printf(fstr__1, "EmployeeName,");
			for (int i = 0; i < products.size() - 1; ++i)
			{
				System.out.printf(fstr__2, "Product " + products.get(i) + ',');
			}
			System.out.printf(fstr__2 + '\n', "Product " + products.get(products.size() - 1));

			for (int i = 0; i < staffs.size(); ++i)
			{
				int staffid = staffs.get(i);
				System.out.printf(fstr__1, names.get(staffid));
				List<Integer> quantities = q.get(staffid);
				for (int j = 0; j < quantities.size() - 1; ++j)
				{
					System.out.printf(fstr__2, quantities.get(j) + ",");
				}
				System.out.printf(fstr__2 + '\n', quantities.get(quantities.size() - 1));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param conn
	 *            An open database connection
	 * @param year
	 *            The target year we match employee and product sales against
	 */
	public static void option8(Connection conn, int year)
	{
		try
		{
			op8_select.setInt(1, year);
			ResultSet r = op8_select.executeQuery();
			while (r.next())
			{
				System.out.println(r.getString("FName") + ' ' + r.getString("LName"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Used during development to connect to Oracle DB run on localhost. You
	 * should only need to fetch the connection details once
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection2() throws ClassNotFoundException, SQLException
	{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		// DriverManager.registerDriver (new oracle.jdbc.OracleDriver());

		final String user = "c##jaccy";
		final String passwrd = "jacc3421";
		return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:ORCL256", user, passwrd);
	}

	/**
	 * You should only need to fetch the connection details once. Original
	 * method used to create connection with prod Oracle DB run on
	 * csv.warwick.ac.uk
	 * 
	 * @return Established connection or if error occurred null is returned
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection() throws ClassNotFoundException, SQLException
	{
		// User and password should be left blank. Do not alter!
		String user = "";
		String passwrd = "";
		Connection conn;

		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@arryn-ora-prod-db-1.warwick.ac.uk:1521:cs2db", user,
				passwrd);
		return conn;

	}

	public static void main(String args[]) throws SQLException, IOException
	{
		// try-with-resources statement ensures that each resource is closed at
		// the end of the statement (Oracle,
		// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
		try (Connection conn = getConnection2(); Scanner scnr = new Scanner(System.in))
		{
			if (!prepareStatements(conn))
			{
				System.err.println("[CRITICAL ERROR] FAILED TO PREPARE STATEMENTS");
				return;
			}
			int option;
			for (;;)
			{
				System.out.println(menu);
				option = getSomeInt(scnr, "Enter your choice: ");
				if (option == 0)
				{
					break;
				}
				switch (option)
				{
				case 1:
					handleOption1(conn, scnr);
					break;
				case 2:
					handleOption2(conn, scnr);
					break;
				case 3:
					handleOption3(conn, scnr);
					break;
				case 4:
					handleOption4(conn, scnr);
					break;
				case 5:
					handleOption5(conn, scnr);
					break;
				case 6:
					handleOption6(conn, scnr);
					break;
				case 7:
					handleOption7(conn, scnr);
					break;
				case 8:
					handleOption8(conn, scnr);
					break;
				default:
					handleOptionInvalid();
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.err.println("[ERROR] Driver could not be loaded");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.err.println("[ERROR] Error while retrieving connection");
		}
	}

	private static void handleOptionInvalid()
	{
		System.out.println("INVALID OPTION GIVEN! PLEASE TRY AGAIN");
	}

	private static void handleOption8(Connection conn, Scanner s)
	{
		option8(conn, getSomeInt(s, "Enter the year: "));
	}

	private static void handleOption7(Connection conn, Scanner s)
	{
		option7(conn);
	}

	private static void handleOption6(Connection conn, Scanner s)
	{
		option6(conn);

	}

	private static void handleOption5(Connection conn, Scanner s)
	{
		option5(conn, getSomeOracleDate(s, "Enter the date: "));
	}

	private static void handleOption4(Connection conn, Scanner s)
	{
		option4(conn);
	}

	private static String getSomeVarChar(Scanner s, String prompt, int minlen, int maxlen)
	{
		String in;
		System.out.print(prompt);
		VarConstraints<String> lencheck = new LengthCheck(minlen, maxlen + 1);
		while (!lencheck.verify(in = s.nextLine()))
		{
			System.out.println(
					"INVALID STRIRN GIVEN, must be between " + minlen + " and " + maxlen + " characters (inclusive)");
			System.out.print(prompt);
		}
		return in;
	}

	/**
	 * Prompt the user for a string enough times to input a string of at most
	 * maxlen characters and at least length of 1. (Returned string is not
	 * empty) Input is delimited on newlines.
	 * 
	 * @param s
	 *            Scanner to read lines from.
	 * @param prompt
	 *            What will be shown every time they are requested to give a
	 *            string.
	 * @param maxlen
	 *            The maximum length of string to be returned.
	 * @return A non-empty string of length at most maxlen
	 */
	private static String getSomeVarChar(Scanner s, String prompt, int maxlen)
	{
		return getSomeVarChar(s, prompt, 1, maxlen);
	}

	private static void handleOption3(Connection conn, Scanner s)
	{
		Queue<Integer> amounts = new LinkedList<>();
		Queue<Integer> products = new LinkedList<>();
		do
		{
			int pid = getSomeInt(s, "Enter a product ID: ");
			if (products.contains(pid))
			{
				System.out.println("Product already added to order previously.");
				continue;
			}
			Product prod = Product.select(pid);
			if (prod == null)
			{
				System.out.println("No product exists with such ID: " + pid);
				continue;
			}
			if (prod.getStock() == 0)
			{
				System.out.println("Selected product is out of stock!");
				continue;
			}
			int amount = getSomeInt(s, "Enter the quantity sold: ");
			if (amount > prod.getStock())
			{
				System.out.println("Product not ordered, not enough stock, only " + prod.getStock() + " left");
				continue;
			}
			if (amount <= 0)
			{
				System.out.println("Product quantity must be a positive integer.");
				continue;
			}
			amounts.add(amount);
			products.add(prod.id);
		} while (getSomeYesNo(s, "Is there another product in the order?: "));
		if (products.isEmpty())
		{
			System.out.println("No products given, no order was placed.");
			return;
		}
		String orderDate = getSomeOracleDate(s, "Enter the date sold: ");
		String deliveryDate = getSomeOracleDate(s, "Enter the date of delivery: ");
		String fname = getSomeVarChar(s, "Enter the first name of the recipient:  ", 30);
		String lname = getSomeVarChar(s, "Enter the last name of the recipient: ", 30);
		String house = getSomeVarChar(s, "Enter the house name/no: ", 30);
		String street = getSomeVarChar(s, "Enter the street: ", 30);
		String city = getSomeVarChar(s, "Enter the City: ", 30);
		int staffid;
		while (!Staff.exists(staffid = getSomeInt(s, "Enter your staff ID: ")))
		{
			System.out.println("Staff with ID " + staffid + " does not exist");
		}
		// convert queues to arrays
		int[] quantities = amounts.stream().mapToInt(Integer::intValue).toArray();
		int[] productIDs = products.stream().mapToInt(Integer::intValue).toArray();
		option3(conn, productIDs, quantities, orderDate, deliveryDate, fname, lname, house, street, city, staffid);
	}

	private static boolean getSomeYesNo(Scanner s, String prompt)
	{
		final StringOptionsNoCase yes_check = new StringOptionsNoCase("yes", "ye", "y"),
				no_check = new StringOptionsNoCase("no", "n");
		while (true)
		{
			System.out.print(prompt);
			String in = s.nextLine();
			if (no_check.verify(in))
			{
				return false;
			}
			if (yes_check.verify(in))
			{
				return true;
			}
			System.out.println("INVALID YES/NO");
		}
	}

	private static int getSomeInt(Scanner s, String prompt)
	{
		while (true)
		{
			try
			{
				System.out.print(prompt);
				int r = s.nextInt();
				s.nextLine();
				return r;
			}
			catch (InputMismatchException e)
			{
				s.nextLine();
				System.out.println("INVALID STRING! INTEGER EXPECTED.");
			}
		}
	}

	private static String getSomeOracleDate(Scanner s, String prompt)
	{
		System.out.print(prompt);
		String in;
		while (!OracleDateFormat.isValid(in = s.nextLine()))
		{
			System.out.println("INVALID STRING GIVEN! FORMAT: D-Mon-YY (case insensitive)");
			System.out.print(prompt);
		}
		return in;
	}

	private static void handleOption2(Connection conn, Scanner s)
	{
		Queue<Integer> amounts = new LinkedList<>();
		Queue<Integer> products = new LinkedList<>();
		do
		{
			int pid = getSomeInt(s, "Enter a product ID: ");
			if (products.contains(pid))
			{
				System.out.println("Product already added to order previously.");
				continue;
			}
			Product prod = Product.select(pid);
			if (prod == null)
			{
				System.out.println("No product exists with such ID: " + pid);
				continue;
			}
			if (prod.getStock() == 0)
			{
				System.out.println("Selected product is out of stock!");
				continue;
			}
			int amount = getSomeInt(s, "Enter the quantity sold: ");
			if (amount > prod.getStock())
			{
				System.out.println("Product not ordered, not enough stock, only " + prod.getStock() + " left");
				continue;
			}
			if (amount <= 0)
			{
				System.out.println("Product quantity must be a positive integer.");
				continue;
			}
			amounts.add(amount);
			products.add(prod.id);
		} while (getSomeYesNo(s, "Is there another product in the order?: "));
		if (products.isEmpty())
		{
			System.out.println("No products given, no order was placed.");
			return;
		}
		String orderDate = getSomeOracleDate(s, "Enter the date sold: ");
		String collectionDate = getSomeOracleDate(s, "Enter the date of collection: ");
		String fname = getSomeVarChar(s, "Enter the first name of the collector: ", 30);
		String lname = getSomeVarChar(s, "Enter the last name of the collector: ", 30);
		int staffid;
		while (!Staff.exists(staffid = getSomeInt(s, "Enter your Staff ID: ")))
		{
			System.out.println("Staff with ID " + staffid + " does not exist");
		}
		// convert queues to arrays
		int[] quantities = amounts.stream().mapToInt(Integer::intValue).toArray();
		int[] productIDs = products.stream().mapToInt(Integer::intValue).toArray();
		option2(conn, productIDs, quantities, orderDate, collectionDate, fname, lname, staffid);
	}

	private static void handleOption1(Connection conn, Scanner s)
	{
		Queue<Integer> amounts = new LinkedList<>();
		Queue<Integer> products = new LinkedList<>();
		do
		{
			int pid = getSomeInt(s, "Enter a product ID: ");
			if (products.contains(pid))
			{
				System.out.println("Product already added to order previously.");
				continue;
			}
			Product prod = Product.select(pid);
			if (prod == null)
			{
				System.out.println("No product exists with such ID: " + pid);
				continue;
			}
			if (prod.getStock() == 0)
			{
				System.out.println("Selected product is out of stock!");
				continue;
			}
			int amount = getSomeInt(s, "Enter the quantity sold: ");
			if (amount > prod.getStock())
			{
				System.out.println("Product not ordered, not enough stock, only " + prod.getStock() + " left");
				continue;
			}
			if (amount <= 0)
			{
				System.out.println("Product quantity must be a positive integer.");
				continue;
			}
			amounts.add(amount);
			products.add(prod.id);
		} while (getSomeYesNo(s, "Is there another product in the order?: "));
		if (products.isEmpty())
		{
			System.out.println("No products given, no order was placed.");
			return;
		}
		String date = getSomeOracleDate(s, "Enter the date sold: ");
		int staffid;
		while (!Staff.exists(staffid = getSomeInt(s, "Enter your Staff ID: ")))
		{
			System.out.println("Invalid Staff ID given");
		}
		// convert queues to arrays
		int[] quantities = amounts.stream().mapToInt(Integer::intValue).toArray();
		int[] productIDs = products.stream().mapToInt(Integer::intValue).toArray();
		option1(conn, productIDs, quantities, date, staffid);
	}
}
