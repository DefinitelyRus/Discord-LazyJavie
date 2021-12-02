package commands;

import java.util.concurrent.TimeUnit;

import home.Bot;
import home.DiscordUtil;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This closes the bot's connection to Discord servers making it effectively offline
 * or exits the program entirely.
 * @author DefinitelyRus
 */
public class Quit extends ListenerAdapter{
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getMessage().getContentRaw().startsWith(Bot.prefix)) return;
		
		
		String[] args = event.getMessage().getContentRaw().split("\\s+");
		
		try {
			if (!args[0].equalsIgnoreCase(Bot.prefix + "quit")) return;
			boolean isAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
			
			//Case: Unfinished command.
			P.print("\n[quit] Exit request by: " + event.getMember().getUser().getName());
			
			//Case: User is an admin and completes the default prompt.
			if (args[1].equalsIgnoreCase("confirm") && isAdmin == true) {
				P.print("Shutting down LazyJavie 2.0 ALPHA.");
				DiscordUtil.send(event, "Bye bye!");
				TimeUnit.SECONDS.sleep(5);
				exit();
				return;
			}

			//Case: User is not an admin.
			else if (isAdmin == false) {
				P.print("Requester is not an admin; cancelling.");
				DiscordUtil.send(event, "Only admins can do that!");
				return;
			}
			
			//Case: User is an admin and completes the soft-exit prompt.
			else if (args[0].equalsIgnoreCase(Bot.prefix + "quit") && args[1].equalsIgnoreCase("confirmKeepUI") && isAdmin == true) {
				DiscordUtil.send(event, "Bye bye!");
				TimeUnit.SECONDS.sleep(5);
				softExit();
				return;
			}

			//Case: User is an admin, but doesn't complete the prompt.
			else if (args[0].equalsIgnoreCase(Bot.prefix + "quit") && isAdmin == true) {
				P.print("args[1] is not 'confirm'; cancelling.");
				DiscordUtil.send(event, args[1] + " is not 'confirm' or 'confirmKeepUI'.");
				return;
			}
		
		//Missing arguments
		} catch (ArrayIndexOutOfBoundsException e) {
			P.print("Error ignored: Missing args.");
			DiscordUtil.send(event, ":warning: Are you sure you want to disconnect the bot?\nEnter `" +Bot.prefix+ "quit confirm` to confirm.");
			return;
		
		//Any other error
		} catch (Exception e) {SQLconnector.callError(e); P.print(e.toString()); return;}
	}
	
	//For use in UI.
	public static void softExit() {
		P.print("Shutting down " + Bot.VERSION);
		try {TimeUnit.SECONDS.sleep(5);}
		catch (InterruptedException e) {SQLconnector.callError(e); P.print(e.toString());}
		Bot.jda.shutdown();
	}
	
	public static void exit() {
		System.exit(0);
		return;
	}
}