package home;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import commands.NewMemberPrompter;
import commands.Quit;
import commands.Returns;
import commands.TicketAutoPrompter;
import commands.TicketHostBuilder;
import commands.TicketMatchup;
import commands.TicketMatchupBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	
	//Initialization of user-coded objects and variables
	public static final String VERSION = "LazyJavie v2.0 ALPHA";
	public static JDA jda;
	public static boolean isAwake = false;
	public static String ticketMessage = "Need help? Click the emote below!";
	public static MessageEmbed ticketEmbed = null;
	public static List<Integer> activeTickets = new LinkedList<Integer>();
	public static List<Integer> activeMatches = new LinkedList<Integer>();
	
	//Variables changeable via commands.
	public static boolean tokenOverride = false;
	public static String token = "";
	public static String prefix = "$";
	public static Object currentChannel;
	public static boolean muted = false;
	public static boolean ticketsEnabled = false;
	public static String modRoomId = "821027416607555615";
	
	public static boolean start() {
		try {
			P.print("[Bot] Starting " + VERSION + "...");
			//[A] Getting the Token----------------------------------------
			
			/*
			 * This part is important because if this step is skipped,
			 * the discord bot's API token is visible publicly on GitHub.
			 * 
			 * This solution allows the token to be stored locally on the host's computer,
			 * while still having the source code be readily available on the GitHub repository.
			 */
			
			if (tokenOverride == false) {
				//Looks for the text file "lazyjavie_token.txt".
				P.print("|Getting token from file");
				File file = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "\\lazyjavie_token.txt");
				P.print("|File found.");
				
				//Scans the file.
			    Scanner scanner = new Scanner(file);
			    P.print("|Scanning...");
			    token = scanner.nextLine();
			    P.print("|Token assigned: " + StringUtils.substring(token, 0, 10) + "*".repeat(token.length()-10));
			    
			    //Closes the scanner.
			    scanner.close();
			    P.print("|Scanner closed.");
			    
			} else {P.print("|Getting token from control panel"); P.print("[A-2] Token assigned: " + token);}
		    
		    
			//[B] Logging in the bot----------------------------------------
			P.print("|Logging in...");
			try {
				jda = JDABuilder.createDefault(token)
						.setChunkingFilter(ChunkingFilter.ALL)
						.setMemberCachePolicy(MemberCachePolicy.ALL)
						.setEnabledIntents(EnumSet.allOf(GatewayIntent.class))
						.build();
				}
			catch (LoginException e) {P.print("'" +token+ "' is not a valid token."); return false;}
			catch (ErrorResponseException e) {P.print(e.toString() + " - likely caused by bad connection."); return false;}
			catch (Exception e) {P.print(e.toString());}
			
			
			P.print("|Setting status...");
			jda.getPresence().setStatus(OnlineStatus.ONLINE);
			//jda.getPresence().setActivity(Activity.listening("to Rick Astley - Never Gonna Give You Up"));
			
			P.print("|Opening to commands...");
			//[IMPORTANT] Add new commands here.
			jda.addEventListener(new Quit());
			jda.addEventListener(new Returns());	
			jda.addEventListener(new TicketAutoPrompter()); //Removed temporarily.
			jda.addEventListener(new NewMemberPrompter());
			jda.addEventListener(new TicketMatchup());
			jda.addEventListener(new TicketMatchupBuilder());
			jda.addEventListener(new TicketHostBuilder());
			
			P.print("Ready!");
			TimeUnit.MILLISECONDS.sleep(1000);
			LazyJavie.isReady = true;
			DiscordUtil.sendLog("900289688475144202", "913282006383722527", "");
			return true;
		}
		//[A] Case: File not found.
		catch (FileNotFoundException e) {
			P.print("Missing file error:\n" + e.toString());
			SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e));
			return false;
		}
		//[A] Case: File empty.
		catch (NoSuchElementException e) {
			P.print("Empty file error:\n" + e.toString());
			SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e));
			return false;
		}
		//[B] Case: Bot likely not initialized
		catch (NullPointerException e) {
			P.print(e.toString() + " - Likely caused by a bad or no connection or an invalid token.");
			SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e));
			return false;
		}
		//[A-B] Case: Every other exception.
		catch (Exception e) {
			P.print(ExceptionUtils.getStackTrace(e));
			SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
}
