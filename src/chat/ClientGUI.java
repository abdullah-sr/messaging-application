package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * ClientGUI creates the user interface that the users see
 * takes a host and port (to connect)
 * default vals are set
 * gives the users options such as login and logout
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:"
	private JLabel label;
	// to hold the Username
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer;
	private JTextField tfPort;
	// to Logout
	private JButton login;
	private JButton logout;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;

	//socket number
	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;

		// The NorthPanel
		// The NorthPanel with the server name and the port number
		JPanel northPanel = new JPanel(new GridLayout(2,1));
		JPanel serverGUI = new JPanel(new GridLayout(1,2, 20, 1));
		tfServer = new JTextField(host);
		serverGUI.add(new JLabel("Server Address:  "));
		serverGUI.add(tfServer);
		northPanel.add(serverGUI);
		JPanel portGUI = new JPanel(new GridLayout(1,2, 20, 1));
		tfPort = new JTextField("" + port);
		portGUI.add(new JLabel("Port Number:  "));
		portGUI.add(tfPort);
		northPanel.add(portGUI);
		add(northPanel, BorderLayout.NORTH);

		JPanel southPanel = new JPanel(new GridLayout(2, 0));
		// Label and the TextField
		JPanel cols = new JPanel(new GridLayout(1, 0, 0, 0));
		label = new JLabel("Username:");
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		cols.add(label);
		cols.add(tf);
		southPanel.add(cols);
		add(southPanel, BorderLayout.SOUTH);
		add(northPanel, BorderLayout.NORTH);

		// center panel (chat room)
		ta = new JTextArea(50, 50);
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		//login first. logout second
		logout.setEnabled(false); 

		JPanel westPanel = new JPanel(new GridLayout(3, 1, 0, 10));
		westPanel.add(login);
		westPanel.add(logout);
		add(westPanel, BorderLayout.WEST);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}
	
	// Append text when message is sent
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

//connection failed
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		label.setText("Username");
		tf.setText("Anonymous");
		// reset port number
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}

	/*
	 * Button or JTextField clicked
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if Logout button
		if (o == logout) {
			client.sendMessage(new Chat(Chat.LOGOUT, ""));
			return;
		}

		// ok it is coming from the JTextField
		if (connected) {
			// just have to send the message
			client.sendMessage(new Chat(Chat.MESSAGE, tf.getText()));
			tf.setText("");
			return;
		}

		if (o == login) {
			//connection request
			String username = tf.getText().trim();
			// empty username 
			if (username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if (server.length() == 0)
				return;
			// empty or invalid port num
			String portNumber = tfPort.getText().trim();
			if (portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception en) {
				return; 
			}

			// create new Client with GUI
			client = new Client(server, port, username, this);
			//start the Client
			if (!client.start())
				return;
			tf.setText("");
			label.setText("Enter your message here");
			connected = true;

			// disable login button
			login.setEnabled(false);
			// enable the buttons
			logout.setEnabled(true);
			// disable the Server and Port
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener 
			tf.addActionListener(this);
		}

	}

	// to start
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

}
