package home;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class DiscordUtil {
	
	/**
	 * Checks if the sender has admin permissions based on their user permissions, channel permissions, and/or role label.
	 * 
	 * @param event - The GuildMessageEvent the script will be based on.
	 * @param searchType - The level of checking to be done.
	 * <br>Use "trueAdmin" to only check for Administrator permission on a per-user or per-role level.
	 * <br>Use "channelManager" to also check for channel management permissions based on the channel the action was performed on.
	 * <br>Use "labelAdmin" to also check if the user has a role named starting with "Admin".
	 * <br>Use "any" to use all checks.
	 * @return TRUE if the user passes the specified checks or FALSE if not.
	 */
	public static boolean isUserAdmin(GuildMessageReceivedEvent event, String searchType) {
		Pattern pattern = Pattern.compile("Admin", Pattern.CASE_INSENSITIVE);
		if (searchType == null) searchType = "both";
		
		//Gets the sender's list of permissions
		List<Permission> memPerms = new LinkedList<Permission>(event.getMember().getPermissions());
		
		//[True Admin]
		//Checks if any of the sender's permissions are named "Administrator".
		//If this doesn't pass, it just moves on to checking per-role perms.
		for (Permission p : memPerms) {
			if (p.getName().equals("Administrator")) return true;
		}
		
		//Checks the permissions of each role for "Manage Channels" permission.
		for (Role r : event.getMember().getRoles()) {
			String roleName = r.getName();
			boolean isChannelManager = false;
			
			//Gets the list of permissions from the role based on the channel the action was performed on.
			List<Permission> rolePerms = new LinkedList<Permission>(r.getPermissions(event.getChannel()));
			
			//Checks if any of the permissions are named "Manage Channels".
			for (Permission p : rolePerms) {
				if (p.getName().equals("Manage Channels")) return true;
			}
			
			//Checks 
			if ((searchType.equals("channelManager") || searchType.equals("any")) && isChannelManager == true) return true; 
			if ((searchType.equals("labelAdmin") || searchType.equals("any")) && pattern.matcher(roleName).find()) return true;
		}
		
		return false;
	}
	
	//------------------------- PRINT & SEND -------------------------
	/**
	 * Prints a specified string to the console and sends to Discord in one method call.
	 * @param event
	 * @param message
	 */
	public static void printsend(GenericGuildMessageEvent event, String message) {
		P.print(message); send(event, message);
	}
	
	//------------------------- SEND MESSAGE -------------------------
	public static void send(GenericGuildMessageEvent event, Object message) {
		if (message instanceof String) {
			event.getChannel().sendMessage((String) message).queue();
		} else if (message instanceof MessageEmbed) {
			event.getChannel().sendMessage((MessageEmbed) message).queue();
		}
	}
	
	//------------------------- SEND TARGETTED BY CHANNEL CLASS -------------------------
	public static void sendTo(MessageChannel target, Object message) {
		if (message instanceof String) {
			target.sendMessage((String) message).queue();
		} else if (message instanceof MessageEmbed) {
			target.sendMessage((MessageEmbed) message).queue();
		}
	}

	//------------------------- SEND TARGETTED BY CHANNEL ID -------------------------
	public static void sendTo(Guild guild, String target, Object message) {
		if (message instanceof String) {
			guild.getTextChannelById(target).sendMessage((String) message).queue();
		} else if (message instanceof MessageEmbed) {
			guild.getTextChannelById(target).sendMessage((MessageEmbed) message).queue();
		}
	}
	
	//------------------------- LOG TO CHANNEL -------------------------
	public static void sendLog(String guildId, String channelId, String message) {
		if (LazyJavie.isReady == false) {
			LazyJavie.pendingLog.append(message + "\n");
		}
		else {
			try {
				LazyJavie.pendingLog.append(message + "\n");
				message = LazyJavie.pendingLog.toString();
				
				if (message.length() > 2000) {
					LazyJavie.pendingLog = new StringBuffer();
					return;
				}
				else {
					Bot.jda.getGuildById(guildId).getTextChannelById(channelId).sendMessage("`" + message + "`").queue();
					LazyJavie.pendingLog = new StringBuffer();
				}
			} catch (OutOfMemoryError e) {
				Bot.jda.getPresence().setActivity(Activity.listening("to OutOfMemoryError"));
				Bot.isAwake = false;
				Bot.currentChannel = null;
				Bot.ticketEmbed = null;
				Bot.ticketMessage = null;
				LazyJavie.pendingLog = null;
				LazyJavie.isReady = false;
				SQLconnector.callError(e);
			} catch (Exception e) {
				LazyJavie.pendingLog.append(message);
				SQLconnector.callError(e);
			}
		}
	}
	
//	//Original code using regular concatenation.
//	public static void sendLog(String guildId, String channelId, String message) {
//		if (LazyJavie.isReady == false) {
//			LazyJavie.pendingLog += message + "\n";
//		}
//		else {
//			try {
//				message = LazyJavie.pendingLog + message;
//				Bot.jda.getGuildById(guildId).getTextChannelById(channelId).sendMessage("`" + message + "`").queue();
//				LazyJavie.pendingLog = "";
//			} catch (Exception e) {
//				LazyJavie.pendingLog += message;
//			}
//		}
//	}
	
	//------------------------- DELETE MESSAGE -------------------------
	public static void delete(Message message) {
		message.delete().queue();
	}
}
