package home;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * This is a general-purpose class made to ease the process of doing seemingly menial tasks but somehow isn't.
 * This includes the unnecessarily long System.out.println() function, which can get tedious to type.
 * @author DefinitelyRus
 */
public class P {
	//This string holds the contents of the entire console.
	private static String currentConsoleContents = "";
	
	private static final String expectedNullPointerError =
			"java.lang.NullPointerException: Cannot invoke \"javax.swing.JTextArea.setText(String)\" because the return value of \"home.LazyJavieUI.getConsoleOutput()\" is null";
	
	//-------------------------PRINT-------------------------
	/**
	 * Similar in function to System.out.println().
	 * @param args - A string value to be printed to the console.
	 */
	public static void print (String args) {
		System.out.println(args);
		
		//Additional code for custom-made systems (optional).
		consoleOut(args);
	}
	
	//-------------------------PRINTRAW-------------------------
	/**
	 * Identical in function to System.out.println().
	 * @param args - A string value to be printed to the console.
	 */
	public static void printraw (String args) {System.out.println(args);}
 	
	//-------------------------CONSOLE OUTPUT [PROPRIETARY]-------------------------
	/**
	 * Takes the current console contents, appends the string argument, then applies the changes.
	 * @param args
	 */
	private static void consoleOut(String args) {
		String newString = null;
		try {
			currentConsoleContents = LazyJavieUI.consoleOutput.getText();
			
			try {
				/* Checks if the console UI is empty.
				 * If it is, this will set the console to display only the new text.
				 * If not, this will set the console to display the old text then appends the new text at the end.
				 */
				if (currentConsoleContents == null || currentConsoleContents.equals("")) {newString = args;}
				else {newString = currentConsoleContents + "\n" + args;}
			}
			catch (Exception e) {print(e.toString() + "\n" + args); e.printStackTrace();}
			
		} catch (NullPointerException e) {newString = args;}
		
		//Applies the changes
		try {LazyJavieUI.getConsoleOutput().setText(newString);}
		catch (Exception e) {
			try {
				if (!e.toString().equals(expectedNullPointerError)) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); printraw(e.toString());}
			} catch (Exception e1) {SQLconnector.callError(e1.toString(), ExceptionUtils.getStackTrace(e1)); P.printraw(e1.toString());}
		}
	}
	
	//PRINT & SEND
	public static void printsend(String args) {}
	
	//-------------------------PRIMITIVE BOOLEAN ARRAY-------------------------
	/**Converts a boolean list into primitive boolean array.
	 * @param booleanList
	 * @return A primitive boolean array form of the list.
	 */
	public static boolean[] toBooleanArray(final List<Boolean> booleanList) {
	    final boolean[] primitives = new boolean[booleanList.size()];
	    int index = 0;
	    for (Boolean object : booleanList) {
	        primitives[index++] = object;
	    }
	    return primitives;
	}
	
	//TODO Make one for every primitive data type.
	//-------------------------PRIMITIVE STRING ARRAY-------------------------
	/**Converts a string list into a primitive string array.
	 * @param stringList
	 * @return A primitive string array form of the list.
	 */
	public static String[] toStringArray(final List<String> stringList) {
	    final String[] primitives = new String[stringList.size()];
	    int index = 0;
	    for (String object : stringList) {
	        primitives[index++] = object;
	    }
	    return primitives;
	}
	
	//-------------------------[PROPRIETARY] DISCORD SEND MESSAGE-------------------------
	public static void send(GenericGuildMessageEvent event, Object message) {
		if (message instanceof String) {
			event.getChannel().sendMessage((String) message).queue();
		} else if (message instanceof MessageEmbed) {
			event.getChannel().sendMessage((MessageEmbed) message).queue();
		}
	}
	//
	public static void delete(Message message) {
		message.delete().queue();
	}
}
