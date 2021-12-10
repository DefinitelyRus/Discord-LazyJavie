package home;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.exception.ExceptionUtils;

import sql_queries.noDB_autofix;

/**
 * This serves as the connector between the program and the local SQL server.
 * Given the complexity of this program, we decided to put all MySQL-related
 * code in this module so all we have to do is call a function if we ever
 * need to get records from the database or update a table.
 * @author DefinitelyRus
 */
public class SQLconnector {
	//Initializing variables
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String USER_DOCS_FOLDER_PATH = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	static final String DB_ADDRESS = "jdbc:sqlite:" + USER_DOCS_FOLDER_PATH + "\\lazyjavie.db";
	static final String DEFAULT_ADDRESS = "jdbc:sqlite:" + USER_DOCS_FOLDER_PATH + "\\lazyjavie.db";
	static final String DB_LOGIN_ID = "root";
	static String dbPass = "password"; //Placeholder. I'm not that dumb.
	static String ps_dir = USER_DOCS_FOLDER_PATH + "\\lazyjavie_token.txt";
	
	private static Connection conn = null;
	public static Connection getConn() throws SQLException {
	    if (conn == null || conn.isClosed()) {
	    	try {Class.forName("org.sqlite.JDBC");}
	    	catch (ClassNotFoundException e) {P.print(ExceptionUtils.getStackTrace(e));}
	    	conn = DriverManager.getConnection(DB_ADDRESS, DB_LOGIN_ID, dbPass);
	    }
	    return conn;
    }
	
	//-------------------------UPDATE-------------------------
	/**Can be used to add, delete, or modify tables and its contents. This method does not return any values.
	 * If you wish to retrieve records from the database, use SQLconnector.get().
	 * 
	 * <p>Usage:
	 * <br>update("UPDATE [table] SET [new value] WHERE [column] = [old value]", true) <p>
	 * <p>This will set all values in a column to 0 where the value is 10.<p>
	 * 
	 * @param query An SQL query like UPDATE-WHERE-SET. Note that you can only use this for one statement at a time.
	 * @param tp Boolean whether running this function should print what it's currently doing.
	 */
	public static void update(String query, boolean tp) {
		//Initialization
		String exeScript = query;
		dbPass = getPass();
		
		try {
			if (tp == true) {P.print("|[SQLcA-1] Starting connection with the database...");}
			Connection connection = getConn();
			
			if (tp == true) {P.print("|[SQLcA-2] Creating statement...");}
			Statement statement = connection.createStatement();
			
			if (tp == true) {P.print("|[SQLcA-3] Executing SQL script...");}
			statement.execute(exeScript);
			
			if (tp == true) {P.print("|[SQLcA-4] Done!");}
			connection.close();
		}
		catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString());}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
	}
	
	//-------------------------GET VALUE-------------------------
	/**Returns a record from the database.
	 * 
	 * <p>Usage:
	 * <br>SQLconnector.get("SELECT * FROM database.table WHERE username IS NOT NULL", "username")
	 * <br>This will return the last item in the table that fits the condition.<p>
	 * 
	 * @param query An SQL query like SELECT-WHERE. Note that you can only use this for one statement at a time.
	 * @param toReturn A column or value you want returned.
	 * @param tp Boolean whether running this function should print what it's currently doing.
	 * @return The last or only string value that qualify the SQL query.
	 * @see getList(String query, String toReturn, boolean tp) - for getting a list of values.
	 */
	public static String get(String query, String toReturn, boolean tp) {
		//Initialization
		String returnMsg = null;
		dbPass = getPass();
		
		try {
			//Starts a connection to the database using the JDBC driver.
			if (tp == true) {P.print("|[SQLcB-1] Starting connection with the database...");}
			Connection connection = getConn();
			
			//Creates a statement
			if (tp == true) {P.print("|[SQLcB-2] Creating statement...");}
			Statement statement = connection.createStatement();
			
			//Starts the SQL query.
			if (tp == true) {P.print("|[SQLcB-3] Executing SQL script...");}
			ResultSet results = statement.executeQuery(query);
			
			//Returns the requested record.
			if (tp == true) {P.print("|[SQLcB-4] Outputting results...");}
			while (results.next()) {returnMsg = results.getString(toReturn);}

			connection.close();
			return returnMsg;
		}
		catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString()); return "Error encountered: " + e;}
		catch (Exception e) {
			SQLconnector.callError(e); P.print(e.toString());
			return "Error encountered: " + e;
		}
	}
	
	//-------------------------GET LIST-------------------------
	/**Returns a list of records from the database.
	 * 
	 * <p>Usage:
	 * <br>SQLconnector.get("select * from database.table where username is not null", "username")
	 * <br>This will return all the items in the table that fit this condition into a linked list.<p>
	 * 
	 * @param query An SQL query like SELECT-WHERE. Note that you can only use this for one statement at a time.
	 * @param toReturn A column or value you want returned.
	 * @param tp Boolean whether running this function should print what it's currently doing.
	 * @return A list (LinkedList) of results according to the SQL query and toReturn (column) parameters.
	 * @see get(String query, String toReturn, boolean tp) - for getting single values.
	 */
	public static LinkedList<String> getList(String query, String toReturn, boolean tp) {
		//Initialization
		LinkedList<String> returnList = new LinkedList<String>(); returnList.add("");
		String exeScript = query;
		dbPass = getPass();
		
		try {
			//Starts a connection to the database using the JDBC driver.
			if (tp == true) {P.print("|[SQLcB-1] Starting connection with the database...");}
			Connection connection = getConn();
			
			//Creates a statement
			if (tp == true) {P.print("|[SQLcB-2] Creating statement...");}
			Statement statement = connection.createStatement();
			
			//Starts the SQL query.
			if (tp == true) {P.print("|[SQLcB-3] Executing SQL script...");}
			ResultSet results = statement.executeQuery(exeScript);
			
			//Returns the requested record.
			if (tp == true) {P.print("|[SQLcB-4] Outputting results...");}
			while (results.next()) {returnList.add(results.getString(toReturn));}

			connection.close();
			return returnList;
		}
		catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString()); return returnList;}
		catch (Exception e) {
			SQLconnector.callError(e); P.print(e.toString());
			return returnList;
		}
	}
	
	//-------------------------AUTO CREATE DB-------------------------
	/**
	 * In the event that the database is missing, this function is called.
	 * 
	 * <p>NOTE: COMPILED .JAR WILL CRASH IF THIS METHOD IS CALLED.
	 * <br>Reason: NoDB_autofix.sql cannot be found</p>
	 * 
	 * 
	 * <p>ANOTHER NOTE: The note above only applies if a scanner is used to search for a file.
	 * As a substitute, the entire SQL sequence has been stored as a string in Java.
	 * This is subject to change.</p>
	 */
	public static void NoDBfixer(String sqlType) {
		try {
			P.print("\nStarting automatic database setup...");
			P.print("|[SQLcD-1] Running fixer script...");
			Statement statement = getConn().createStatement();
			for (String line : noDB_autofix.get(sqlType)) {
				P.print("|SQL Statement: " + line);
				try {statement.execute(line);}
				catch (SQLException e2) {P.print("Error encountered (skipping): " + e2.toString());}
			}
			P.print("|[SQLcD-4] New database created.\n");
			
			statement.getConnection().close();
		}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
	}
	
	//-------------------------UPDATE EXTERNAL-------------------------
	/**<p>In the event that the target database is different, this function is called instead of update().<p>
	 * @param query An SQL query like SELECT-WHERE. Note that you can only use this for one statement at a time.
	 * @param dbAddress The target database address.
	 * @param tp (Boolean) Whether running this function should print what it's currently doing.
	 * @throws LoginException
	 * @throws SQLException
	 */
	public static void updateExternal(String query, String dbAddress, boolean tp) {
		
		dbPass = getPass();
		
		try {
			//Starts a connection to the database using the JDBC driver.
			if (tp == true) {P.print("[SQLcB] Starting connection with the database...");}
			Connection connection = DriverManager.getConnection(dbAddress, DB_LOGIN_ID, dbPass);
			
			//Creates a statement
			if (tp == true) {P.print("|Creating statement...");}
			Statement statement = connection.createStatement();
			
			//Starts the SQL query.
			if (tp == true) {P.print("|Executing SQL script...");}
			statement.execute(query);
			
			//Closes the connection to the database.
			connection.close();
		}
		catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString());}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
	}
	
	//-------------------------CALL ERROR-------------------------
	/**<p>Saves the error message to the database for later debugging. Note that this is saved along with the discord bot's settings and saved data.
	 * <br><br>Usage:
	 * <br>catch (Exception e) callError(Except, ExceptionUtils.getStackTrace(e));
	 * <br><br>Shortcut with print:
	 * <br>SQLconnector.callError(e); P.print(e.toString());</p>
	 * 
	 * @param exception - The throwable exception that needs to be saved in database.
	 */
	public static void callError(Throwable exception) {

		String errorLabel = exception.getMessage();
		String stackTrace = ExceptionUtils.getStackTrace(exception);
		
		if (!(exception instanceof RejectedExecutionException))
			P.print("\nError Received: \n" + stackTrace);
		
		String exeScript = "insert into errorlog (err_type, err_stacktrace, eventdate, appver) values ('" + errorLabel + "', '" + stackTrace + "', datetime(), '" +Bot.VERSION+ "');";
		dbPass = getPass();
		
		try {
			Connection connection = getConn();
			Statement statement = connection.createStatement();
			statement.execute(exeScript);
			connection.close();
		}
		catch (SQLException e) {P.print("[SQLconnector] callError() failed. Please send the error code to the developer.");}
		catch (Exception e) {P.print("[SQLconnector] callError() failed. Please send the error code to the developer.");}
	}
	
	//-------------------------GET ROW & COLUMN SIZE-------------------------
	/**
	 * Gets the number of rows and columns in a given table.
	 * @param tableName The target table.
	 * @return int[rows, columns]
	 */
	public static int[] getXY(String tableName) {
		int x = 0, y = 0;
		final String getX = "select count(*) from " +tableName+ ";";
		final String getY = "select * from " +tableName+ " where 1=2;";
		dbPass = getPass();
		
		try {
			//Starts a connection to the database using the JDBC driver.
			Connection connection = getConn();
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(getX);
			
			//Gets the number of rows.
			while (results.next()) {x = Integer.valueOf(results.getString("count(*)"));}
			
			//Gets the number of columns.
			results = statement.executeQuery(getY);
			y = results.getMetaData().getColumnCount();

			connection.close();
		} catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString());}
		int[] XY = {x, y};
		return XY;
	}
	
	//-------------------------GET RESULT SET-------------------------
	public static Connection con = null;
	/**Primarily used to get data outside of individual tables or about the table themselves.
	 * 
	 * @param query An SQL query like SELECT-WHERE. Note that you can only use this for one statement at a time.
	 * @return A ResultSet object containing data about the query.
	 * @throws SQLException 
	 */
	public static ResultSet getResultSet(String query) throws SQLException {
		con = getConn();
		ResultSet resultSet = con.createStatement().executeQuery(query);
		return resultSet;
	}
	
	//-------------------------GET DATABASE PASSWORD-------------------------
	/**
	 * Gets the password for the database from a locally-stored text file.
	 * @return The database's password.
	 */
	public static String getPass() {
		try {
			String dbpass = "";
			File file = new File(ps_dir);
			Scanner reader = new Scanner(file);
			
			//Looks for the password on the second line of the text file.
			int i = 0; //index
		    while (reader.hasNextLine() && i >= 1)
				{dbpass = reader.nextLine(); i++;}
		    
		    reader.close();
		    return dbpass;
		}
		
		catch (FileNotFoundException e) {
			P.print("[SQLconnector] lazyjavie_token.txt is missing from target directory.");
			String dbpass = "";
			
			try {
				P.print("|Attempting to create a new file at " + ps_dir + "...");
				File file = new File(ps_dir);
				file.createNewFile();

				P.print("|Attempting to write on file " + ps_dir + "...");
				FileWriter writer = new FileWriter(file);
				writer.write("[REPLACE THIS LINE WITH YOUR BOT'S TOKEN. ALSO REMOVE THE BRACKETS.]\n[REPLACE THIS LINE WITH YOUR SQL SERVER'S PASSWORD (IF ANY). ALSO REMOVE THE BRACKETS.]");
				writer.close();
				
				P.print("lazyjavie_token.txt was created automatically. Please enter your bot token and password here: " + ps_dir);
				P.print("[!] This console will automatically close in 30 seconds... [!]");
				TimeUnit.SECONDS.sleep(30);
				System.exit(0);
			} catch (Exception e2) {}
			
			return dbpass;
		}
		
		catch (Exception e) {
			P.print(ExceptionUtils.getStackTrace(e));
			String dbpass = "";
			return dbpass;
		}
	}
	
	//-------------------------DATABASE EXISTENCE CHECK-------------------------
	/**
	 * Automatically checks whether lazyjavie.db exists. It returns true only if
	 * @return True if all checks are passed. False if any of the checks fail.
	 */
	public static boolean dbCheck() {
		//Initialization
		dbPass = getPass();
		String query1 = "create table existence_check (id int)";
		String query2 = "select count(id) from existence_check";
		String query3 = "drop table existence_check";
		try {
			//Starts a connection to the database using the JDBC driver.
			P.print("[SQLc - dbCheck] Starting connection with the database...");
			Connection connection = getConn();
			
			//Creates a statement
			P.print("|Creating statement...");
			Statement statement = connection.createStatement();
			
			//Pre-drops potentially already-existing table. This happens when write test fails.
			//The table is created during write test but is dropped after read test because the read test uses the table.
			//TODO Inspect - What does this do? Is this necessary?
			try {statement.execute(query3);}
			catch (SQLException e2) {}

			//Write test
			P.print("|Executing SQL write test...");
			try {statement.execute(query1);}
			catch (SQLException e2) {P.print("Write test failed: " + e2.toString()); return false;}
			
			//Read test
			P.print("|Executing SQL read test...");
			try {@SuppressWarnings("unused")
			String s = statement.executeQuery(query2).getString("count(id)");}
			catch (SQLException e2) {P.print("Read test failed: " + e2); return false;}
			
			//Returns the requested record.
			P.print("lazyjavie.db exists, fully readable and writable.");
			
			//Closes the connection then returns the result.
			connection.close();
			return true;
		}
		catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString()); return false;}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString()); return false;}
		finally {
			try {getConn().close();}
			catch (SQLException e) {SQLconnector.callError(e); P.print(e.toString());}
		}
	}
	
	//-------------------------PRINT SHORTCUT-------------------------
	/**
	 * Identical in function to System.out.println().
	 * This function was made to shorten the process of printing text to console.
	 * @param s String to print.
	 */
	@SuppressWarnings("unused")
	private static void print(String s) {System.out.println(s);}
}