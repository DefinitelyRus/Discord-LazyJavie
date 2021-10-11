package commands;

import java.awt.Color;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.sun.jdi.event.Event;

import home.Bot;
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
	 */
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Prevents self from triggering this listener.
		if (event.getUserId().equals(Bot.jda.getSelfUser().getId())) return;
		
		//Prevents listener from being triggered by other messages.
		else if (event.getMessageId().equals(SQLconnector.get("select * from botsettings where name = 'matchup_message_id'", "value", false))) {
			
			String codename = null;
			User user = event.getUser();
			String senderId = event.getUserId();
			String userTag = event.getUser().getAsTag();
			String partnerId = null;
			Member[] matchPair = {null, null};
			String emoteCodePoint = null;
			String randStr5 = randomString(5).toLowerCase();
			try {emoteCodePoint = event.getReactionEmote().getAsCodepoints();}
			catch (IllegalStateException e) {emoteCodePoint = "" + event.getReactionEmote().getAsReactionCode() + "";}
			boolean isAnon = true;
			
			//Non-anonymous
			if (emoteCodePoint.equals("U+2755")) {
				isAnon = false;
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				codename = event.getUser().getAsTag();
	
				//Checks if the member is already queued.
				if (SQLconnector.get("select * from matchlist where id = '" + senderId + "'", "id", false) != null) return;
			}
			
			//Anonymous
			else if (emoteCodePoint.equals("U+2754")) {
				isAnon = true;
				event.retrieveMessage().complete().removeReaction(emoteCodePoint, user).queue();
				codename = randomName();
	
				//Checks if the member is already queued.
				if (SQLconnector.get("select * from matchlist where id = '" + senderId + "'", "id", false) != null) return;
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
					SQLconnector.getList("select * from matchlist where matchcode is null", "id", false);
			
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
				codenames[0] = SQLconnector.get("select * from matchlist where id = '" + senderId + "'", "codename", false);
				codenames[1] = SQLconnector.get("select * from matchlist where id = '" + partnerId + "'", "codename", false);
				for (String s : codenames) { if (s != null) { isMatchAnonymous = true; break; } }
				
				newMatch(matchPair, codenames, isMatchAnonymous, randStr5, event);
				return;
			}
		}
		
		else {
			List<Message> messages = event.getChannel().getHistoryFromBeginning(100).complete().getRetrievedHistory();
			for (Message m : messages) {
				for (MessageEmbed e : m.getEmbeds()) {
					if (e.getTitle().equals("Stuff and things you need to know!")) {
						String emoteCodePoint = event.getReactionEmote().getAsCodepoints();
						P.print(emoteCodePoint);
						if (emoteCodePoint.equals("U+1f6d1")) {
							P.print("[TicketMatchup] Close ticket request by: " + event.getMember().getUser().getAsTag());
							P.send(event, "Closing ticket...");
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
		
		if (messageSplit[0].equals(Bot.prefix + "end") && senderChannel.getName().startsWith("match")) {
			P.print("\n[TicketMatchup] Close ticket request by: " + event.getMember().getUser().getAsTag());
			P.send(event, "Closing ticket...");
			endMatch(event);	
			return;
		}
		else if (messageSplit[0].equals(Bot.prefix + "delete")) {
			P.print("\n[TicketMatchup] Delete archive request by: " + event.getMember().getUser().getAsTag());
			//if () {TODO Put admin check here.}
			
			//Cancels if the ticket is not yet archived.
			if (senderChannelName.startsWith("match")) {
				P.print("Ticket is not yet archived. Cancelling...");
				P.send(event, "Enter `" + Bot.prefix + "end` first to close the ticket before deleting!");
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
				P.printsend(event, "No partner channel found. Please delete this channel manually. Possible storage leak (unusused stored data not deleted); consider a database cleanup.");
				return;
			}
			else {
				P.printsend(event, "This command isn't intended for this channel!");
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
	 * Creates a new ticket.
	 * This 
	 * @param matchPair
	 * @param codenames
	 * @param isAnon
	 * @param randomString
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
		} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.printsend(event, e.toString()); return;}
		
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
			embedBuilder.setTitle(title);
			embedBuilder.setDescription(desc);
			embedBuilder.setFooter(footer);
			MessageEmbed embed = embedBuilder.build();
			
			//Delays 1 second for cache to refresh.
			try {TimeUnit.SECONDS.sleep(1);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			int index = 0;
			List<TextChannel> textChannels = event.getGuild().getTextChannels();
			
			for (TextChannel t : textChannels) {
				//Skips all channels that don't end with a specific suffix.
				if (!t.getName().endsWith(randomString)) {continue;}
				
				//Sends the embedded message to the current channel.
				t.sendMessage(embed).queue();
				
				//Delays 1 second for cache to refresh.
				try {TimeUnit.SECONDS.sleep(1);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
				
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
				catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
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
		//Initialization
		P.print("|Initializing...");
		for (TextChannel otherChannel : event.getGuild().getTextChannels()) {
			TextChannel senderChannel = event.getChannel();
			String name = otherChannel.getName();
			String suffix = senderChannel.getName().replace("match1-", "").replace("match2-", "");
			if (name.endsWith(suffix) && !name.equals(senderChannel.getName())) {
				Category archive = event.getGuild().getCategoryById(
						SQLconnector.get("select value from botsettings where name = 'matchup_archive_cat_id'", "value", false));
				ChannelManager c1Manager = senderChannel.getManager();
				ChannelManager c2Manager = otherChannel.getManager();
				List<Permission> perms = new LinkedList<Permission>();
				perms.add(Permission.VIEW_CHANNEL); perms.add(Permission.MESSAGE_ADD_REACTION);
				perms.add(Permission.MESSAGE_ATTACH_FILES); perms.add(Permission.MESSAGE_EXT_EMOJI);
				perms.add(Permission.MESSAGE_HISTORY); perms.add(Permission.MESSAGE_READ);
				perms.add(Permission.MESSAGE_WRITE); perms.add(Permission.USE_SLASH_COMMANDS);
				List<String> memberIdList = (SQLconnector.getList("select * from matchlist where matchcode = '" + suffix + "'", "id", false));
				
				//Changes name to closed1-xxxxx and closed2-xxxxx.
				P.print("|Archiving " + senderChannel.getName() + "...");
				c1Manager.setName(senderChannel.getName().replace("match", "closed"))
				.setParent(archive)
				.putPermissionOverride(event.getGuild().getMemberById(memberIdList.get(2)), null, perms)
				.queue();

				P.print("|Archiving " + otherChannel.getName() + "...");
				c2Manager.setName(name.replace("match", "closed"))
				.setParent(archive)
				.putPermissionOverride(event.getGuild().getMemberById(memberIdList.get(1)), null, perms)
				.queue();
				
				P.print("|Deleting leftover records from database...");
				SQLconnector.update("delete from matchlist where matchcode = '" + suffix + "'", false);
				
				P.printsend(event, "Ticket has been archived!");
				return;
			}
		}
		P.printsend(event, "No partner channel found. Please delete this channel manually. Possible storage leak (unusused stored data not deleted); consider a database cleanup.");
		return;
	}
	
	//TODO Move this to P.java
	//Random name generator. I know it's not the best way to do it but we don't need that many names.
	public static String randomName() {
		String name = null;
		
		//TODO Add this to another .java file.
		List<String> names = new LinkedList<String>();
		names.add("Falcon"); names.add("Chief"); names.add("Flower"); names.add("Northern Light"); names.add("Iceberg"); names.add("Amber");
		names.add("Eagle"); names.add("Fox"); names.add("Macro"); names.add("Niner"); names.add("Savanna"); names.add("Astley"); names.add("Locke");
		names.add("Opera"); names.add("Nickel"); names.add("Coiler"); names.add("Mongus"); names.add("Pinkel"); names.add("Copper");
		
		int i = 0;
		try {i = RandomUtils.nextInt(0, names.size()-1);}
		catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString()); return names.get(0);}
		
		//Assign a random name. Recurse if it returns null.
		name = names.get(i);
		if (name == null) name = randomName();
		return name;
	}
	
	//TODO Move this to P.java
	public static String randomString(int length) {
		if (length < 1) return "";
		
		String string = "";
		final char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
				'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
		
		for (int i = 0; i < length; i++) {
			char randomChar = chars[RandomUtils.nextInt(0, chars.length-1)];
			string = string + randomChar;
		}
		
		return string;
	}
	
	//TODO Move to P.java
	public static void sendEmbed(GenericGuildMessageEvent event,
			@Nullable String author, @Nullable String authorUrl, @Nullable String authorIconUrl,
			@Nullable Color color, @Nullable String description, @Nullable TemporalAccessor timestamp,
			@Nullable String footer, @Nullable String footerIconUrl,
			@Nullable String imageUrl, @Nullable String thumbnailUrl,
			@Nullable String title, @Nullable String titleUrl) {
		EmbedBuilder embed = new EmbedBuilder();
		
		//TODO Add if null checkers.
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