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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketMatchup extends ListenerAdapter {
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Prevents self from triggering this listener.
		if (event.getUserId().equals(Bot.jda.getSelfUser().getId())) {return;}
		
		String userTag = event.getUser().getAsTag();
		String userId = event.getUserId();
		String matchId = null;
		Member[] matchPair = {null, null};
		String emoteCodePoint = event.getReactionEmote().getAsCodepoints();
		boolean isAnon = true;
		
		//Non-anonymous
		if (emoteCodePoint.equals("\u2755")) isAnon = false;
		
		//Anonymous
		else if (emoteCodePoint.equals("\u2754")) isAnon = true;
		
		//Remove from queue
		else if (emoteCodePoint.equals("\u274C")) {
			//TODO Remove from # of queued members.
			SQLconnector.update("delete from matchlist where id = '" + userId + "'", isAnon);
			return;
		}
		
		//Wrong emote
		else {
			event.retrieveMessage().complete().removeReaction(emoteCodePoint).queue();
			return;
		}
		
		//Adds the member to the 
		SQLconnector.update("insert into matchlist values ('" + userId + "', '" + userTag + "', 0)", false);
		
		List<String> queueIdList =
				SQLconnector.getList("select * from matchlist where is_matched = 0", "id", false);
		
		P.print("\n[TicketMatchup] " + event.getUser().getAsTag() + " has been added to matchup queue.");
		
		//Checks if there are not enough members to make a pair.
		if (queueIdList.size() < 2) {P.print("Not enough members in queue. Waiting for members..."); return;}
		
		//Else, it randomly chooses from a list of available members and pairs 2 of them.
		else {
			P.print("|Match found.");
			
			//Picks a random id from the list. If it's the same as the current "customer's",
			//it will randomize again until it finds a different one.
			do {
				int i = RandomUtils.nextInt(0, queueIdList.size()-1);
				matchId = queueIdList.get(i);
			} while (matchId.equals(userId));
			
			//Pairs them up in an array for later use.
			matchPair[0] = event.getGuild().getMemberById(userId);
			matchPair[1] = event.getGuild().getMemberById(matchId);
			
			SQLconnector.update("update matchlist set is_matched = 1 where id = '" + userId + "'", false);
			SQLconnector.update("update matchlist set is_matched = 1 where id = '" + matchId + "'", false);
			
			
			boolean isMatchAnonymous = false;
			String[] codenames = {null, null};
			codenames[0] = SQLconnector.get("select * from matchlist where id = '" + userId + "'", "codename", false);
			codenames[1] = SQLconnector.get("select * from matchlist where id = '" + matchId + "'", "codename", false);
			for (String s : codenames) { if (s != null) { isMatchAnonymous = true; break; } }
			
			newMatch(matchPair, codenames, isMatchAnonymous);
			}
		
		
		String msg_id1 = event.getMessageId();
		String msg_id2 = null;
		String cat_id = null;
		String resp_id = null;
		String newMatchAName = null;
		String newMatchBName = null;
		
		//Gets the expected emote origin's message, channel, and the target category.
		try {
			msg_id2 = SQLconnector.get("select * from botsettings where name = 'matchup_message_id'", "value", false);
			cat_id = SQLconnector.get("select * from botsettings where name = 'matchup_category_id'", "value", false);
			resp_id = SQLconnector.get("select * from botsettings where name = 'matchup_moderator_role_id'", "value", false);
		} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString()); return;}
		
		if (msg_id1.equals(msg_id2)) {
			
			int highestValue = 0;
			List<TextChannel> textChannels = event.getGuild().getTextChannels();
			List<TextChannel> ticketChannels = new LinkedList<TextChannel>();
			
			//Adds every ticket channel to a list.
			for (TextChannel t : textChannels) {if (t.getName().startsWith("matchA")) ticketChannels.add(t);}
			
			//Adds every ticket channel's ID to Bot.activeMatches.
			for (TextChannel ch : ticketChannels) {
				P.print("|Adding " + ch.getName() + " to list.");
				
				//Gets the channel name then removes all non-numerical characters, then assigns it to a variable.
				highestValue = Integer.valueOf(ch.getName().replace("matchA-", ""));
				
				//Adds the variable to an existing list.
				Bot.activeMatches.add((Integer) highestValue);
				P.print("|Current highest value: " + highestValue);
			}
			
			//Loops until the new ticket ID has the largest value for redundancy.
			while (Bot.activeMatches.contains(highestValue)) {
				highestValue++;
				P.print("|[Warning] Highest value mismatch. Currently relying on redundancy.");
			}
			
			//Assigns the channel names to a string variable.
			newMatchAName = "matchA-" + String.format("%05d", highestValue);
			newMatchBName = "matchB-" + String.format("%05d", highestValue);
			
			//Creates new channels.
			P.print("|Creating channels for #" + newMatchAName + "...");
			event.getGuild().getCategoryById(cat_id).createTextChannel(newMatchAName).queue();
			event.getGuild().getCategoryById(cat_id).createTextChannel(newMatchBName).queue();
			
			//Pauses for 2 seconds for local cache to refresh.
			try {TimeUnit.SECONDS.sleep(2);}
			catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			//Binds a list of perms to a list.
			//TODO Store in a separate .java file.
			List<Permission> perms = new LinkedList<Permission>();
			perms.add(Permission.VIEW_CHANNEL); perms.add(Permission.MESSAGE_ADD_REACTION);
			perms.add(Permission.MESSAGE_ATTACH_FILES); perms.add(Permission.MESSAGE_EXT_EMOJI);
			perms.add(Permission.MESSAGE_HISTORY); perms.add(Permission.MESSAGE_READ);
			perms.add(Permission.MESSAGE_WRITE); perms.add(Permission.USE_SLASH_COMMANDS);
			
			//Assigns a random name to the ticketter.
			String name = randomName();
			
			//Lets ticket maker to chat in ticket-X but not mirror-X, and vice-versa.
			P.print("|Finding target channel...");
			for (TextChannel c : Bot.jda.getGuildById(event.getGuild().getId()).getTextChannelsByName(newMatchAName, false)) {
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
				catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
				List<Message> msgs = Bot.jda.getTextChannelById(c.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {m.addReaction("\uD83D\uDCC1").queue();}
			}
			
			for (TextChannel c : Bot.jda.getGuildById(event.getGuild().getId()).getTextChannelsByName(newMatchBName, false)) {
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
				catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
				List<Message> msgs = Bot.jda.getTextChannelById(c.getId()).getHistory().retrievePast(1).complete();
				for (Message m : msgs) {m.addReaction("\uD83D\uDCC1").queue();}
			}
			return;
		}
	}
	
	private void newMatch(Member[] matchPair, String[] codenames, boolean isAnon) {
		/*
		 * TODO
		 * Check if at least one of the members are anonymous.
		 * 
		 * If so, create 2 channels then give both members permission to talk in their respective channels.
		 * Have the messages be mirrored and have the anonymous member(s) be assigned a random code name.
		 * 
		 * If not, create 1 channel then give both members permission to talk.
		 */
	}

	//Random name generator. I know it's not the best way to do it but we don't need that many names.
	public static String randomName() {
		
		//TODO Add this to another .java file.
		List<String> names = new LinkedList<String>();
		names.add("Falcon"); names.add("Chief"); names.add("Flower"); names.add("Northern Light"); names.add("Iceberg"); names.add("Amber");
		names.add("Eagle"); names.add("Fox"); names.add("Macro"); names.add("Niner"); names.add("Savanna"); names.add("Astley"); names.add("Locke");
		names.add("Opera"); names.add("Nickel"); names.add("Coiler"); names.add("Mongus"); names.add("Pinkel"); names.add("Copper");
		
		int i = 0;
		try {i = RandomUtils.nextInt(0, names.size()-1);}
		catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString()); return names.get(0);}
		return names.get(i);
	}
}