package home;

import java.sql.SQLException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import commands.Quit;

public class LazyJavie {
	
	public static void main(String args[]) {
		P.print("[LazyJavie] Pre-startup sequence...");
		P.print("|Looking for LazyJavie.db...");
		String tokenOverride = null;
		
		//Checks if the database exists and is functional. If the checker returns false,
		//this will attempt to create a new empty database ready for use.
		if (SQLconnector.dbCheck() == false) {
			P.print("\n[LazyJavie] Attempting to create a new database...");
			SQLconnector.NoDBfixer("sqlite");
		}
		
		//Tries to access a pre-made table "errorlog". This is for cases where the database check
		//doesn't detect a blank database with no tables.
		//This will attempt to create new tables without first trying to delete them.
		try {
			P.print("\n[LazyJavie] Attempting to access table 'errorlog'...");
			SQLconnector.getConn().createStatement().executeQuery("select * from errorlog");
		} catch (SQLException e) {
			P.print(e.toString());
			P.print("|Attempting to create a new database...");
			SQLconnector.NoDBfixer("sqlite-nodrop");
		}
		
		//Attempts to get a replacement token saved from database.
		tokenOverride = SQLconnector.get("select * from botsettings where name = 'discord_bot_token_override'", "value", false);
		
		//Prompts the bot for startup with the assigned token.
		P.print("|Attempting to start LazyJavie...");
		botLauncher("Start", tokenOverride);
	}
	
	public static void botLauncher(String task, @Nullable String botTokenOverride) {
		if (task.equalsIgnoreCase("Start")) {
			
			//Determines whether the token should be grabbed from the UI or from system.
			if (botTokenOverride == null) {Bot.tokenOverride = false;}
			else {Bot.tokenOverride = true; Bot.token = botTokenOverride;}
			
			/* Finally starts the bot.
			 * 
			 * Bot.start() returns a boolean value which corresponds to whether
			 * it was successful in starting up. Whenever it returns false,
			 * the following line will close the program automatically.
			 */
			P.print("|Awaiting startup...");
			boolean status = Bot.start();
			if (status == false) return;
			
			//Waits until the bot is ready. (Otherwise, the code will continue but the cache isn't yet ready, causing errors.)
			try {Bot.jda.awaitReady();}
			catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			catch (NullPointerException e) {SQLconnector.callError(e.toString() + " - likely caused by bad connection.", ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			return;
		
		} else {
			Quit.softExit();
			return;
		}
	}
}
