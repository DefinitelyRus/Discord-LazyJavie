package home;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;

/**
 * This is a general-purpose class made to ease the process of doing seemingly menial tasks but somehow isn't.
 * This includes the unnecessarily long System.out.println() function, which can get tedious to type.
 * @author DefinitelyRus
 */
public class P {
	//-------------------------PRINT-------------------------
	/**
	 * Similar in function to System.out.println().
	 * @param args - A string value to be printed to the console.
	 */
	public static void print (String args) {
		System.out.println(args);
		
		//Additional code for custom-made systems (optional).
		//code code code code code code
	}
	
	//-------------------------PRINTRAW-------------------------
	/**
	 * Identical in function to System.out.println().
	 * @param args - A string value to be printed to the console.
	 */
	public static void printraw (String args) {System.out.println(args);}
 
	
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
	
	public static void delete(Message message) {
		message.delete().queue();
	}
}
