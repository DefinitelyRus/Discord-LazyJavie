package commands;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import home.Bot;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketEmoteListener extends ListenerAdapter {
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Prevents bots from triggering this listener.
		if (event.getUserId().equals(Bot.jda.getSelfUser().getId())) {return;}
		
		String msg_id1 = event.getMessageId();
		String msg_id2 = null;
		String cat_id = null;
		String resp_id = null;
		String newTicketName = null;
		String newMirrorName = null;
		
		//Gets the expected emote origin's message, channel, and the target category.
		try {
			msg_id2 = SQLconnector.get("select * from botsettings where name = 'ticket_message_id'", "value", false);
			cat_id = SQLconnector.get("select * from botsettings where name = 'ticket_category_id'", "value", false);
			resp_id = SQLconnector.get("select * from botsettings where name = 'ticket_responder_role_id'", "value", false);
		} catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
		
		if (msg_id1.equals(msg_id2)) {
			P.print("\n[TicketEmoteListener] New ticket in query requested by "
					+ "*".repeat(event.getUser().getAsTag().length()) + "...");
			List<TextChannel> textChannels = event.getGuild().getTextChannels();
			List<TextChannel> ticketChannels = new LinkedList<TextChannel>();
			int highestValue = 0;
			
			//Adds every ticket channel to a list.
			for (TextChannel t : textChannels) {
				if (t.getName().startsWith("ticket"))
					ticketChannels.add(t);
			}
			
			//Adds every ticket channel's ID to Bot.activeTickets.
			for (TextChannel ch : ticketChannels) {
				P.print("|Adding " + ch.getName() + " to list.");
				highestValue = Integer.valueOf(ch.getName().replace("ticket-", ""));
				Bot.activeTickets.add((Integer) highestValue);
				P.print("|Current highest value: " + highestValue);
			}
			
			//Loops until the new ticket ID has the largest value for redundancy.
			while (Bot.activeTickets.contains(highestValue)) highestValue++;
			
			//Assigns the channel names to a string variable.
			newTicketName = "ticket-" + String.format("%05d", highestValue);
			newMirrorName = "mirror-" + String.format("%05d", highestValue);
			
			//Creates new channels.
			P.print("|Creating channels for #" + newTicketName + "...");
			event.getGuild().getCategoryById(cat_id).createTextChannel(newTicketName).queue();
			event.getGuild().getCategoryById(cat_id).createTextChannel(newMirrorName).queue();
			
			//Pauses for 1 second for local cache to refresh.
			try {TimeUnit.SECONDS.sleep(1);}
			catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
			
			//Binds a list of perms to a list.
			List<Permission> perms = new LinkedList<Permission>();
			perms.add(Permission.VIEW_CHANNEL); perms.add(Permission.MESSAGE_ADD_REACTION);
			perms.add(Permission.MESSAGE_ATTACH_FILES); perms.add(Permission.MESSAGE_EXT_EMOJI);
			perms.add(Permission.MESSAGE_HISTORY); perms.add(Permission.MESSAGE_READ);
			perms.add(Permission.MESSAGE_WRITE); perms.add(Permission.USE_SLASH_COMMANDS);
			
			//Assigns a random name to the ticketter.
			String name = randomName();
			
			//Lets ticket maker to chat in ticket-X but not mirror-X, and vice-versa.
			P.print("|Finding target channel...");
			for (TextChannel c : Bot.jda.getGuildById(event.getGuild().getId()).getTextChannelsByName(newTicketName, false)) {
				P.print("|Main channel found! " + c.getName());
				c.createPermissionOverride(event.getMember()).setAllow(perms).queue();
				c.createPermissionOverride(Bot.jda.getRoleById(resp_id)).setDeny(perms).queue();

				//Creates an embed to send as a prompt. It contains the welcome message and the close ticket instruction.
				EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(0x35AA35);
				embed.setTitle("Welcome to " + c.getName() + "!");
				embed.setDescription("Need our help? You've come to the right place! Don't worry - you're completely anonymous, even to us!" +
										"\n\nPlease note that you will be referred to as '" + name + "'." +
										"\n\nEnter `" + Bot.prefix + "close` or press :file_folder: to close the ticket.");
				embed.setFooter("Session ID: " + String.format("%05d", highestValue), event.getJDA().getSelfUser().getAvatarUrl());
				c.sendMessage(embed.build()).queue();

				try {TimeUnit.SECONDS.sleep(1);}
				catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
				List<Message> msgs = Bot.jda.getTextChannelById(c.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {m.addReaction("\uD83D\uDCC1").queue();}
			}
			
			for (TextChannel c : Bot.jda.getGuildById(event.getGuild().getId()).getTextChannelsByName(newMirrorName, false)) {
				P.print("|Mirror channel found! " + c.getName());
				c.createPermissionOverride(event.getMember()).setDeny(perms).queue();
				c.createPermissionOverride(Bot.jda.getRoleById(resp_id)).setAllow(perms).queue();

				//Creates an embed to send as a prompt. It contains the welcome message and the close ticket instruction.
				EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(0x35AA35);
				embed.setTitle("Welcome to " + c.getName() + "!");
				embed.setDescription("Someone needs your help. Please refer to them as '" + name + "'." + 
										"\n\nEnter `" + Bot.prefix + "close` or press :file_folder: to close the ticket.");
				embed.setFooter("Session ID: " + String.format("%05d", highestValue), event.getJDA().getSelfUser().getAvatarUrl());
				c.sendMessage(embed.build()).queue();

				try {TimeUnit.SECONDS.sleep(1);}
				catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
				List<Message> msgs = Bot.jda.getTextChannelById(c.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {m.addReaction("\uD83D\uDCC1").queue();}
			}
			return;
		}
	}
	
	//Random name generator. I know it's not the best way to do it but we don't need that many names.
	public static String randomName() {
		List<String> names = new LinkedList<String>();
		names.add("Falcon"); names.add("Chief"); names.add("Flower"); names.add("Northern Light"); names.add("Iceberg"); names.add("Amber");
		names.add("Eagle"); names.add("Fox"); names.add("Macro"); names.add("Niner"); names.add("Savanna"); names.add("Astley");
		
		int i = 0;
		try {i = RandomUtils.nextInt(0, names.size()-1);}
		catch (Exception e) {SQLconnector.callError(e); P.print(e.toString()); return names.get(0);}
		return names.get(i);
	}
}