/*
 * ---------------!!! ADD TO README !!!---------------
 * This is where all return commands go. Commands that only returns a message and does nothing else.
 * 
 * Available commands are as follows:
 * -	$bottoken
 * 		Rick rolls you.
 * 
 * -	$ping
 * 		Returns the latency from the server to the bot's host.
 * 
 * -	$test
 * 		Confirms that the bot is still connected. 
 * 
 * -	$points
 * 		Returns how many points a member has accumulated.
 * 
 * -	$help
 * 		Returns a list of available commands, their syntax, and description.
 */
package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import home.Bot;
import home.DiscordUtil;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Returns extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getMessage().getContentRaw().startsWith(Bot.prefix)) return;
		
		//Initialization
		final String[] args = event.getMessage().getContentRaw().split("\\s+");
		final String argsFull = event.getMessage().getContentRaw();
		final String requestby = event.getMember().getUser().getAsTag();;
		final boolean isAdmin = DiscordUtil.isUserAdmin(event, "any");
		
		//[BOT TOKEN] Returns the bot's token... not really.----------------------------------------------------
		if (args[0].equalsIgnoreCase(Bot.prefix + "bottoken")) {
			P.print("\n[Returns] Requesting bot token: " + requestby);
			event.getChannel().sendMessage("Bot token: ||Never gonna give you up~ Never gonna let you down~||").queue();
			return;
		}
		
		//[PING] Returns the latency.---------------------------------------------------------------------------
		else if (args[0].equalsIgnoreCase(Bot.prefix + "ping")) {
			P.print("\n[Returns] Requesting ping: " + requestby);
			long ping = event.getJDA().getGatewayPing();
			P.print("|Latency returned.");
			
			//Embed block
			P.print("Ping: " + ping + "ms");
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(0x77B255);
			embed.setDescription("Pong: **" +ping+ "ms**");
			embed.setFooter("Requested by " + requestby , event.getMember().getUser().getAvatarUrl());
			event.getChannel().sendMessage(embed.build()).queue();
			return;
		}
		
		//[TEST] Just returns a confirmation message to see if the bot works.-----------------------------------
		else if (args[0].equalsIgnoreCase(Bot.prefix + "test")) {
			P.print("\n[Returns] TEST! Sender: " + event.getMember().getUser().getName());
			
			//Embed block
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(0x77B255);
			embed.setTitle(":white_check_mark: Test success!");
			embed.setFooter("Requested by " + requestby , event.getMember().getUser().getAvatarUrl());
			
			DiscordUtil.send(event, embed.build());
			return;
		}
		
		//[SPAMCONSOLE] Continually sends "SPAM!" to the console 100 times.
		else if (args[0].equalsIgnoreCase(Bot.prefix + "spamconsole") && isAdmin) {
			P.print("\n[Returns] Console spam requested by " + requestby);
			event.getChannel().sendMessage("Spamming console...").queue();
			P.print("");
			for (int i = 0; i < 100; i++) {P.print("SPAM!");}
			return;
		}
		
		else if (args[0].equalsIgnoreCase(Bot.prefix + "cmd") && isAdmin) {
			P.print(requestby + ": " + argsFull.replace(Bot.prefix + "cmd ", ""));
			return;
		}
		
		//[HIDDENPING] Pings someone without message residue.
		else if (args[0].equalsIgnoreCase(Bot.prefix + "hiddenping") && isAdmin) {
			P.print("\n[Returns] Hidden ping request by " + requestby);
			Member member = null;
			TextChannel channel = null;
			
			try {
				String userId = args[1];
				String channelId = args[2];
				
				//TODO Allow option between @user and usertag:user#0000.
				//if (args[1].startsWith("usertag:")) {
				//	userId = args[1].replace("usertag:", "");
				//}
				
				//Removes the symbols included in the formatting when mentioning text channels.
				P.print("|Filtering user input...");
				userId = userId.replace("<", "");
				userId = userId.replace(">", "");
				userId = userId.replace("!", "");
				userId = userId.replace("@", "");
				channelId = channelId.replace("<", "");
				channelId = channelId.replace(">", "");
				channelId = channelId.replace("#", "");
				
				member = event.getGuild().getMemberById(userId);
				channel = event.getGuild().getTextChannelById(channelId);
			} catch (Exception e) {
				//TODO Add exception catcher exclusively for array pointers.
				P.print("|Missing arguments.");
				event.getChannel().sendMessage("Format: `" + Bot.prefix + "hiddenping <@user> <#text-channel>`. If you're sure you formatted this correctly, check console for an error code.").queue();
				P.print(ExceptionUtils.getStackTrace(e));
				SQLconnector.callError(e);
				return;
			}
			
			P.print("|Sending message...");
			channel.sendMessage("Pssst! " + member.getAsMention()).queue();
			
			P.print("|Waiting for local cache to refresh...");
			try {TimeUnit.MILLISECONDS.sleep(400);} catch (InterruptedException e) {SQLconnector.callError(e); e.printStackTrace();}
			
			P.print("|Deleting message...");
			List<Message> msgs = channel.getHistory().retrievePast(1).complete();
			msgs.forEach((m) -> m.delete().queue());
			P.print("Message deleted. " + member.getUser().getAsTag() + " was spooked successfully.");
			return;
		}
		
		
		//[CLEAN] Cleans the rules channel of any residue from failed deletions.
		else if (args[0].equalsIgnoreCase(Bot.prefix + "clean")) {
			P.print("\n[Returns] Clean request by " + requestby);
			String targetChannelID = SQLconnector.get("select * from botsettings where name = 'automention_on_join_channel_id'", "value", false);
			List<Message> history = event.getGuild().getTextChannelById(targetChannelID).getHistory().retrievePast(5).complete();
			for (Message m : history) {if (m.getContentRaw().startsWith("<@NMP>")) m.delete().queue(); break;}
			return;
		}
		
		//[RICKROLL] hehe
		else if (args[0].equalsIgnoreCase(Bot.prefix + "rickroll") && isAdmin) {
			P.print("\n[Returns] Rickroll request by " + requestby);
			String userId;
			try {userId = args[1];}
			catch (Exception e) {return;}
			
			userId = userId.replace("<", "");
			userId = userId.replace(">", "");
			userId = userId.replace("@", "");
			userId = userId.replace("!", "");
			
			//P.print(userId);
			
			DiscordUtil.send(event, "||" + event.getGuild().getMemberById(userId).getUser().getAsMention() + "Never gonna give you up~ Never gonna let you down~ Never gonna run around and desert you~||");
			return;
		}
		
		//[HELP] Sends a link to the GitHub repo.
		if (args[0].equalsIgnoreCase(Bot.prefix + "help")) {
			P.print("\n[Returns] Requesting help list: " + requestby);
			DiscordUtil.send(event, "Here's the link to the public GitHub repository. "
					+ "You can help debug and update this bot with your contributions!\nhttps://github.com/DefinitelyRus/LazyJavie/tree/UI-Branch"
					+ "\n\n**The available commands for " + Bot.VERSION + " are listed here:**\nhttps://github.com/DefinitelyRus/LazyJavie/tree/UI-Branch#commands");
			return;
		}
	}
}