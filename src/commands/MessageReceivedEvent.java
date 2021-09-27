package commands;

import home.Bot;
import home.P;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedEvent extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		call(event);
	}

	public static void call(GuildMessageReceivedEvent event) {
		String msg = event.getMessage().getContentRaw();
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		//Prints every message to the console.
		if (Bot.muted == false && event.getChannel().equals(Bot.currentChannel)) {P.print(event.getMember().getUser().getAsTag() + ": " + msg);}
		
		//if (args[0].equalsIgnoreCase(Bot.prefix + "")) {}
		
		//if (args[0].equalsIgnoreCase(Bot.prefix + "")) {}
	}
	
	// TODO Let a number of commands be runnable from console without having to rewrite code.
}
