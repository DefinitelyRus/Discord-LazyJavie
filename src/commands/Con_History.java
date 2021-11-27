package commands;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import home.Bot;
import home.LazyJavieUI;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Con_History {
	public static void get(int n) {
		TextChannel ch = LazyJavieUI.channelDict.get(LazyJavieUI.getTextChannelsList().getSelectedItem());
		if (n < 1) {P.print(n + " is too small. The value must be between 1 and 100. Value set to 1 by default."); n = 1;}
		else if (n > 100) {P.print(n + " is too large. The value must be between 1 and 100. Value set to 100 by default."); n = 100;}
		List<Message> msgs = ch.getHistory().retrievePast(n).complete();
		String usertag, content;
		
		try {
			P.print("\nGetting the " +n+ " most recent messages from " + ch.getName());
			for (Message msg : msgs) {
				usertag = msg.getMember().getUser().getAsTag();
				content = msg.getContentRaw();
				P.print(usertag + ": " + content);;
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {P.print("No amount specified: " + Bot.prefix + "history <no. of messages>");}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
	}
}

//Where is this used? Presumably made for retrieving the contents of the UI console but where exactly?