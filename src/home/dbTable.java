package home;

import java.awt.Choice;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.exception.ExceptionUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class dbTable {
	
	//-------------------------UPDATE TABLE DISPLAY-------------------------
	/**Returns the contents of the table as a 2D object array.
	 * @param tableList The list of tables in the database.
	 * @param tableGrid The target JTable object to update.
	 * @return A 2D array containing the contents of the table.
	 */
	@SuppressWarnings("serial")
	public static void updateTableDisplay(Choice tableList, JTable tableGrid) {

		//Initialization
		boolean gotMembers = false;
		String table = tableList.getSelectedItem();
		if (table.equals("- Select table -")) {LazyJavieUI.getEntryCounterLabel().setText("No table selected."); return;} //Checks if there are no items selected.
		else if (table.equals("members")) gotMembers = getMembers();
		int xCount = 0, yCount = 0;
		int xy[] = SQLconnector.getXY(table);
		xCount = xy[0];
		yCount = xy[1];
		
		try {
			//Creates a list proportional in size to the number of columns.
			//This determines whether a column is editable or not.
			//This table is display-only so its contents are all false.
			List<Boolean> columnEditablesList = new LinkedList<>();
			for (int c = 1; c <= yCount; c++) {columnEditablesList.add(false);}
			
			
			//Creates a connection to the database, then gets a result set for an entire table.
			Connection con = DriverManager.getConnection(SQLconnector.DB_ADDRESS, SQLconnector.DB_LOGIN_ID, SQLconnector.getPass());
			ResultSet results = con.createStatement().executeQuery("select * from " + table);
			
			//Updates the table UI.
			tableGrid.setModel(new DefaultTableModel(getTableContentsByRow(table), getColumnHeaders(yCount, results)) {
					boolean[] columnEditables = P.toBooleanArray(columnEditablesList);
					public boolean isCellEditable(int row, int column) {return columnEditables[column];}
				}
			);
			
			//Closes the connection to the database.
			con.close();
			
			//Grammar.
			if (gotMembers == true) {
				String str;
				if (xCount == 1) str = " entry found."; else str = " entries found.";
				LazyJavieUI.getEntryCounterLabel().setText(xCount + str);
			}
			
			return;
			
		} catch (SQLException e) {
			SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());
			return;
		}
	}
	
	//-------------------------GET TABLE CONTENTS BY ROW-------------------------
	/**
	 * Gets the contents of a database table as a 2D array on a per-row basis.
	 * This means the inner array contains the contents of an entire row.
	 * @param table Where the contents are gonna be taken from.
	 * @return A 2D array containing the database table contents.
	 * @see getTableContentsByColumn(String table)
	 */
	public static Object[][] getTableContentsByRow(String table) {
		String query = "select * from " +table;
		List<Object> tg_row = new LinkedList<Object>();
		List<Object[]> tg_2d = new ArrayList<Object[]>();
		SQLconnector.dbPass = SQLconnector.getPass();
		
		try {
			Connection connection = DriverManager.getConnection(SQLconnector.DB_ADDRESS, SQLconnector.DB_LOGIN_ID, SQLconnector.getPass());
			Statement statement = connection.createStatement();
			
			//Gets the number of columns.
			ResultSet results = statement.executeQuery("select * from " +table+ " where 1=2;");
			int yCount = results.getMetaData().getColumnCount();
			
			results = statement.executeQuery(query);
			
			//Gets all the contents of an entire row, then adds it to tg_2d.
			//It will do this for every record in the table.
			//Suspected cause of bug. TODO Fix bug
			while (results.next()) {
				P.print(results.getString(2));
				for (int c = 1; c <= yCount; c++) {tg_row.add(results.getString(c));}
				
				if (!tg_2d.contains(tg_row.toArray())) tg_2d.add(tg_row.toArray());
				else P.print("DUPLICATE");
				tg_row.clear();
			}
			
			connection.close();	
		}
		catch (SQLException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}

		//Converts tg_2d into an object array then assigns it to a variable.
		Object[][] tableGridContents = tg_2d.toArray(new Object[0][0]);
		
//		//temp
//		int i= 0;
//		int s = Bot.jda.getGuilds().size();
//		for (Object[] a : tableGridContents) {
//			for (Object b : a) {
//				P.print(i + " " + b.toString());
//			}
//			i++;
//			//if (i >= s) break;
//		}
		
		return tableGridContents;
	}
	
	//-------------------------UPDATE TABLE CHOICE LIST-------------------------
	/**Updates the table choice list under the database tab.
	 * 
	 * @param tableList
	 */
	public static void updateTableList(Choice tableList) {
		try {
			ResultSet tableResultSet = SQLconnector.getResultSet("SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';");
			tableList.removeAll();
			tableList.add("- Select table -");
			while (tableResultSet.next()) {tableList.add(tableResultSet.getString("name"));}
			SQLconnector.con.close();
		}
		catch (SQLException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
	}
	
	//-------------------------GET COLUMN HEADERS FROM DATABASE-------------------------
	/**Returns the headers of the table as a string array.
	 * Made specifically for JTable.
	 * 
	 * @param columnCount
	 * @param results - ResultSet (uses JDBC)
	 * @return An array of column headers.
	 */
	public static String[] getColumnHeaders(int columnCount, ResultSet results) {
		
		List<String> headersList = new LinkedList<String>();
		for (int i = 1; i <= columnCount; i++) {
			try {headersList.add(results.getMetaData().getColumnName(i));}
			catch (SQLException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		}
		String[] columnHeaders = P.toStringArray(headersList);
		return columnHeaders;
	}
	
	//-------------------------GET MEMBERS-------------------------
	public static boolean getMembers() {
		try {
			//Lists all members the bot can see & excludes all duplicates.
			List<Member> newMemberList = new LinkedList<Member>();
			List<Member> allMemberList = new LinkedList<Member>();
			
			//Adds all the members from all servers into a list. (Includes duplicates)
			for (Guild g : Bot.jda.getGuilds()) {allMemberList.addAll(g.getMembers());}
			
			//Removes all duplicates.
			for (Member m : allMemberList) {
				P.print("Checking for: " + m.getUser().getAsTag());
				P.print(String.valueOf(newMemberList.size()));
				
				//TODO Fix issue. No new members are getting added because there is none to compare it to.
				for (Member m2 : newMemberList) {
					P.print("Comparing to: " + m2.getUser().getAsTag());
					if (m.getIdLong() == m2.getIdLong()) {P.print("Duplicate found: " + m.getUser().getAsTag()); break;}
					newMemberList.add(m);
				}
			}
			
			//Deletes all records
			SQLconnector.update("delete from members", false);
			
			//Gets the userId and userTag of every member and adds it into the database.
			P.print("Getting list.");
			for (Member m : newMemberList) {
				String userid = m.getId();
				String usertag = m.getUser().getAsTag();
				
				P.print("New member added to database: " +usertag);
				SQLconnector.update("insert into members (userid, usertag) values ('" +userid+ "', '" +usertag+ "');", false);
			}
			P.print("Done!");
			return true;
		} catch (NullPointerException e) {
			if (Bot.isAwake == false) {
				int memberCount = SQLconnector.getList("select * from members", "userid", false).size();
				P.print(String.valueOf(memberCount));
				
				//Grammar.
				String str;
				if (memberCount == 1) str = " entry found."; else str = " entries found.";
				str = memberCount + str;
				LazyJavieUI.getEntryCounterLabel().setText("Bot has to be online to get an updated list. " + str);
			} else {
				int memberCount = SQLconnector.getList("select * from members", "userid", false).size();
				
				//Grammar.
				String str;
				if (memberCount == 1) str = " entry found."; else str = " entries found.";
				str = memberCount + str;
				SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print("Unknown error caught: " + e.toString());
				LazyJavieUI.getEntryCounterLabel().setText("Error encountered; showing offline database. " + str);
			}
		} return true;
	}
}
