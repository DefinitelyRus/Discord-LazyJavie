# LazyJavie
LazyJavie 2.2 is a multi-purpose Discord Bot instruction set with multiple unique features, many of which are still in development.

### Private chat with random strangers!
A place where members can privately talk with strangers, anonymously or otherwise. All they have to do is click on a button to queue up!
It features a REPORT system where either one of the participants can flag the conversation as
suspicious which then allows assigned moderators to view the conversation and take action accordingly.

# Commands
## Quit
* `$quit` - This forces the bot to shut itself off. **This requires TRUE Administrator permissions.**

*Preview: Entering $quit.*<br>
![image](https://user-images.githubusercontent.com/72731965/145540297-efcae7a0-1f30-48d4-aad1-fd260fca6d5a.png)
<br>*Preview: Successfully exits the bot.*<br>
![image](https://user-images.githubusercontent.com/72731965/145540483-9d0a5479-ef5a-4fbe-97be-8ac6c234e943.png)

## TicketMatchup
Commands used on existing matchup channels. These serve as backup, in case the existing systems fail.

*Preview: Anonymous messaging! You can also send images and other media (using links)!*<br>
![image](https://user-images.githubusercontent.com/72731965/145543478-06cd826d-98be-4b80-8c2e-c2f343d9c18a.png)

### End
`$end` - Ends the current matchup.

*Preview: You can use $end to end the match.*<br>
![image](https://user-images.githubusercontent.com/72731965/145541714-d0c406a5-8588-4db3-bb24-56cf4342c490.png)

### Report
`$report` - Ends the current matchup and gives pre-selected moderators access to both channels.

*Preview: You can $report sketchy conversations!*<br>
![image](https://user-images.githubusercontent.com/72731965/145542123-3fed9884-ea17-42bd-8239-cf0344f45980.png)

### Delete
`$delete` - Deletes the matchup archive. This only works on channels named #close-XXXXX.
<br>*No preview. This deletes the channels immediately after sending.*

## TicketMatchupBuilder
Commands used to create the matchup queue system.

### Set Matchup Message
`$setMatchupMessage <message..>` - Sets the matchup queue message. This is the message that will appear on the message users will click on to queue up.

*Preview: What to expect; see $setMatchupChannel for more images.*<br>
![image](https://user-images.githubusercontent.com/72731965/145537462-e19729f2-3483-4a88-8582-01b86ef20685.png)

### Set Matchup Embed
`$setMatchupEmbed <args...>` - Same as the one above, except this one's an embed builder.
<br>*No preview. This feature is currently known as bugged.*

### Set Matchup Channel
`$setMatchupChannel <@role> <#channel> <category_name> <archive_category>* <emote1>* <emote2>* <emote3>*` - Sets which role will act as moderators, where the matchup queue message will be sent, where new matchup channels will be created on, where they will be moved to upon closing*, and the 3 custom emotes*.

*Preview: What to expect*<br>
![image](https://user-images.githubusercontent.com/72731965/145539677-a597022a-66d3-45e7-b422-d53d9d3484f8.png)
<br>*Preview: The message set in $setMatchupMessage is used in the creation of the queue message.*<br>
![image](https://user-images.githubusercontent.com/72731965/145539860-d1545da7-5aee-42e4-9abc-99a6a1872e13.png)

### Set Archive Category
`$setArchiveCategory <archive_category>` - Sets where the closed matchups will be moved to.
<br>*No preview. It works but doesn't output anything.*

## NewMemberPrompter
* `$setAutomentionChannel <#channel>` - New Discord members will be mentioned in the selected channel upon joining the server.

*Preview: What to expect*<br>
![image](https://user-images.githubusercontent.com/72731965/145536837-7679eb67-b323-45d8-893a-1654436d554a.png)
<br>*Preview: The bot mentions you, then deletes the message shortly after.*<br>
![image](https://user-images.githubusercontent.com/72731965/145537012-846c86c1-d828-4b56-95c1-1b6513bfb890.png)

## Returns
Random commands that don't need their own file, for now.
* `$botToken` - Displays the bot's token. ;)
* `$ping` - Sends the latency of the bot. From you, to Discord, to LazyJavie's host, to Discord again.
* `$test` - Will perform a series of tests, checking if the bot's functions still work.
* `$spamConsole` - Spams the console. **Only labeled admins, channel managers, and true admins can use this command.**
* `$cmd <message..>` - Sends a message to the console. **Only labeled admins, channel managers, and true admins can use this command.**
* `$hiddenPing <@user>` - Silently pings a user. User IDs also work. **Only labeled admins, channel managers, and true admins can use this command.**
* `$clean` - In case NewMemberPrompter didn't automatically delete the mention message, use this command to remove it.
* `$rickroll <@user>` - Rickrolls the mentioned user.

# Installation
See the [LazyJavie releases page](https://github.com/DefinitelyRus/LazyJavie/releases) for detailed instructions fit for the version you're interested in using.
 
# How to invite
Watch the **How to invite** portion of [this video](https://youtu.be/Dq40V9BhbwU?t=202).
 
# Future Updates
Too many ideas, too little time. If you want your idea implemented, perhaps you should contribute to the project!

# Project Contributors
### The sole developer.
- [@DefinitelyRus](https://github.com/DefinitelyRus)

# License
This project uses GNU General Public License v3.0. See LICENSE for more information.

