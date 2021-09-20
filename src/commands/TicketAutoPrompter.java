package commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import home.Bot;
import home.P;
import home.SQLconnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketAutoPrompter extends ListenerAdapter {
	final static int color = 0xFF8822;
	final static String utf1 = "\u0031\u20E3";
	final static String utf2 = "\u0032\u20E3";
	final static String utf3 = "\u0033\u20E3";
	final static String utf4 = "\u0034\u20E3";
	final static String utf5 = "\u0035\u20E3";
	final static String utfCheck = "\u2705";
	
	public void onTextChannelCreate (TextChannelCreateEvent event) {
		String chName = event.getChannel().getName();
		P.print("New Channel Detected: " + chName);
		
		if (!Bot.ticketsEnabled) {P.print("\n[TicketAutoPrompter] (Ignore) Ticket created; response disabled."); return;}
		
		if (chName.matches("ticket-[0-9]{4}")) {
			TextChannel channel = event.getChannel();
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(color);
			embed.setTitle("Need help?");
			embed.setDescription("\nPress :one: if you're having trouble **joining for the first time**." +
								"\n\nPress :two: if you **lost items** due to a **crash or glitch**." +
								"\n\nPress :three: if you've been **griefed or stolen from**." +
								"\n\nPress :four: if you **can't join** the server." +
								"\n\nPress :five: if it's **NONE** of the above.");
			embed.setFooter("DO NOT SKIP THIS PROMPT. This will help us gather information to fix your issue.\n" +
								"If the emotes don't appear automatically, you can still add them yourself or skip this process entirely.");
			embed.setAuthor("TetrabearMC <@0>");
			MessageEmbed msgEmbed = embed.build();
			
			P.print("Detected new ticket!");
			channel.sendMessage(msgEmbed).queue();
			
			try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			
			List<Message> msgs = Bot.jda.getTextChannelById(channel.getId()).getHistory().retrievePast(1).complete();
			Message promptMsg = null;
			
			for (Message m : msgs) {
				m.getEmbeds().forEach((x) -> P.print(x.getAuthor().getName()));
				try {
					//Application queries
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//Lost items
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//Griefing reports
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//Joining issues
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//Others
					m.addReaction(utf5).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
				
				//Assigns the current message to a variable for later reference.
				promptMsg = m;
			}
			
			if (promptMsg.equals(null)) {
				P.print("[TicketAutoPrompter] Issue: Prompt message not found.");
				event.getChannel().sendMessage("[TicketAutoPrompter] Encountered an unexpected error.").queue();
				return;
			}
			
			promptMsg.getId();
		}
	}
	
	//Implement a non-binary tree to allow for modular and easily traversable data structure.
	//This current one is awful to navigate through.
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Cancels if it detects itself or (temp) user is not admin.
		if (event.getUserId().equals(Bot.jda.getSelfUser().getId())) return;
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;
		
		String channelName = event.getChannel().getName();
		String channelId = event.getChannel().getId();
		
		//Console print if name matches, cancel if not.
		if (channelName.matches("ticket-[0-9]{4}")) {P.print("[TicketAutoPrompter] Deploying ticket auto-responder to " + channelName + "...");}
		else return;

		final String cp1 = "U+31U+20e3";
		final String cp2 = "U+32U+20e3";
		final String cp3 = "U+33U+20e3";
		final String cp4 = "U+34U+20e3";
		final String cp5 = "U+35U+20e3";
		final String cpCheck = "U+2705";
		
		List<Message> msgHistory = Bot.jda.getTextChannelById(channelId).getHistory().retrievePast(100).complete();
		for (Message m : msgHistory) {
			boolean isSelf = m.getMember().getId().equals(Bot.jda.getSelfUser().getId());
			if (!isSelf) continue;
			
			//Initialization
			String emoteCodePoint = event.getReactionEmote().getAsCodepoints();
			String coords = null;
			List<MessageEmbed> embeds = m.getEmbeds();
			for (MessageEmbed MeEm : embeds) {coords = MeEm.getAuthor().getName().replace("TetrabearMC ", "");}
			//Yes I went with that name (MeEm) just because it's funny.
			
			P.print(emoteCodePoint);
			/*
			 * INSTRUCTIONS:
			 * For those reading through this mess of a workaround, good luck, but here's some instructions to help you.
			 * 
			 * [@n] refers to the number of steps the member has already traversed through.
			 * n is the path the member has taken, this is for every path resulting in different outputs.
			 */
			
			//[@0]		----------------------------------------------------------------------------------------------------
			//DONE
			if (coords.equals("<@0>")) {
				P.delete(m);
				switch (emoteCodePoint) {
				
				//[@1] Path newJoin
				case cp1:
					newJoin(event);
					break;
					
				//[@2] Path lostItem
				case cp2:
					lostItem(event);
					break;
				
				//[@3] Path griefTheft
				case cp3:
					griefTheft(event);
					break;
					
				//[@4] Path cantConnect
				case cp4:
					cantConnect(event);
					break;
					
				//[@5] Path unspecificQuery
				case cp5:
					unspecificQuery(event);
					break;
				}
			}
			
			//[@1]		----------------------------------------------------------------------------------------------------
			else if (coords.startsWith("<@1")) {
				
				//[@1] Path newJoin				--------------------------------------------------
				if (coords.equals("<@1>")) {
					switch (emoteCodePoint) {
					
					//[@1-1] Path nonPremiun
					case cp1:
						P.delete(m);
						nonPremium(event);
						break;
						
					//[@1-2] TODO Path badIp
					case cp2:
						P.delete(m);
						badIp(event);
						break;
					
					//[@1-3] TODO Path discordNotLinked
					case cp3:
						P.delete(m);
						discordNotLinked(event);
						break;
						
					//[@1-4] TODO Path otherBadConn
					case cp4:
						P.delete(m);
						unspecificQuery(event);
						break;
					}
				}
				
				//TODO @1-1
				else if (coords.startsWith("<@1-1")) {
					
					//[@1-1] Path nonPremium		--------------------------------------------------
					if (coords.equals("<@1-1>")) {
						switch (emoteCodePoint) {
						
						//[@1-1-1] Path needWhitelist		-------------------------
						case cp1:
							P.delete(m);
							needWhitelist(event);
							return;
						}
					}
					
					//[@1-1-1] Path needWhitelist	-------------------------
					//DONE
					else if (coords.equals("<@1-1-1>")) {
						if (emoteCodePoint.equals(cpCheck)) {
							P.delete(m);
							P.send(event, "The <@&311398364237266945> will be assisting you as soon as possible. Please remain patient.");
							return;
						}
					}

					//[@1-1-2] Path allDone1		-------------------------
					else if (coords.startsWith("<@1-1-2")) {
						
					}

					//[@1-1-3] Path otherCantJoin	-------------------------
					else if (coords.startsWith("<@1-1-3")) {
						
					}
				}
				
				//TODO @1-2
				else if (coords.startsWith("<@1-2")) {

					//[@1-2] Path badIp				--------------------------------------------------
					if (coords.equals("<@1-2>")) {
						
					}
				}
				
				//TODO @1-3
				else if (coords.startsWith("<@1-3")) {

					//[@1-3] Path discordNotLinked	--------------------------------------------------
					if (coords.equals("<@1-3>")) {
						
					}
				}
				
				//TODO @1-4
				else if (coords.startsWith("<@1-4")) {

					//[@1-4] Path otherBadConn		--------------------------------------------------
					if (coords.equals("<@1-4>")) {
						
					}
				}
			}
			
			else if (coords.startsWith("<@2")) {
				
				//[@2] Path lostItem				--------------------------------------------------
				if (coords.equals("<@2>")) {
					P.delete(m);
				}

				//[@2-1] Path deathLost				-------------------------
				else if (coords.startsWith("<@2-1")) {
					
				}

				//[@2-2] Path deathLost				-------------------------
				else if (coords.startsWith("<@2-2")) {
					
				}

				//[@2-3] Path deathLost				-------------------------
				else if (coords.startsWith("<@2-3")) {
					
				}
			}
			
			else if (coords.startsWith("<@3")) {

				//[@3] Path griefTheft				--------------------------------------------------
				if (coords.equals("<@3>")) {
					P.delete(m);
				}
			}
			
			else if (coords.startsWith("@<4")) {
				
				//[@4] cantConnect					--------------------------------------------------
				if (coords.equals("<@4>")) {
					P.delete(m);
				}
			}
		}
	}
	
	//[@1] newJoin								---------------------------------------------------------------------------
	private static void newJoin(GuildMessageReactionAddEvent event) {
		String desc = "**What does it say when you try to join?**\n\n" + 
					"Press :one: if `Only premium Minecraft accounts are allowed to join the server.`\n\n" +
					"Press :two: if `io.netty.channel.abstractchannel$annotatedconnectexception`\n\n" +
					"Press :three: if **you can join** but you **can't move** in-game.\n\n" +
					"Press :four: if it's **none of the above**.\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("So you're having trouble joining **for the first time**...");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(500);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1>")) {
				try {
					//[@1-1] nonPremium
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-2] badIp
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-3] discordNotLinked
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-4] otherBadConn
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}
	
	//[@1-1] nonPremium							--------------------------------------------------
	private static void nonPremium(GuildMessageReactionAddEvent event) {
		boolean isMember = false;
		List<Role> memRoles = event.getMember().getRoles();
		for (Role r : memRoles) {if (r.getName().equalsIgnoreCase("Member")) isMember = true;}
		
		String desc = "**Do the steps in #how-to-join first!**\n\n" + 
				"Press :one: if you're playing on a **cracked account**.\n\n" +
				"Press :two: if you're no longer have this issue.\n\n" +
				"Press :three: if you're **none** of the above.\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		if (!isMember) desc = desc + "\n**IMPORTANT:**\nDo step 1 in #how-to-join first. This will save you plenty of trouble later on.\n";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("Are you playing on a cracked account?");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1-1>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1-1>")) {
				try {
					//[@1-1-1] needWhitelist
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-1-2] allDone1-1
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);

					//[@1-1-3] otherCantJoin
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}
	
	//[@1-1-1] needWhitelist
	private static void needWhitelist(GuildMessageReactionAddEvent event) {
		String desc = "**Please send us these details:**\n\n" +
					"- Your in-game username: \n\n" +
					"- DiscordTag#XXXX of who invited you: \n\n" +
					"Press :white_check_mark: when you're done!\n";
		String footer = "If this is an ALT account, please include your main account's DiscordTag#XXXX as well.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("You need to be whitelisted!");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1-1-1>");
		P.send(event, embed.build());

		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1-1-1>")) {
				try {
					//[@1-1-1 Done]
					m.addReaction(utfCheck).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}
	//[@1-2] badIp
	private static void badIp(GuildMessageReactionAddEvent event) {
		String desc = "`tetrabear.leanghosting.com` or `play.tetrabear.xyz:32115`\n\n" + 
				"Press :one: if you are now **able to join**.\n\n" +
				"Press :two: if you are **still unable to join**.\n\n" +
				"Press :three: if it **shows a different error code**.\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("Try these IP addresses!");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1-2>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1-1>")) {
				try {
					//[@1-2-1] allDone1-2
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-2-2] clientIssue
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);

					//[@1-2-3] otherError
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}
	
	//[1-3] discordNotLinked
	private static void discordNotLinked(GuildMessageReactionAddEvent event) {
		boolean isMember = false;
		List<Role> memRoles = event.getMember().getRoles();
		for (Role r : memRoles) {if (r.getName().equalsIgnoreCase("Member")) isMember = true;}
		
		String desc = "**Do the steps in #how-to-join first!**\n\n" + 
				"Press :one: if you've **already followed the instructions** in #rules.\n\n" +
				"Press :two: if `/discord link` **command isn't working**.\n\n" +
				"Press :three: if you **sent @TetrabearMC** the 4-digit code, but **nothing happened**.\n\n" +
				"Press :four: if it's **NONE of the above**.";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		if (!isMember) desc = desc + "\n**IMPORTANT:**\nDo step 1 in #how-to-join first. You **need** this in order to proceed.\n";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("Are you playing on a cracked account?");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1-1>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1-1>")) {
				try {
					//[@1-3-1] followedInstructions
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-3-2] commandNotWorking
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);

					//[@1-3-3] botNotWorking
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-3-4] otherLinkFail
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}
	
	//[@2] lostItem								---------------------------------------------------------------------------
	private static void lostItem(GuildMessageReactionAddEvent event) {
		String desc = "\n" + 
				"Press :one: if you **died due to lag or a glitch**.\n\n" +
				"Press :two: if you lost items/blocks due to **lag, server crash, or a glitch.**\n\n" +
				"Press :three: if you think **you've been stolen from.**\n\n" +
				"Press :four: if it's **none of the above.**\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("How did you lose your items?");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@2>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@2>")) {
				try {
					//[@2-1] deathLost
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@2-2] crashLost
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);

					//[@3-3] theftLost
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@2-3] otherLost
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
	}

	//[@3] griefTheft							---------------------------------------------------------------------------
	private static void griefTheft(GuildMessageReactionAddEvent event) {
		String desc = "\n" + 
				"Press :one: if you were **killed without consent**.\n\n" +
				"Press :two: if a **structure you built was damaged or altered** significantly without permission.\n\n" +
				"Press :three: if you think **you've been stolen from.**\n\n" +
				"Press :four: if it's **none of the above.**\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("Describe how you were griefed.");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@3>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@3>")) {
				try {
					//[@3-1] deathGrief
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@3-2] structureGrief
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);

					//[@3-3] theftLost
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@3-4] otherGrief
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
		
	}

	//[@4] cantConnect							---------------------------------------------------------------------------
	private static void cantConnect(GuildMessageReactionAddEvent event) {
		String desc = "**What does it say when you try to join?**\n\n" + 
				"Press :one: if `Only premium Minecraft accounts are allowed to join the server.`\n\n" +
				"Press :two: if `io.netty.channel.abstractchannel$annotatedconnectexception`\n\n" +
				"Press :three: if **you can join** but you **can't move** in-game.\n\n" +
				"Press :four: if you think the **server has crashed**.\n\n" +
				"Press :five: if it's **none of the above**.\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("Can't join?");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@1-1>");
		P.send(event, embed.build());
		
		try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
		
		List<Message> history = event.getChannel().getHistory().retrievePast(100).complete();
		
		for (Message m : history) {
			String coords = null;
			for (MessageEmbed em : m.getEmbeds()) {coords = em.getAuthor().getName().replace("TetrabearMC ", "");}
			
			if (m.getMember().getId().equals(Bot.jda.getSelfUser().getId()) && coords.equals("<@1-1>")) {
				try {
					//[@1-1] nonPremium
					m.addReaction(utf1).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-2] badIp
					m.addReaction(utf2).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-3] discordNotLinked
					m.addReaction(utf3).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@4-1] serverCrash
					m.addReaction(utf4).queue();
					TimeUnit.MILLISECONDS.sleep(100);
					
					//[@1-4] otherBadConn
					m.addReaction(utf5).queue();
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {SQLconnector.callError(e.toString(), ExceptionUtils.getStackTrace(e)); P.print(e.toString());}
			}
		}
				
	}
	
	//[@5] unspecifiedQuery						---------------------------------------------------------------------------
	private static void unspecificQuery(GuildMessageReactionAddEvent event) {
		String desc = "Tell us about your issue and we'll talk about it as soon as we can.\n" +
				"Provide detailed information such as... \n" +
				"\nWHAT: \nWHEN: \nWHO: \nWHERE: \nWHY: \nHOW: \n\n(Put \"N/A\" if you're unsure or if it's unapplicable.)\n";
		String footer = "PLEASE BE PATIENT! DO NOT @mention unless absolutely necessary.";
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle("What's up?");
		embed.setDescription(desc);
		embed.setFooter(footer);
		embed.setAuthor("TetrabearMC <@5>");
		P.send(event, embed.build());
	}
}
