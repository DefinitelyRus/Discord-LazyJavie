# LazyJavie
LazyJavie 2.2 is a multi-purpose Discord Bot instruction set with multiple unique features, many of which are still in development.

## Features
### Private chat with random strangers!
A place where members can privately talk with strangers, anonymously or otherwise. All they have to do is click on a button to queue up!
It features a REPORT system where either one of the participants can flag the conversation as
suspicious which then allows assigned moderators to view the conversation and take action accordingly.

## Commands
### Quit
* `$quit` - This forces the bot to shut itself off. **This requires TRUE Administrator permissions.**

### TicketMatchup
Commands used on existing matchup channels. These serve as backup, in case the existing systems fail.
* `$end` - Ends the current matchup.
* `$report` - Ends the current matchup and gives pre-selected moderators access to both channels.
* `$delete` - Deletes the matchup archive. This only works on channels named #close-XXXXX.

### TicketMatchupBuilder
Commands used to create the matchup queue system.
* `$setMatchupMessage <message..>` - Sets the matchup queue message. This is the message that will appear on the message users will click on to queue up.
* `$setMatchupEmbed <args...>` - Same as the one above, except this one's an embed builder.
* `$setMatchupChannel <@role> <#channel> <category_name> <archive_category>* <emote1>* <emote2>* <emote3>*` - Sets which role will act as moderators, where the matchup queue message will be sent, where new matchup channels will be created on, where they will be moved to upon closing*, and the 3 custom emotes*.
* `$setArchiveCategory <archive_category>` - Sets where the closed matchups will be moved to.

### NewMemberPrompter
* `$setAutomentionChannel <#channel>` - New Discord members will be mentioned in the selected channel upon joining the server.

### Returns
Random commands that don't need their own file, for now.
* `$botToken` - Displays the bot's token. ;)
* `$ping` - Sends the latency of the bot. From you, to Discord, to LazyJavie's host, to Discord again.
* `$test` - Will perform a series of tests, checking if the bot's functions still work.
* `$spamConsole` - Spams the console. **Only labeled admins, channel managers, and true admins can use this command.**
* `$cmd <message..>` - Sends a message to the console. **Only labeled admins, channel managers, and true admins can use this command.**
* `$hiddenPing <@user>` - Silently pings a user. User IDs also work. **Only labeled admins, channel managers, and true admins can use this command.**
* `$clean` - In case NewMemberPrompter didn't automatically delete the mention message, use this command to remove it.
* `$rickroll <@user>` - Rickrolls the mentioned user.

## Installation
See the [LazyJavie releases page](https://github.com/DefinitelyRus/LazyJavie/releases) for detailed instructions fit for the version you're interested in using.
 
## How to invite
Watch the **How to invite** portion of [this video](https://youtu.be/Dq40V9BhbwU?t=202).
 
## Future Updates
Too many ideas, too little time. If you want your idea implemented, perhaps you should contribute to the project!

## Code Contributors
### The sole developer.
- [@DefinitelyRus](https://github.com/DefinitelyRus)

## License
This project uses GNU General Public License v3.0. See LICENSE for more information.

