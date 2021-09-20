package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import home.Bot;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Warning:
 * This only works for one server at a time.
 * It only stores the ID for one channel which is specific to a server.
 * However, it will still attempt to mention someone on the target channel
 * if it detects that a new member joins even if the member is from another server
 * where the bot also resides.
 * <br><br>This also means the bot can only support one server at a time.
 * @author DefinitelyRus
 */
//TODO Allow for server-specific settings.
public class NewMemberPrompter extends ListenerAdapter{
	
	//A GuildMemberJoinEvent listener where new members are mentioned in a specified channel
	//upon joining, most useful for forcing them to read rules.
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		Guild guild = event.getGuild();
		String targetChannelID = SQLconnector.get("select * from botsettings where name = 'automention_on_join_channel_id'", "value", false);
		TextChannel channel = guild.getTextChannelById(targetChannelID);
		
		//Creates a message that includes instructions for everyone in case the deletion doesn't work out as planned.
		P.print("\n[NewMemberPrompter] New member detected. Prompting " + member.getUser().getAsTag() + "...");
		channel.sendMessage("<@NMP> " + member.getAsMention() + ", if you see this, please read the rules. If this message doesn't get deleted, tell a staff member.").queue();
		P.print("|Member mentioned in #" + channel.getName() + ". Deleting message...");
		
		/* 
		 * Waits for 0.2 seconds so the local cache can refresh.
		 * Otherwise, the wrong message will be deleted and the new one will remain.
		 * 
		 * This should be replaced with a more reliable waiting method.
		 * 1 second may not be enough in case of sudden connection issues so
		 * the above-mentioned issue may still occur.
		 */
		try {TimeUnit.MILLISECONDS.sleep(1000);} catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		//Gets the most recent message (a list containing 1 item) then deletes it.
		List<Message> msg = channel.getHistory().retrievePast(1).complete();
		msg.forEach((m) -> m.delete().queue());
		P.print("Message deleted. Prompt successful.");
		
		return;
	}
	
	//Commands related to the feature above.
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getMessage().getContentRaw().startsWith(Bot.prefix)) return;
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		//-------------------------SET AUTOMENTION CHANNEL-------------------------
		if (args[0].equalsIgnoreCase(Bot.prefix + "setAutoMentionChannel") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			P.print("\n[NewMemberPrompter] Set automention channel request by " + event.getMember().getUser().getAsTag());
			String channelId = null;
			Guild guild = event.getGuild();
			
			//Attempting to retrieve ID from message...
			P.print("|Attempting to retrieve ID from message...");
			try {channelId = args[1];}
			catch (ArrayIndexOutOfBoundsException e) {
				P.print("args[1] is empty.");
				P.send(event, "Missing argument. Mention the channel you want to set or enter its ID.");
				return;
			}
			catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString()); return;}
			
			//Removes the symbols included in the formatting when mentioning text channels.
			P.print("|Filtering user input...");
			channelId = channelId.replace("<", "");
			channelId = channelId.replace(">", "");
			channelId = channelId.replace("#", "");
			
			P.send(event, "Attempting to set auto-mention to " + args[1] + ". You will receive a mention there during this.");
			
			//----------TESTING PHASE----------
			//Attempts to mention the sender to the target channel.
			P.print("|Attempting to mention sender to target channel...");
			try {guild.getTextChannelById(channelId).sendMessage(event.getMember().getAsMention()).queue();}
			//Triggered when entering gibberish or a non-text channel ID.
			catch (NumberFormatException e) {
				P.print("args[1] is not a valid TextChannel ID.");
				P.send(event, args[1] + " is not a valid text channel ID.");
				return;
			}
			//Possibly triggered if ID belongs to a channel from other servers the bot doesn't recognize.
			catch (NullPointerException e) {
				P.print("This ID might belong to a text channel outside the bot's scope. Invite the bot there if this is your intended target.");
				P.send(event, "This ID might belong to a text channel outside the bot's scope. Invite the bot there if this is your intended target.");
				return;
			}
			//Every other error that may occur.
			catch (Exception e) {
				P.print("Unknown error encountered:\n" + ExceptionUtils.getStackTrace(e));
				P.send(event, "Unknown error encountered: `" + e.toString() + "`.\nCheck console for more details.");
				return;
			}
			
			//Same issue.
			P.print("|Waiting for cache to refresh...");
			try {TimeUnit.MILLISECONDS.sleep(1000);} catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			P.print("|Getting message history...");
			List<Message> msg = guild.getTextChannelById(channelId).getHistory().retrievePast(1).complete();
			P.print("|Deleting message...");
			msg.forEach((m) -> m.delete().queue());
			
			P.print("|Test done. Updating database...");
			P.send(event, "Test done. Check if everything works as expected.");
			
			//----------FINALIZING----------
			//Updating the local settings database.
			P.print("|Updating bot setting 'automention_on_join_channel_id' from database...");
			SQLconnector.update("update botsettings set value = '" + channelId + "' where name = 'automention_on_join_channel_id'", false);
			SQLconnector.update("update botsettings set last_modified = datetime() where name = 'automention_on_join_channel_id'", false);
			P.print("Setup successful.");
			return;
		}
	}
}
