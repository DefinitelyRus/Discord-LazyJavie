package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import home.Bot;
import home.DiscordUtil;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketMatchupBuilder extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		String argsRaw = event.getMessage().getContentRaw();
		argsRaw = argsRaw.replace(args[0] + " ", "");
		
		//Presets the ticket message to a custom-made one.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setMatchupMessage") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("Set matchup message request by " + event.getMember().getUser().getAsTag() + ".");
			Bot.ticketEmbed = null;
			Bot.ticketMessage = argsRaw;
			DiscordUtil.send(event, "Set matchup prompt message to '" + argsRaw + "'. Waiting for '" + Bot.prefix + "setMatchupChannel <args...>'...");
		}
		
		//Presets the matchup message to a custom-made embed.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setMatchupEmbed") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketMatchupBuilder] Set matchup embed request by " + event.getMember().getUser().getAsTag() + ".");
			
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
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString()); return;}

			//Sets the footer. (Optional)
			try {
				footer = argsRawArray[2];
				embed.setFooter(footer);
			}
			catch (ArrayIndexOutOfBoundsException e) {P.print("|Optional footer missing; skipping...");}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			//Finalizes the embed.
			Bot.ticketEmbed = embed.build();
			event.getChannel().sendMessage("Set matchup prompt embed to display the following message. Waiting for '" + Bot.prefix + "setMatchupChannel <args...>'...");
			event.getChannel().sendMessage(Bot.ticketEmbed);
			return;
		}
		
		//Creates a channel where members can create matchups by clicking on an emote on a premade bot message.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setMatchupChannel") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketMatchupBuilder] Set matchup channel request by " + event.getMember().getUser().getAsTag() + ".");
			
			//Initialization
			String channelName = null;
			String categoryName = null;
			String archiveName = null;
			String role_id = null;
			String cat_id = null;
			String arc_id = null;
			String emote1 = "\u2755";
			String emote2 = "\u2754";
			String emote3 = "\u274c";
			long id = event.getGuild().getIdLong();

			try {role_id = args[1].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Moderator role tag/id argument missing. Cancelling..."); sendFail(event); return;}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			try {channelName = args[2].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Channel name argument missing. Cancelling..."); sendFail(event); return;}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}

			try {categoryName = args[3].toLowerCase();}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Category name argument missing. Cancelling..."); sendFail(event); return;}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}

			try {archiveName = args[4];}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Optional archive category not specified. Setting to default..."); archiveName = categoryName;}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			try {emote1 = args[5];}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Optional custom emote 1 not specified. Setting to default...");}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}

			try {emote2 = args[6];}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Optional custom emote 2 not specified. Setting to default...");}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			try {emote3 = args[7];}
			catch (ArrayIndexOutOfBoundsException e) {DiscordUtil.printsend(event, "Optional custom emote 3 not specified. Setting to default...");}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			//Filters the inputs.
			role_id = role_id.replace("<@&", "").replace(">", "");
			categoryName = categoryName.replace('_', ' ');
			emote1 = emote1.replace("<", "").replace(">", "");
			emote2 = emote2.replace("<", "").replace(">", "");
			
			//Prints and sends
			P.print("|Setting '" + channelName + "' as dedicated matchup prompt...");
			P.print("|Setting '" + categoryName + "' as dedicated matchup category...");
			P.print("|Setting '" + archiveName + "' as dedicated archive category...");
			DiscordUtil.send(event, "Setting `" + channelName + "` as dedicated matchup prompt...");
			DiscordUtil.send(event, "Setting `" + categoryName + "` as dedicated matchup category...");
			DiscordUtil.send(event, "Setting `" + archiveName + "` as dedicated archive category...");
			
			
			//CATEGORY ID ASSIGNMENT
			//Finds the specified category.
			P.print("|Finding category '" + categoryName + "'...");
			for (Category c : Bot.jda.getGuildById(id).getCategoriesByName(categoryName, true)) {
				P.print("|Found category '" + categoryName + "'.");
				cat_id = c.getId();
				break;
			}
			
			//Checks if cat_id is null as a result of not finding such named category.
			if (cat_id == null) {DiscordUtil.printsend(event, "Category name argument missing. Cancelling..."); return;}
			
			
			//ARCHIVE ID ASSIGNMENT
			//Finds the specified archive category.
			if (archiveName.equalsIgnoreCase(categoryName)) arc_id = cat_id;
			else {
				for (Category c : Bot.jda.getGuildById(id).getCategoriesByName(archiveName, true)) {
					P.print("|Found category '" + archiveName + "'.");
					arc_id = c.getId();
					break;
				}
			}

			//Checks if arc_id is null as a result of not finding such named category.
			if (arc_id == null) {DiscordUtil.printsend(event, "Optional archive category not specified. Setting to default..."); arc_id = cat_id;}
			
			
			//FIND CHANNEL
			//Finds the specified channel. There should be only one (1) channel in this list.
			P.print("|Finding channel '" + channelName + "'...");
			for (TextChannel ch : Bot.jda.getTextChannelsByName(channelName, true)) {
				P.print("|Found channel '" + channelName + "'.");
				P.print("|Sending prompt message...");
				
				//Checks if there is a preset matchup message or embed, then uses it.
				if (Bot.ticketEmbed == null) ch.sendMessage(Bot.ticketMessage).queue();
				else if (Bot.ticketMessage == null) ch.sendMessage(Bot.ticketEmbed).queue();
				else ch.sendMessage("Need help? Click the emote below!").queue();
				
				//1 second delay for cache refresh.
				P.print("|Prompt sent. Adding emote...");
				try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
				
				//SEND MESSAGE
				//Gets the most recent message sent from the target channel.
				List<Message> msgs = Bot.jda.getTextChannelById(ch.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {
					//Adds the reaction emote to the message.
					m.addReaction(emote1).queue();
					m.addReaction(emote2).queue();
					m.addReaction(emote3).queue();
					P.print("|Emote added to message.");
					
					//Saves all IDs to database.
					P.print("|Storing relevant IDs to database...");
					String msg_id = m.getId();
					String ch_id = m.getChannel().getId();
					SQLconnector.update("update botsettings set value = '" + cat_id  + "' where name = 'matchup_category_id'", false);
					SQLconnector.update("update botsettings set value = '" + arc_id + "' where name = 'matchup_archive_cat_id'", false);
					SQLconnector.update("update botsettings set value = '" + ch_id   + "' where name = 'matchup_channel_id'", false);
					SQLconnector.update("update botsettings set value = '" + msg_id  + "' where name = 'matchup_message_id'", false);
					SQLconnector.update("update botsettings set value = '" + role_id + "' where name = 'matchup_moderator_role_id'", false);
					
					DiscordUtil.printsend(event, "Matchup prompt fully created.");
					return;
				}
			}
		}
		
		//Sets archive category. A category where closed matchups are moved to.
		if (args[0].equalsIgnoreCase(Bot.prefix + "setArchiveCategory") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[TicketMatchupBuilder] Set archive category request by " + event.getMember().getUser().getAsTag() + ".");
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
			if (arc_id == null) {P.print("No category named '" + categoryName + "' found."); return;}
			
			//Updates the database.
			P.print("|Updating 'matchup_archive_cat_id' from database...");
			SQLconnector.update("update botsettings set value = '" + arc_id + "' where name = 'matchup_archive_cat_id'", false);
			
			P.print("Done!");
			return;
		}
	}

	private void sendFail(GuildMessageReceivedEvent event) {
		String descText = "\n`" + Bot.prefix + "setMatchupChannel <@role>¹ <channel-name>² <category_name>³ <archive_name>* <:emote1:>* <:emote2:>* <:emote3:>*`\n\n" +
							"[1] @mention the role you want to assign as moderators." +
							"[2] Don't #mention the text channel, only enter the name.\n" +
							"[3] Use underscores ( _ ) instead of spaces for entering categories.\n" +
							"[*] Optional parameters. You can skip these.\n\n" +
							"Sample command:\n `" + Bot.prefix + "setMatchupChannel @Moderators chat-matchup matchup_channels archive_channels :dab:`\n\n" +
							"You can use `" + Bot.prefix + "setMatchupMessage` OR `" + Bot.prefix + "setMatchupEmbed` to change the queue manager message. " +
							"You can also change the archive category after creating the queue manager using `" + Bot.prefix + "setArchiveCategory`.";
		String footText = "Made with ❤ by DefinitelyRus.";
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(0xD82D42);
		builder.setTitle("That's not how you do it!");
		builder.setDescription(descText);
		builder.setFooter(footText);
		DiscordUtil.send(event, builder.build());
		return;
	}
}