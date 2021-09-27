package commands;

import home.Bot;
import home.P;

public class ConsoleCmd {
	public static void call(String cmd) {
		String[] args = cmd.split("\\s+");
		
		//Get history
		if (args[0].equalsIgnoreCase(Bot.prefix + "history") || args[0].equalsIgnoreCase(Bot.prefix + "gethistory")) {
			try {Con_History.get(Integer.valueOf(args[1]));}
			catch (Exception e) {P.print(e.toString());}
			return;
		}
	}
}
