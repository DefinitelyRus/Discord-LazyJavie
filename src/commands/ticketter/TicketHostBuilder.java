package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import home.Bot;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketHostBuilder extends ListenerAdapter{
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		String argsRaw = event.getMessage().getContentRaw();
		argsRaw = argsRaw.replace(args[0] + " ", "");
		
		//Presets the ticket message to a custom-made one.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setTicketMessage") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("Set ticket message request by " + event.getMember().getUser().getAsTag() + ".");
			Bot.ticketEmbed = null;
			Bot.ticketMessage = argsRaw;
			event.getChannel().sendMessage("Set ticket prompt message to '" + argsRaw + "'. Waiting for '" + Bot.prefix + "setTicketChannel <args...>'...").queue();
		}
		
		//Presets the ticket message to a custom-made embed.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setTicketEmbed") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketHostBuilder] Set ticket embed request by " + event.getMember().getUser().getAsTag() + ".");
			
			String[] argsRawArray = argsRaw.split("<b>");
			String header, body, footer = null;
			Bot.ticketMessage = null;
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(0xD82D42);
			
			//Sets the header and body.
			try {
				header = argsRawArray[0];
				body = argsRawArray[1];
				embed.addField(header, body, true);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				P.print("Missing arguments. Cancelling...");
				event.getChannel().sendMessage("Missing header and/or body. Use `<b>` to separate them.").queue();
				return;
				}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString()); return;}

			//Sets the footer. (Optional)
			try {
				footer = argsRawArray[2];
				embed.setFooter(footer);
			}
			catch (ArrayIndexOutOfBoundsException e) {P.print("|Optional footer missing; skipping...");}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			//Finalizes the embed.
			Bot.ticketEmbed = embed.build();
			event.getChannel().sendMessage("Set ticket prompt embed to display the following message. Waiting for '" + Bot.prefix + "setTicketChannel <args...>'...").queue();
			event.getChannel().sendMessage(Bot.ticketEmbed).queue();
			return;
		}
		
		//Creates a channel where members can create tickets by clicking on an emote on a premade bot message.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setTicketChannel") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketHostBuilder] Set ticket channel request by " + event.getMember().getUser().getAsTag() + ".");
			
			//Initialization
			String channelName = null;
			String categoryName = null;
			String role_id = null;
			String emote = ":incoming_envelope:868713407749173248";
			long id = event.getGuild().getIdLong();
			String cat_id = null;
			String arc_id = null;
			String archiveName = null;

			try {role_id = args[1].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {P.print("Channel name argument missing. Cancelling..."); return;}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			try {channelName = args[2].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {P.print("Channel name argument missing. Cancelling..."); return;}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}

			try {categoryName = args[3].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {P.print("Category name argument missing. Cancelling..."); return;}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			try {emote = args[4];}
			catch (ArrayIndexOutOfBoundsException e) {P.print("Optional custom emote not specified. Setting to default...");}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			try {archiveName = args[5];}
			catch (ArrayIndexOutOfBoundsException e) {P.print("Optional archive category not specified. Setting to default..."); archiveName = categoryName;}
			catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			//Filters the inputs.
			categoryName = categoryName.replace('_', ' ');
			emote = emote.replace("<", "");
			emote = emote.replace(">", "");
			role_id = role_id.replace("<@&", "");
			role_id = role_id.replace(">", "");
			if (emote.equalsIgnoreCase("null")) emote = ":incoming_envelope:868713407749173248";
			
			//Outputs
			P.print("|Setting '" + channelName + "' as dedicated ticket prompt...");
			P.print("|Setting '" + categoryName + "' as dedicated ticket category...");
			P.print("|Setting '" + archiveName + "' as dedicated archive category...");
			event.getChannel().sendMessage("Setting `" + channelName + "` as dedicated ticket prompt...").queue();
			event.getChannel().sendMessage("Setting `" + categoryName + "` as dedicated ticket category...").queue();
			event.getChannel().sendMessage("Setting `" + archiveName + "` as dedicated archive category...").queue();
			
			//Finds the specified category.
			P.print("|Finding category '" + categoryName + "'...");
			for (Category c : Bot.jda.getGuildById(id).getCategoriesByName(categoryName, true)) {
				P.print("|Found category '" + categoryName + "'.");
				cat_id = c.getId();
				break;
			}
			
			//Finds the specified archive category.
			if (archiveName.equalsIgnoreCase(categoryName)) arc_id = cat_id;
			else {
				for (Category c : Bot.jda.getGuildById(id).getCategoriesByName(archiveName, true)) {
					P.print("|Found category '" + archiveName + "'.");
					arc_id = c.getId();
					break;
				}
			}
			
			//Finds the specified channel.
			P.print("|Finding channel '" + channelName + "'...");
			for (TextChannel ch : Bot.jda.getTextChannelsByName(channelName, true)) {
				P.print("|Found channel '" + channelName + "'."); 
				P.print("|Sending prompt message...");
				
				//Checks if there is a preset ticket message or embed, then uses it.
				if (Bot.ticketEmbed == null) ch.sendMessage(Bot.ticketMessage).queue();
				else if (Bot.ticketMessage == null) ch.sendMessage(Bot.ticketEmbed).queue();
				else ch.sendMessage("Need help? Click the emote below!").queue();
				
				//1 second delay for cache refresh.
				P.print("|Prompt sent. Adding emote...");
				try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
				
				//Gets the most recent message sent from the target channel.
				List<Message> msgs = Bot.jda.getTextChannelById(ch.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {
					//Adds the reaction emote to the message.
					m.addReaction(emote).queue();
					P.print("|Emote added to message.");
					
					//Saves all IDs to database.
					P.print("|Storing relevant IDs to database...");
					String msg_id = m.getId();
					String ch_id = m.getChannel().getId();
					SQLconnector.update("update botsettings set value = '" + role_id + "' where name = 'ticket_responder_role_id'", false);
					SQLconnector.update("update botsettings set value = '" + msg_id  + "' where name = 'ticket_message_id'", false);
					SQLconnector.update("update botsettings set value = '" + ch_id   + "' where name = 'ticket_channel_id'", false);
					SQLconnector.update("update botsettings set value = '" + cat_id  + "' where name = 'ticket_category_id'", false);
					SQLconnector.update("update botsettings set value = '" + arc_id + "' where name = 'ticket_archive_cat_id'", false);
					
					P.print("Ticket prompt fully created.");
					return;
				}
			}
		}
		
		//Sets archive category. A category where closed tickets are moved to.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setArchiveCategory") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketHostBuilder] Set archive category request by " + event.getMember().getUser().getAsTag() + ".");
			String categoryName = args[1].replace('_', ' ');
			String arc_id = null;
			
			//Searches for a specified category.
			P.print("|Searching for a category named '" + categoryName + "' from guild '" + event.getGuild().getName() + "'.");
			List<Category> catList = event.getGuild().getCategoriesByName(categoryName, true);
			for (Category c : catList) {
				arc_id = c.getId();
				P.print("|Category '" + c.getName() + "' found.");
			}
			
			//Checks if arc_id is still null after the search.
			if (arc_id.equals(null)) {P.print("No category named '" + categoryName + "' found."); return;}
			
			//Updates the database.
			P.print("|Updating 'ticket_archive_cat_id' from database...");
			SQLconnector.update("update botsettings set value = '" + arc_id + "' where name = 'ticket_archive_cat_id'", false);
			
			P.print("Done!");
			return;
		}
	}
}
