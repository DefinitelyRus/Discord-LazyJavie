package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import home.Bot;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketMessageExchange extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		String botID = event.getJDA().getSelfUser().getId();
		
		//Mirrors any message sent to their corresponding channel.
		if (event.getChannel().getName().matches("ticket-[0-9]{5}") && !event.getMember().getId().equals(botID)) {
			String mirrorName = event.getChannel().getName().replace("ticket-", "mirror-");
			
			List<TextChannel> mirrorSolo = event.getGuild().getTextChannelsByName(mirrorName, true);
			
			for (TextChannel c : mirrorSolo) {
				c.sendMessage(event.getMessage()).queue();
			}
		}
		
		//Same function as before, but reverse.
		if (event.getChannel().getName().matches("mirror-[0-9]{5}") && !event.getMember().getId().equals(botID)) {
			String mirrorName = event.getChannel().getName().replace("mirror-", "ticket-");
			
			List<TextChannel> mirrorSolo = event.getGuild().getTextChannelsByName(mirrorName, true);
			
			for (TextChannel c : mirrorSolo) {
				c.sendMessage(event.getMessage()).queue();
			}
		}
		
		//Close ticket command.
		//Checks if it matches either #ticket-XXXXX or #mirror-XXXXX and if it's a self-report.
		if ((event.getChannel().getName().matches("ticket-[0-9]{5}") || event.getChannel().getName().matches("mirror-[0-9]{5}"))
				&& !event.getMember().getId().equals(botID) && args[0].equalsIgnoreCase(Bot.prefix + "close")) {
			String channelName = event.getChannel().getName();
			String mirrorName = null;
			
			if (channelName.startsWith("ticket")) mirrorName = event.getChannel().getName().replace("ticket-", "mirror-");
			else mirrorName = event.getChannel().getName().replace("mirror-", "ticket-");
			
			List<TextChannel> mirrorSolo = event.getGuild().getTextChannelsByName(mirrorName, true);
			
			for (TextChannel c : mirrorSolo) {
				EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(0x35AA35);
				embed.setTitle("Ticket closed.");
				
				//Identifies whether the ticket was closed from client or responder side.
				String closeRequester = null;
				if (channelName.startsWith("ticket")) closeRequester = "client";
				else if (channelName.startsWith("mirror")) closeRequester = "responder";
				embed.setDescription("Ticket closed by " + closeRequester + ". Press :open_file_folder: to reopen ticket.");
				
				//Sends to origin channel.
				event.getChannel().sendMessage(embed.build()).queue();
				
				//Sends to mirror channel.
				c.sendMessage(embed.build()).queue();
				try {TimeUnit.SECONDS.sleep(1);}
				catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
				
				try {
					//Sends reaction emote to origin.
					List<Message> msgs = Bot.jda.getTextChannelById(c.getId()).getHistory().retrievePast(1).complete();
					for (Message m : msgs) {m.addReaction("\uD83D\uDCC2").queue();}
					
					//Sends reaction emote to mirror.
					msgs = Bot.jda.getTextChannelById(event.getChannel().getId()).getHistory().retrievePast(1).complete();
					for (Message m : msgs) m.addReaction("\uD83D\uDCC2").queue();
				} catch (Exception e) {P.print("[TicketMessageExchange] Probable ContextException. Ignore it, but here's an error code anyway."); P.print(e.toString());}
			}
			
			if (channelName.startsWith("ticket")) {
				//TODO Only delete when :x: emote is pressed.
				//Deletes the client-side channel.
				event.getChannel().delete().queue();
				
				//Renames the responder-side channel from "mirror-XXXXX" to "closed-XXXXX".
				for (TextChannel c : mirrorSolo) c.getManager().setName(c.getName().replace("mirror", "closed"));
				
			} else if (channelName.startsWith("mirror")) {
				//TODO Only delete when :x: emote is pressed.
				//Deletes the client-side channel.
				for (TextChannel c : mirrorSolo) c.delete().queue();
				
				//Renames the responder-side channel from "mirror-XXXXX" to "closed-XXXXX".
				event.getChannel().getManager().setName(event.getChannel().getName().replace("mirror", "closed"));
			}
		}
	}
}
