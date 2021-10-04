package home;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import home.UI_Functions.ConsoleCallables;
import net.dv8tion.jda.api.entities.TextChannel;


@SuppressWarnings("serial")
public class LazyJavieUI extends JFrame {

	private JPanel contentPane;
	private static JTextField consoleInput;
	private static JPasswordField botTokenField;
	private static JTable tableGrid;
	private JTextField startFromField;
	public static JTextArea consoleOutput;
	public static JButton startBotToggle;
	
	//Developer-written instantiations
	static boolean hideUI = false;
	private static Choice textChannelsList;
	private static JButton sendButton;
	public static List<TextChannel> channelsList = new ArrayList<TextChannel>();
	public static Dictionary<String, TextChannel> channelDict = new Hashtable<String, TextChannel>();
	private Choice tableList;
	private static JLabel entryCounterLabel;
	
	public static void main(String[] args) throws LoginException, SQLException{
		
		//Developer code intended to run before the instantiation of the UI.
		//Temporarily disabled; database not yet created.
		//String hideUI_asString = SQLconnector.get("select * from botsettings where id = 'hide_ui_on_close'", "value", false);
		//hideUI = Boolean.parseBoolean(hideUI_asString);
		
		//Pre-generated code
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LazyJavieUI frame = new LazyJavieUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//All startup settings goes here.
		P.print("UI Ready!");
		
		//Database reset - debugging purposes only. -----------------------------------------------------------------------------------------------------------------
		//SQLconnector.NoDBfixer("sqlite");
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConsoleCallables.startBot(getStartBotToggle(), LazyJavieUI.startBotToggle.getText(), getTextChannelsList());
	}
	
	public static int willHideOnClose(boolean toHide) {
		if (toHide == true) {return JFrame.HIDE_ON_CLOSE;}
		else {return JFrame.EXIT_ON_CLOSE;}
	}

	public LazyJavieUI() {
		setDefaultCloseOperation(willHideOnClose(hideUI));
		setTitle("LazyJavie Host Control Panel");
		setResizable(false);
		setBackground(SystemColor.text);
		setBounds(100, 100, 720, 540);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.menu);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		//Sets the look & feel of the UI. This is set to be the same as the system it's running on.
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			P.print(e.toString());
			e.printStackTrace();
		}
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(SystemColor.menu);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		//-----------------------------------CONSOLE PANEL-----------------------------------
		
		JPanel consolePanel = new JPanel();
		consolePanel.setBackground(SystemColor.menu);
		consolePanel.setBorder(null);
		tabbedPane.addTab("Console", null, consolePanel, "The console can be controlled and viewed through this tab.");
		consolePanel.setLayout(null);
		
		JScrollPane consoleScrollPane = new JScrollPane();
		consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		consoleScrollPane.setBounds(18, 21, 662, 294);
		consolePanel.add(consoleScrollPane);
		
		consoleOutput = new JTextArea();
		consoleOutput.setLineWrap(true);
		consoleOutput.setWrapStyleWord(true);
		consoleOutput.setBackground(SystemColor.text);
		consoleOutput.setEditable(false);
		consoleOutput.setColumns(82);
		consoleOutput.setRows(16);
		consoleOutput.setText("");
		consoleScrollPane.setViewportView(consoleOutput);
		consoleOutput.setEnabled(false);
		
		consoleInput = new JTextField();
		consoleInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					try {ConsoleCallables.send(getTextChannelsList(), getConsoleInput());}
					catch (IllegalStateException e) {P.print("Cannot send a message without content.");}
				}
			}
		});
		consoleInput.setBounds(18, 320, 662, 20);
		consoleInput.setColumns(82);
		consolePanel.add(consoleInput);
		consoleInput.setEnabled(false);
		
		textChannelsList = new Choice();
		textChannelsList.setBounds(18, 347, 373, 20);
		textChannelsList.setForeground(SystemColor.textText);
		textChannelsList.setBackground(SystemColor.text);
		consolePanel.add(textChannelsList);
		textChannelsList.add("- Select text channel -");
		textChannelsList.setEnabled(false);
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {ConsoleCallables.send(getTextChannelsList(), getConsoleInput());}
				catch (IllegalStateException e) {P.print("Cannot send a message without content.");}
			}
		});
		sendButton.setBounds(397, 344, 283, 23);
		consolePanel.add(sendButton);
		sendButton.setEnabled(false);
		
		//-------------------------START BUTTON-------------------------
		startBotToggle = new JButton("Start Bot");
		startBotToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConsoleCallables.startBot(getStartBotToggle(), LazyJavieUI.startBotToggle.getText(), getTextChannelsList());
			}
		});
		startBotToggle.setBounds(600, 439, 89, 23);
		consolePanel.add(startBotToggle);
		
		JLabel lblNewLabel_1 = new JLabel("When changing channels, press send to focus on the selected channel.");
		lblNewLabel_1.setBounds(18, 373, 341, 14);
		consolePanel.add(lblNewLabel_1);
		
		//-----------------------------------SETTINGS PANEL-----------------------------------
		
		JTabbedPane optionsPanel = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.addTab("Options", null, optionsPanel, "All modifiable bot settings are included in here.");
		
		JPanel startupPanel = new JPanel();
		optionsPanel.addTab("Startup", null, startupPanel, null);
		startupPanel.setLayout(null);
		
		JCheckBox hideUIcheckbox = new JCheckBox("Hide UI on close.");
		hideUIcheckbox.setBounds(6, 7, 107, 23);
		startupPanel.add(hideUIcheckbox);
		
		JPanel authenticationPanel = new JPanel();
		optionsPanel.addTab("Authentication", null, authenticationPanel, "All required values needed to give you access to Console and other bot settings.");
		authenticationPanel.setLayout(null);
		
		JLabel botTokenLabel = new JLabel("Bot Token");
		botTokenLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		botTokenLabel.setBounds(10, 11, 80, 14);
		authenticationPanel.add(botTokenLabel);
		
		botTokenField = new JPasswordField();
		botTokenField.setToolTipText("If you want to use your own bot, put your bot's token here. Otherwise, leave it empty.");
		botTokenField.setColumns(256);
		botTokenField.setBounds(100, 8, 470, 20);
		authenticationPanel.add(botTokenField);
		
		JLabel lblNewLabel = new JLabel("If you want to use your own bot, put your bot's token here. Otherwise, leave it empty.");
		lblNewLabel.setBounds(46, 36, 420, 14);
		authenticationPanel.add(lblNewLabel);
		
		//-----------------------------------DATABASE TAB-----------------------------------
		
		JPanel databasePanel = new JPanel();
		tabbedPane.addTab("Database", null, databasePanel, "A place for all locally-stored tables.");
		databasePanel.setLayout(null);
		
		tableList = new Choice();
		tableList.setBounds(10, 6, 350, 20);
		databasePanel.add(tableList);
		dbTable.updateTableList(tableList);
		
		JButton updateTableButton = new JButton("Update table");
		updateTableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dbTable.updateTableDisplay(getTableList(), getTableGrid());
			}
		});
		updateTableButton.setToolTipText("Update the table.");
		updateTableButton.setBounds(529, 6, 160, 23);
		databasePanel.add(updateTableButton);
		
		JScrollPane dbTableScrollPane = new JScrollPane();
		dbTableScrollPane.setBounds(10, 32, 679, 413);
		databasePanel.add(dbTableScrollPane);
		
		tableGrid = new JTable();
		tableGrid.setColumnSelectionAllowed(true);
		tableGrid.setFillsViewportHeight(true);
		dbTableScrollPane.setViewportView(tableGrid);
		tableGrid.setModel(new DefaultTableModel(
			new Object[][] {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}},
			new String[] {"", "", "", "", "", "", "", ""}) {
			boolean[] columnEditables = new boolean[] {false, false, false, false, false, false, false, false};
			public boolean isCellEditable(int row, int column) {return columnEditables[column];}
		});
		tableGrid.setCellSelectionEnabled(true);
		tableGrid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		startFromField = new JTextField();
		startFromField.setToolTipText("IMPORTANT: This currently does nothing. The table varies itself in size so this feature (intended for fixed-size tables) is redundant. It's currently considered for removal. \r\n\r\nIndicates from which point the scanner should start from. (Default: 0)\r\n\r\nThis part is important for viewing tables with more than 100 entries.\r\nIf your table has more than 100 items, the 101st item onwards will not be displayed by default.\r\n\r\nThis value will revert back to 0 if the entered value is below 0, not a number, or if the value is too high.");
		startFromField.setText("0");
		startFromField.setBounds(433, 6, 86, 20);
		databasePanel.add(startFromField);
		startFromField.setColumns(10);
		
		JLabel startFromLabel = new JLabel("Start from");
		startFromLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		startFromLabel.setBounds(366, 6, 57, 20);
		databasePanel.add(startFromLabel);
		
		entryCounterLabel = new JLabel("0 entries found.");
		entryCounterLabel.setBounds(10, 448, 679, 14);
		databasePanel.add(entryCounterLabel);
		
		JLabel errorMessageLabel = new JLabel("");
		errorMessageLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		errorMessageLabel.setBounds(120, 448, 569, 14);
		databasePanel.add(errorMessageLabel);
		
		JScrollPane helpPanel = new JScrollPane();
		helpPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Help", null, helpPanel, "Includes shortened documentation and a list of functions.");
	}
	
	
	
	public static int filterChannelName(String name) {
		return 0;
	}
	
	
	public static JTable getTableGrid() {
		return tableGrid;
	}
	public static JButton getStartBotToggle() {
		return startBotToggle;
	}
	public static JTextArea getConsoleOutput() {
		return consoleOutput;
	}
	public static JPasswordField getBotTokenField() {
		return botTokenField;
	}
	public static Choice getTextChannelsList() {
		return textChannelsList;
	}
	public static JButton getSendButton() {
		return sendButton;
	}
	public static JTextField getConsoleInput() {
		return consoleInput;
	}
	public Choice getTableList() {
		return tableList;
	}
	public static JLabel getEntryCounterLabel() {
		return entryCounterLabel;
	}
}

