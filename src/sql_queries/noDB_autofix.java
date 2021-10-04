/*
 * This thing does stuff.
 */

package sql_queries;

public class noDB_autofix {
	public static String[] get(String mode) {
		
		
		//TODO Update newDB.
		final String[] sqlite = {
		"drop table members;",
		"drop table botsettings;",
		"drop table cmdlog;",
		"drop table errorlog;",
		"drop table ticketlog;",
		"drop table matchlist;",
		
		"create table members (userid varchar(128) not null, usertag varchar(128) default null);",
		"create table botsettings (name varchar(128) not null, value varchar(256) default null, last_modified datetime, primary key(name));",
		"create table cmdlog (id int not null, userid varchar(256), userquery varchar(1024), errorid varchar(128), eventdate datetime, primary key(id));",
		"create table errorlog (err_type varchar(1024), err_stacktrace varchar(65535), eventdate datetime, appver varchar(32));",
		"create table ticketlog (ticketid varchar(5), userid varchar(128), date_created datetime, date_closed datetime default null, primary key (ticketid));",
		"create table matchlist (id varchar(128) not null, codename varchar(128), matchcode varchar(10), primary key(id));",
		
		
		//Bot Settings: name, value, last_modified
		"insert into botsettings values " +
				"('discord_bot_token_override', null, datetime())," +
				"('ticket_message_id', null, datetime()), " +
				"('ticket_channel_id', null, datetime()), " +
				"('ticket_category_id', null, datetime())," +
				"('ticket_archive_cat_id', null, datetime())," +
				"('ticket_responder_role_id', null, datetime())," +
				"('automention_on_join_channel_id', null, datetime())," +
				"('matchup_message_id', null, datetime()), " +
				"('matchup_channel_id', null, datetime()), " +
				"('matchup_category_id', null, datetime())," +
				"('matchup_archive_cat_id', null, datetime())," +
				"('matchup_moderator_role_id', null, datetime());"
		/*
		,"drop table testtable;",
		"create table testtable (id integer primary key autoincrement not null, col1 varchar(24), col2 varchar(24), col3 varchar(24), col4 varchar(24), col5 varchar(24), col6 varchar(24), col7 varchar(24));"
		*/
		};
		
		switch (mode) {
			case "sqlite":
				return sqlite;
			default:
				return sqlite; //Temp
		}
	}
}