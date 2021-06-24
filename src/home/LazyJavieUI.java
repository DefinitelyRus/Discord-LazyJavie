package home;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import javax.security.auth.login.LoginException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
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
import commands.P;
import commands.Quit;
import javax.swing.JToggleButton;

/*
 * itemnamewith32chars_________00000
itemnamewith32chars_________00001
itemnamewith32chars_________00002
itemnamewith32chars_________00003
itemnamewith32chars_________00004
itemnamewith32chars_________00005
itemnamewith32chars_________00006
itemnamewith32chars_________00007
itemnamewith32chars_________00008
 */

@SuppressWarnings("serial")
public class LazyJavieUI extends JFrame {

	private JPanel contentPane;
	private JTextField consoleInput;
	private JPasswordField botTokenField;
	private static JTable tableGrid;
	private JTextField startFromField;
	private final Action action = new SwingAction();
	public static JTextArea consoleOutput;
	public static JButton startBotToggle;
	
	//Developer-written instantiations
	static boolean hideUI = false;
	
	public static void main(String[] args) throws LoginException, SQLException{
		
		//Developer code intended to run before the instantiation of the UI.
		//Temporarily disabled; database not yet created.
		//String hideUI_asString = SQLconnector.get("select * from botSettings where id = 'hide_ui_on_close'", "value", true);
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
		//dbTable.getTableGridContents(10, 8);
		
		
	}
	
	public static int willHideOnClose(boolean toHide) {
		if (toHide == true) {return JFrame.HIDE_ON_CLOSE;}
		else {return JFrame.EXIT_ON_CLOSE;}
	}

	public LazyJavieUI() {
		//setLookAndFeel();
		setDefaultCloseOperation(willHideOnClose(hideUI));
		setTitle("LazyJavie Host Control Panel");
		setResizable(false);
		setBackground(SystemColor.text);
		setBounds(100, 100, 720, 540);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.menu);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			P.print(e1.toString());
			e1.printStackTrace();
		}
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(SystemColor.menu);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
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
		
		consoleInput = new JTextField();
		consoleInput.setBounds(18, 320, 662, 20);
		consoleInput.setColumns(82);
		consolePanel.add(consoleInput);
		
		Choice textChannelsList = new Choice();
		textChannelsList.setBounds(18, 347, 373, 20);
		textChannelsList.setForeground(SystemColor.textText);
		textChannelsList.setBackground(SystemColor.text);
		consolePanel.add(textChannelsList);
		
		JButton sendButton = new JButton("Send");
		sendButton.setBounds(397, 344, 283, 23);
		consolePanel.add(sendButton);
		
		startBotToggle = new JButton("Start Bot");
		startBotToggle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (startBotToggle.getText().equals("Start Bot")) {
					startBotToggle.setText("Stop");
					Bot.start();
				} else if (startBotToggle.getText().equals("Stop")) {
					startBotToggle.setText("Start Bot");
					Quit.softExit();
				}
			}
		});
		startBotToggle.setBounds(600, 439, 89, 23);
		consolePanel.add(startBotToggle);
		
		JTabbedPane botSettingsPanel = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.addTab("Bot Settings", null, botSettingsPanel, "All modifiable bot settings are included in here.");
		
		JPanel authenticationPanel = new JPanel();
		botSettingsPanel.addTab("Authentication", null, authenticationPanel, "All required values needed to give you access to Console and other bot settings.");
		authenticationPanel.setLayout(null);
		
		JLabel botTokenLabel = new JLabel("Bot Token");
		botTokenLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		botTokenLabel.setBounds(10, 11, 80, 14);
		authenticationPanel.add(botTokenLabel);
		
		botTokenField = new JPasswordField();
		botTokenField.setToolTipText("If you want to use your own bot, put your bot's token here. Otherwise, leave it empty.");
		botTokenField.setColumns(20);
		botTokenField.setBounds(100, 8, 470, 20);
		authenticationPanel.add(botTokenField);
		
		JLabel lblNewLabel = new JLabel("If you want to use your own bot, put your bot's token here. Otherwise, leave it empty.");
		lblNewLabel.setBounds(46, 36, 420, 14);
		authenticationPanel.add(lblNewLabel);
		
		JPanel databasePanel = new JPanel();
		tabbedPane.addTab("Database", null, databasePanel, "A place for all locally-stored tables.");
		databasePanel.setLayout(null);
		
		Choice tableList = new Choice();
		tableList.setBounds(10, 6, 350, 20);
		databasePanel.add(tableList);
		
		JButton updateTableButton = new JButton("View");
		updateTableButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//TODO Update table headers upon clicking update button.
			}
		});
		updateTableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
			new Object[][] {
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
			},
			new String[] {
				"New column", "New column", "New column", "New column", "New column", "New column", "New column", "New column"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tableGrid.setCellSelectionEnabled(true);
		tableGrid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		startFromField = new JTextField();
		startFromField.setToolTipText("Indicates from which point the scanner should start from. (Default: 0)\r\n\r\nThis part is important for viewing tables with more than 100 entries.\r\nIf your table has more than 100 items, the 101st item onwards will not be displayed by default.\r\n\r\nThis value will revert back to 0 if the entered value is below 0, not a number, or if the value is too high.");
		startFromField.setText("0");
		startFromField.setBounds(433, 6, 86, 20);
		databasePanel.add(startFromField);
		startFromField.setColumns(10);
		
		JLabel startFromLabel = new JLabel("Start from");
		startFromLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		startFromLabel.setBounds(366, 6, 57, 20);
		databasePanel.add(startFromLabel);
		
		JLabel entryCounterLabel = new JLabel("0 entries found.");
		entryCounterLabel.setBounds(10, 448, 100, 14);
		databasePanel.add(entryCounterLabel);
		
		JLabel errorMessageLabel = new JLabel("");
		errorMessageLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		errorMessageLabel.setBounds(120, 448, 569, 14);
		databasePanel.add(errorMessageLabel);
		
		JScrollPane helpPanel = new JScrollPane();
		helpPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Help", null, helpPanel, "Includes shortened documentation and a list of functions.");
	}
	public static JTable getTableGrid() {
		return tableGrid;
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	public static JButton getStartBotToggle() {
		return startBotToggle;
	}
	public static JTextArea getConsoleOutput() {
		return consoleOutput;
	}
}
