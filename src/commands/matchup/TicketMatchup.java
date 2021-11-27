package commands;

import java.awt.Color;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import home.Bot;
import home.DiscordUtil;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.ChannelManager;

public class TicketMatchup extends ListenerAdapter {
	/**
	 * Handles the reaction emotes received from the queuing system.
	 * This is where users can choose to join anonymously or as themselves.
	 */
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Prevents self from triggering this listener.
		if (event.getUserId().equals(Bot.jda.getSelfUser().getId())) return;
		
		//Prevents listener from being triggered by other messages.
		//Gets the saved message ID and compares it to the current one.
		else if (event.getMessageId().equals(SQLconnector.get("select value from botsettings where name = 'matchup_message_id'", "value", false))) {

			final User user = event.getUser();
			final String senderId = event.getUserId();
			final String userTag = event.getUser().getAsTag();
			final String randStr5 = P.randomString(5, true).toLowerCase();
			String codename = null;
			String partnerId = null;
			Member[] matchPair = {null, null};
			String emoteCodePoint = null;
			try {emoteCodePoint = event.getReactionEmote().getAsCodepoints();}
			catch (IllegalStateException e) {emoteCodePoint = "" + event.getReactionEmote().getAsReactionCode() + "";}
			boolean isAnon = true;
			
			//Non-anonymous
			//TODO Right now, the non-anonymous option only changes what name is displayed.
			if (emoteCodePoint.equals("U+2755")) {
				isAnon = false;
				codename = event.getUser().getAsTag();
				String appendix = "\n\n***There is 1 person ready to chat!***";
				List<Message> inviteChannelMsgs = event.getChannel().getHistory().retrievePast(100).complete();
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				
				//This FOR loop looks for the queue reception message then adds the appendix whenever someone joins the queue.
				skip:
				for (Message m : inviteChannelMsgs) {
					if (!m.getId().equals(event.getMessageId())) continue;
					
					//Gets all embeds attached.
					List<MessageEmbed> embedList = m.getEmbeds();
					for (MessageEmbed e : embedList) {
						//TODO FIX NullPointerException
						if (!e.getDescription().contains(appendix)) break skip;
						
						//Creates a new embed containing the same contents but with a modified description.
						EmbedBuilder embedBuilder = new EmbedBuilder();
						embedBuilder.setAuthor(e.getAuthor().getName());
						embedBuilder.setColor(e.getColor());
						embedBuilder.setDescription(e.getDescription().replace(appendix, ""));
						embedBuilder.setFooter(e.getFooter().getText());
						embedBuilder.setTitle(e.getTitle());
						
						//Edits the message then closes the script.
						m.editMessage(embedBuilder.build()).queue();
						break skip;
					}
					
					//Edits the message then closes the script.
					//This will only trigger if the list contains no embeds.
					String content = m.getContentRaw();
					if (content.contains(appendix)) m.editMessage(content.replace(appendix, "")).queue();
					break;
				}
				
				//Checks if the member is already queued.
				if (SQLconnector.get("select id from matchlist where id = '" + senderId + "'", "id", false) != null) return;
			}
			
			//Anonymous
			//TODO Combine with Non-anonymous to eliminate repeating code.
			else if (emoteCodePoint.equals("U+2754")) {
				isAnon = true;
				codename = P.randomName();
				String appendix = "\n\n***There is 1 person ready to chat!***";
				List<Message> inviteChannelMsgs = event.getChannel().getHistory().retrievePast(100).complete();
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				//Finds the source message.
				skip:
					for (Message m : inviteChannelMsgs) {
						if (!m.getId().equals(event.getMessageId())) continue;
						
						//Gets all embeds attached.
						List<MessageEmbed> embedList = m.getEmbeds();
						for (MessageEmbed e : embedList) {
							//TODO FIX NullPointerException
							if (!e.getDescription().contains(appendix)) break skip;
							
							//Creates a new embed containing the same contents but with a modified description.
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setAuthor(e.getAuthor().getName());
							embedBuilder.setColor(e.getColor());
							embedBuilder.setDescription(e.getDescription().replace(appendix, ""));
							embedBuilder.setFooter(e.getFooter().getText());
							embedBuilder.setTitle(e.getTitle());
							
							//Edits the message the closes the script.
							m.editMessage(embedBuilder.build()).queue();
							break skip;
						}
						
						//Edits the message then closes the script.
						//This will only trigger if the list contains no embeds.
						String content = m.getContentRaw();
						if (content.contains(appendix)) m.editMessage(content.replace(appendix, "")).queue();
						break;
					}
	
				//Checks if the member is already queued.
				if (SQLconnector.get("select id from matchlist where id = '" + senderId + "'", "id", false) != null) return;
			}
			
			//Remove from queue
			else if (emoteCodePoint.equals("U+274c")) {
				String appendix = "\n\n***There is 1 person ready to chat!***";
				List<Message> inviteChannelMsgs = event.getChannel().getHistory().retrievePast(100).complete();
				
				//Deletes the member's record from the database.
				SQLconnector.update("delete from matchlist where id = '" + senderId + "'", isAnon);
				
				//Removes the reaction emote.
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				
				//Finds the source message.
				for (Message m : inviteChannelMsgs) {
					if (!m.getId().equals(event.getMessageId())) continue;
					
					//Gets all embeds attached.
					List<MessageEmbed> embedList = m.getEmbeds();
					for (MessageEmbed e : embedList) {
						
						//Creates a new embed containing the same contents but with a modified description.
						EmbedBuilder embedBuilder = new EmbedBuilder();
						embedBuilder.setAuthor(e.getAuthor().getName());
						embedBuilder.setColor(e.getColor());
						embedBuilder.setDescription(e.getDescription().replace(appendix, ""));
						embedBuilder.setFooter(e.getFooter().getText());
						embedBuilder.setTitle(e.getTitle());
						
						//Edits the message the closes the script.
						m.editMessage(embedBuilder.build()).queue();
						return;
					}
					
					//Edits the message then closes the script.
					//This will only trigger if the list contains no embeds.
					m.editMessage(m.getContentRaw().replace(appendix, "")).queue();
					return;
				}
				return;
			}
			
			//Wrong emote
			else {
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				return;
			}
			
			//Adds the member to the queue list.
			SQLconnector.update("insert into matchlist (id, codename, matchcode) values ('" + senderId + "', '" + codename + "', null)", false);
			
			//Gets the queue list.
			List<String> queueIdList =
					SQLconnector.getList("select id from matchlist where matchcode is null", "id", false);
			
			P.print("\n[TicketMatchup] " + userTag + " has been added to matchup queue.");
			
			//Checks if there are not enough members to make a pair.
			//Lists always have 1 null value, so it this will check for 2 members + 1 null value.
			if (queueIdList.size() < 3) {
				P.print("Not enough members in queue. Waiting for members...");
				
				List<Message> inviteChannelMsgs = event.getChannel().getHistory().retrievePast(100).complete();
				for (Message m : inviteChannelMsgs) {
	
					//Finds the source message.
					if (!m.getId().equals(event.getMessageId())) continue;
					String appendix = "\n\n***There is 1 person ready to chat!***";
					
					//Gets all embeds attached.
					List<MessageEmbed> embedList = m.getEmbeds();
					for (MessageEmbed e : embedList) {
						
						//Creates a new embed containing the same contents but with a modified description.
						EmbedBuilder embedBuilder = new EmbedBuilder();
						embedBuilder.setAuthor(e.getAuthor().getName());
						embedBuilder.setColor(e.getColor());
						embedBuilder.setDescription(e.getDescription() + appendix);
						embedBuilder.setFooter(e.getFooter().getText());
						embedBuilder.setTitle(e.getTitle());
						
						//Edits the message the closes the script.
						m.editMessage(embedBuilder.build()).queue();
						return;
					}
					
					//Edits the message then closes the script.
					//This will only trigger if the list contains no embeds.
					m.editMessage(m.getContentRaw() + appendix).queue();
					return;
				}
				
				return;
				}
			
			
			//Chooses from a list of available members and pairs 2 of them.
			else {
				
				//Picks a random id from the list. If it's the same as the current "customer's",
				//it will randomize again until it finds a different one.
				//This will act as redundancy in the case that there are somehow more than
				//2 members in the queue before it finishes their matching process.
				for (String s : queueIdList) {
					if (s == null || s.equals(senderId)) continue;
					partnerId = s;
				}
				P.print("|Matching " + senderId + " with " + partnerId + "...");
				
				//Pairs them up in an array for later use.
				matchPair[0] = event.getGuild().getMemberById(senderId);
				matchPair[1] = event.getGuild().getMemberById(partnerId);
				
				//Updates the table to prevent them from being matched with other members.
				SQLconnector.update("update matchlist set matchcode = '" + randStr5 + "' where id = '" + senderId + "'", false);
				SQLconnector.update("update matchlist set matchcode = '" + randStr5 + "' where id = '" + partnerId + "'", false); //Fix????
				
				//Prepares the necessary arguments for calling newMatch() function.
				boolean isMatchAnonymous = false;
				String[] codenames = {null, null};
				codenames[0] = SQLconnector.get("select codename from matchlist where id = '" + senderId + "'", "codename", false);
				codenames[1] = SQLconnector.get("select codename from matchlist where id = '" + partnerId + "'", "codename", false);
				for (String s : codenames) { if (s != null) { isMatchAnonymous = true; break; } }
				
				newMatch(matchPair, codenames, isMatchAnonymous, randStr5, event);
				return;
			}
		}
		
		else {
			List<Message> messages = event.getChannel().getHistoryFromBeginning(100).complete().getRetrievedHistory();
			
			//TODO Something's broken here
			for (Message m : messages) {
				for (MessageEmbed e : m.getEmbeds()) {
					if (e.getTitle().equals("Stuff and things you need to know!")) {
						String emoteCodePoint = event.getReactionEmote().getAsCodepoints();
						if (emoteCodePoint.equals("U+1f6d1")) {
							P.print("[TicketMatchup] Close ticket request by: " + event.getMember().getUser().getAsTag());
							DiscordUtil.send(event, "Closing ticket...");
							event.retrieveMessage().complete().removeReaction("U+1f6d1", event.getUser()).queue();
							endMatch(event);
							return;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Handles the commands associated with the ticket matchup system.
	 */
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		TextChannel senderChannel = event.getChannel();
		String senderChannelName = event.getChannel().getName();
		Message message = event.getMessage();
		String[] messageSplit = message.getContentRaw().split("\\s+");
		List<Attachment> attachments = message.getAttachments();
		
		//[END]
		if (messageSplit[0].equals(Bot.prefix + "end") && senderChannel.getName().startsWith("match")) {
			P.print("\n[TicketMatchup] Close ticket request by: " + event.getMember().getUser().getAsTag());
			DiscordUtil.send(event, "Closing ticket...");
			endMatch(event);	
			return;
		}
		
		//[DELETE]
		else if (messageSplit[0].equals(Bot.prefix + "delete")) {
			P.print("\n[TicketMatchup] Delete archive request by: " + event.getMember().getUser().getAsTag());
			
			//Checks if the sender has an admin role.
			boolean isAdmin = DiscordUtil.isUserAdmin(event, "both");
			
			
			//Cancels if isAdmin remains false.
			if (isAdmin == false) {
				DiscordUtil.send(event, "You don't have permissions to do that!");
				P.print("Insufficient permissions.");
				return;
			}
			
			//Cancels if the ticket is not yet archived.
			if (senderChannelName.startsWith("match")) {
				P.print("Ticket is not yet archived. Cancelling...");
				DiscordUtil.send(event, "Enter `" + Bot.prefix + "end` first to close the ticket before deleting!");
				return;
			}
			
			//Deletes the channels successfully.
			else if (senderChannelName.startsWith("closed")) {
				P.print("|Initializing...");
				String suffix = senderChannelName.replace("closed1-", "").replace("closed2-", "");
				List<TextChannel> channels = event.getGuild().getTextChannels();
				
				for (TextChannel otherChannel : channels) {
					if (!otherChannel.getName().startsWith("closed")) continue;
					else if (!otherChannel.getName().endsWith(suffix)) continue;
					else if (otherChannel.getName().equals(senderChannelName)) continue;
					P.print("|Deleting channels...");
					senderChannel.delete().queue();
					otherChannel.delete().queue();
					
					P.print("Channels deleted successfully.");
					return;
				}
				DiscordUtil.printsend(event, "No partner channel found. Please delete this channel manually. Possible storage leak (unusused stored data not deleted); consider a database cleanup.");
				return;
			}
			else {
				DiscordUtil.printsend(event, "This command isn't intended for this channel!");
				return;
			}
		}
		
		//Cancels if it's not a match channel OR if the bot receives a message from itself.
		if (!senderChannelName.startsWith("match") || event.getMember().getId().equals(Bot.jda.getSelfUser().getId())) return;
		
		//Message mirroring
		else {
			List<TextChannel> channels = event.getGuild().getTextChannels();
			for (TextChannel c : channels) {
				String receiverChannelName = c.getName();
				//Ignores messages from channels that don't start with "match"
				if (!receiverChannelName.startsWith("match")) continue;
				else if (receiverChannelName.equals(senderChannelName)) continue;
				
				//Mirrors the message
				if (receiverChannelName.startsWith("match2-")) {
					String msgAsString = message.getContentRaw();
					String appendix = "\n";
					
					//Ignores all messages that start with the bot's command prefix.
					if (msgAsString.startsWith(Bot.prefix)) return;
					
					//Iterates through all the attachments in the sent message,
					//gets the attachment's URL, then appends each one to the output message.
					for (Attachment a : attachments) {appendix = appendix + a.getUrl();}
					msgAsString = msgAsString + appendix;
					
					//Avoids errors dealing with empty messages or similar exceptions.
					try {c.sendMessage(msgAsString).queue();} catch (IllegalStateException e) {}
					break;
				}
				else if (receiverChannelName.startsWith("match1-")) {
					String msgAsString = message.getContentRaw();
					String appendix = "\n";
					
					//Ignores all messages that start with the bot's command prefix.
					if (msgAsString.startsWith(Bot.prefix)) return;
					
					//Iterates through all the attachments in the sent message,
					//gets the attachment's URL, then appends each one to the output message.
					for (Attachment a : attachments) {appendix = appendix + a.getUrl();}
					msgAsString = msgAsString + appendix;
					
					//Avoids errors dealing with empty messages or similar exceptions.
					try {c.sendMessage(msgAsString).queue();} catch (IllegalStateException e) {}
					break;
				}
			}
		}
	}

	/**
	 * Creates a new matchup.
	 * 
	 * @param matchPair - 2 Member objects in an array. These are the members to be paired in matchups.
	 * @param codenames - 2 String objects in an array. These are the names members will be revealed as.
	 * @param isAnon - A boolean for anonymous matchup. 2 separate channels if true, 1 regular channel is false. [TODO FALSE not yet implemented]
	 * @param randomString - A randomly generated code to use in channel names. This will also serve as a unique identifier in database.
	 * @param event
	 */
	private void newMatch(Member[] matchPair, String[] codenames, boolean isAnon, String randomString, GenericGuildMessageEvent event) {
		//TODO Create 1 channel and give both members permission to talk.
		
		//Instantiation
		Category catg = null;
		String newMatch1Name = null;
		String newMatch2Name = null;
		List<Permission> perms = new LinkedList<Permission>();
		
		try {
			
			//Creates a category and role object based on the IDs taken from earlier.
			catg = event.getGuild().getCategoryById(SQLconnector.get("select * from botsettings where name = 'matchup_category_id'", "value", false));
			
			//2 Strings with the same randomized
			newMatch1Name = "match1-" + randomString;
			newMatch2Name = "match2-" + randomString;
			
			//Creates a list of permissions intended for normal messaging only.
			perms.add(Permission.VIEW_CHANNEL); perms.add(Permission.MESSAGE_ADD_REACTION);
			perms.add(Permission.MESSAGE_ATTACH_FILES); perms.add(Permission.MESSAGE_EXT_EMOJI);
			perms.add(Permission.MESSAGE_HISTORY); perms.add(Permission.MESSAGE_READ);
			perms.add(Permission.MESSAGE_WRITE); perms.add(Permission.USE_SLASH_COMMANDS);
		} catch (Exception e) {SQLconnector.callError(e); DiscordUtil.printsend(event, e.toString()); return;}
		
		//Creates two channels, gives each member permissions to chat in each one.
		if (isAnon == true) {
			//Creates two channels based on the same randomized string.
			catg.createTextChannel(newMatch1Name).complete().createPermissionOverride(matchPair[0]).setAllow(perms).queue();
			catg.createTextChannel(newMatch2Name).complete().createPermissionOverride(matchPair[1]).setAllow(perms).queue();
			
			//Embed builder
			//TODO Maybe minimize creating objects?
			String title = "Stuff and things you need to know!";
			String desc = "No one else can see this conversation.\n\n" +
					"Clicking :warning: or typing `" + Bot.prefix + "report` will give moderators viewing access to both sides of this convesation.\n"
					+ "*Use this if you suspect the other person of anything malicious.*\n\n"
					+ "Clicking :octagonal_sign: or typing `" + Bot.prefix + "end` will close this conversation without summoning moderators.\n"
					+ "*Use this if you only want to end the conversation.*";
			String footer = "Made with ❤ by DefinitelyRus.";
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setColor(0xD82D42);
			embedBuilder.setTitle(title);
			embedBuilder.setDescription(desc);
			embedBuilder.setFooter(footer);
			MessageEmbed embed = embedBuilder.build();
			
			//Delays 1 second for cache to refresh.
			try {TimeUnit.SECONDS.sleep(1);} catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
			
			int index = 0;
			List<TextChannel> textChannels = event.getGuild().getTextChannels();
			
			for (TextChannel t : textChannels) {
				//Skips all channels that don't end with a specific suffix.
				if (!t.getName().endsWith(randomString)) {continue;}
				
				//Sends the embedded message to the current channel.
				t.sendMessage(embed).queue();
				
				//Delays 1 second for cache to refresh.
				try {TimeUnit.SECONDS.sleep(1);} catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
				
				//Loops through the first 10 messages sent in a channel then
				//adds the necessary reaction emotes for exiting or reporting.
				for (Message m : t.getHistory().retrievePast(10).complete()) {
					if (!m.getMember().getId().equals(Bot.jda.getSelfUser().getId())) continue;
					
					//Adds reaction emotes
					m.addReaction("\u26A0").queue(); //Warning sign
					m.addReaction("\uD83D\uDED1").queue(); //Octagonal Sign
				}
				
				//Sends another message telling the user their alias that the other side can see.
				try {t.sendMessage(matchPair[index].getAsMention() + ", *you are talking to **"
				+ codenames[(index-1)*(index-1)] + "** and you will be known as **" + codenames[index] + "**.*").queue();}
				catch (Exception e) {SQLconnector.callError(e); P.print(e.toString());}
				index++;
			}
		}
	}
	
	/**
	 * Closes and archives the ticket.
	 * This will rename the channels, move to the archives category, and remove member permissions.
	 * <br><br>
	 * The channels can only be accessed using $viewArchive command.
	 * @param event The source event from which the changes will be made.
	 */
	private void endMatch(GenericGuildMessageEvent event) {
		P.print("|Initializing...");
		for (TextChannel channel2 : event.getGuild().getTextChannels()) {
			TextChannel channel1 = event.getChannel();
			String name = channel2.getName();
			String matchCode = channel1.getName().replace("match1-", "").replace("match2-", "");
			
			/*
			 * This loops attempts to look for channel2 by checking if the current channel name
			 * contains the same match code but not the exact same name as channel1.
			 * 
			 * channel1 is the origin channel that triggered this command.
			 * channel2 is the corresponding channel.
			 */
			if (name.endsWith(matchCode) && !name.equals(channel1.getName())) {
				Category archiveCategory = event.getGuild().getCategoryById(
						SQLconnector.get("select value from botsettings where name = 'matchup_archive_cat_id'", "value", false));
				ChannelManager c1Manager = channel1.getManager();
				ChannelManager c2Manager = channel2.getManager();
				List<Permission> perms = new LinkedList<Permission>();
				perms.add(Permission.VIEW_CHANNEL); perms.add(Permission.MESSAGE_ADD_REACTION);
				perms.add(Permission.MESSAGE_ATTACH_FILES); perms.add(Permission.MESSAGE_EXT_EMOJI);
				perms.add(Permission.MESSAGE_HISTORY); perms.add(Permission.MESSAGE_READ);
				perms.add(Permission.MESSAGE_WRITE); perms.add(Permission.USE_SLASH_COMMANDS);
				List<String> memberIdList = (SQLconnector.getList("select * from matchlist where matchcode = '" + matchCode + "'", "id", false));
				
				//Changes name to closed1-xxxxx and closed2-xxxxx.
				P.print("|Archiving " + channel1.getName() + "...");
				c1Manager.setName(channel1.getName().replace("match", "closed"))
				.setParent(archiveCategory)
				.putPermissionOverride(event.getGuild().getMemberById(memberIdList.get(2)), null, perms)
				.queue();
				P.print("|Removed permissions for " + event.getGuild().getMemberById(memberIdList.get(2)).getUser().getAsTag() + "...");

				P.print("|Archiving " + channel2.getName() + "...");
				c2Manager.setName(name.replace("match", "closed"))
				.setParent(archiveCategory)
				.putPermissionOverride(event.getGuild().getMemberById(memberIdList.get(1)), null, perms)
				.queue();
				P.print("|Removed permissions for " + event.getGuild().getMemberById(memberIdList.get(1)).getUser().getAsTag() + "...");
				
				P.print("|Deleting leftover records from database...");
				SQLconnector.update("delete from matchlist where matchcode = '" + matchCode + "'", false);
				
				DiscordUtil.printsend(event, "Ticket has been archived!");
				return;
			}
		}
		DiscordUtil.printsend(event, "No partner channel found. Please delete this channel manually. Possible storage leak (unusused stored data not deleted); consider a database cleanup.");
		return;
	}
	
	//TODO REMOVE OR IMPROVE
	@Deprecated
	public static void sendEmbed(GenericGuildMessageEvent event,
			@Nullable String author, @Nullable String authorUrl, @Nullable String authorIconUrl,
			@Nullable Color color, @Nullable String description, @Nullable TemporalAccessor timestamp,
			@Nullable String footer, @Nullable String footerIconUrl,
			@Nullable String imageUrl, @Nullable String thumbnailUrl,
			@Nullable String title, @Nullable String titleUrl) {
		EmbedBuilder embed = new EmbedBuilder();
		
		//TODO Add if-null checkers.
		embed.setAuthor(author, authorUrl, authorIconUrl);
		embed.setColor(color);
		embed.setDescription(description);
		embed.setFooter(footer, footerIconUrl);
		embed.setImage(imageUrl);
		embed.setThumbnail(thumbnailUrl);
		embed.setTimestamp(timestamp);
		embed.setTitle(title, titleUrl);
		event.getChannel().sendMessage(embed.build());
	}
}