package home.UI_Functions;

import java.awt.Choice;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.apache.commons.lang3.exception.ExceptionUtils;
import commands.ConsoleCmd;
import commands.Quit;
import home.Bot;
import home.LazyJavieUI;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConsoleCallables {
	
	//NEEDS UPDATING. Merge with LazyJavie.java to enable UI.
	public static void startBot(JButton button, String mode, Choice list) {
		if (mode.equals("Start Bot")) {
			Bot.isAwake = true;
			button.setEnabled(false);
			button.setText("Stop");
			list.setEnabled(true);
			list.removeAll();
			LazyJavieUI.getConsoleInput().setEnabled(true);
			LazyJavieUI.getSendButton().setEnabled(true);
			LazyJavieUI.getConsoleOutput().setEnabled(true);
			
			//Determines whether the token should be grabbed from the UI or from system.
			String bottoken = String.valueOf(LazyJavieUI.getBotTokenField().getPassword());
			P.print(bottoken);
			if (bottoken.equals("")) {Bot.tokenOverride = false;}
			else {Bot.tokenOverride = true; Bot.token = bottoken;}
			
			//Finally starts the bot.
			Bot.start();
			
			//Waits until the bot is ready. (Otherwise, the code will continue but the cache isn't yet ready, causing errors.)
			try {Bot.jda.awaitReady();}
			catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			catch (NullPointerException e) {SQLconnector.callError(e.toString() + " - likely caused by bad connection.", ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			button.setEnabled(true);
			
			list.add("- Select text channel -");
			
			List<Member> membersNew = new LinkedList<Member>();
			for (Guild g : Bot.jda.getGuilds()) {
				
				//Adds all members the bot can see into a list and excludes all duplicates.
				for (Member m : g.getMembers()) {
					if (!membersNew.contains(m)) {
						membersNew.add(m);
					}
				}
				
				//Lists all text channels in the server for use in UI.
				for (TextChannel txtCh : g.getTextChannels()) {
					String label = g.getName() + " | " + txtCh.getName();
					list.add(label);
					LazyJavieUI.channelsList.add(txtCh);
					LazyJavieUI.channelDict.put(label, txtCh);
				}
			}
			
			P.print("Console ready!");
			return;
		
		} else if (mode.equals("Stop")) {
			Bot.isAwake = false;
			String ch = list.getSelectedItem();
			Quit.softExit();
			
			Bot.currentChannel = LazyJavieUI.channelDict.get(ch);
			
			//Look for an alternative way to delay changing the text
			//try {Bot.jda.awaitStatus(Status.SHUTDOWN);}
			//catch (InterruptedException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			button.setText("Start Bot");
			LazyJavieUI.getConsoleInput().setEnabled(false);
			LazyJavieUI.getSendButton().setEnabled(false);;
			list.setEnabled(false);
			list.removeAll();
			list.add("- Select text channel -");
			return;
		}
	}

	public static void send(Choice list, JTextField input) {
		String selChannel = list.getSelectedItem();
		String inputMsg = input.getText();
		
		if (selChannel.equals("- Select text channel -")) {P.print("No channel selected."); return;}
		
		//Directs all commands to the selected channel.
		LazyJavieUI.filterChannelName(selChannel);
		Bot.currentChannel = LazyJavieUI.channelDict.get(selChannel);
		
		//Sends as normal messages
		try {
			if (!inputMsg.startsWith(Bot.prefix) && !inputMsg.equals("")) {
				Bot.jda.getTextChannelById(LazyJavieUI.channelDict.get(selChannel).getId()).sendMessage(inputMsg).queue();
				input.setText("");
			} else if (inputMsg.startsWith(Bot.prefix) && !inputMsg.equals("")) {
				ConsoleCmd.call(input.getText());
			}
		}
		catch (NullPointerException e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print("NullPointerException: Text channel likely not found.");}
		//Sends as commands
		//else {MessageReceivedEvent.call(inputMsg);}
	}
	
}
