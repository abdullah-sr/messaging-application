package chat;

import java.net.*;
import java.io.*;
import java.util.*;


/*
 * Client class creates the client object
 * there can be many clients on at the same time
 * Takes a Server to connect to, a port to connect to, a username, and a GUI
 * The GUI will always be our GUI class
 * all args have default vals
 *
 * we heavily based our code off of the server and client classes given in the first lab of CS445
 */

public class Client
{
	//read from socket
	private ObjectInputStream inputStream;
	//write to socket
	private ObjectOutputStream outputStream;		
	private Socket socket;
	
	private static ClientGUI clientGUI;
	
	//server
	private String server;
	//username
	private String username;
	//port
	private int port;
	
	Client(String initServer, int initPort, String initUsername, ClientGUI initClientGUI)
	{
		server = initServer;
		username = initUsername;
		port = initPort;
		clientGUI = initClientGUI;
	}//ctor
	
	//for GUI
//	private void write(String msg)
//	{
//		clientGUI.append(msg + "\n");
//	}
	
	/*
	 * Send message to Server
	 */
	public void sendMessage(Chat msg)
	{
		try
		{
			outputStream.writeObject(msg);			
		}
		catch(IOException e)
		{
			clientGUI.append("Exception writing to server");
		}
	}
	
	/*
	 * if something goes wrong, close and disconnect
	 */
	private void disconnect()
	{
		try
		{
			if(outputStream != null) outputStream.close();
		}
		catch(Exception e) {} 
		try 
		{ 
			if(inputStream != null) inputStream.close();
		}
		catch(Exception e) {} 
			
        try
        {
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
		
		clientGUI.connectionFailed();
	}
	
	class ListenServer extends Thread {

		public void run() 
		{
			while(true) 
			{
				try 
				{
					String msg = (String) inputStream.readObject();
					clientGUI.append(msg);
				}
				catch(IOException e)
				{
					clientGUI.append("Connection has been closed");
					clientGUI.connectionFailed();
					break;
				} 
				catch (ClassNotFoundException e) {}
				
			}
		}
	}
	
	public boolean start()
	{
		//connect to server
		try
		{
			socket = new Socket(server, port);
		}
		catch(Exception e)
		{
			clientGUI.append("Error connecting to server");
			return false;
		}
		
		clientGUI.append("Connection successful to server: " + socket.getInetAddress() + " to port number: " + socket.getPort());
		
		//create data stream
		try
		{
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream= new ObjectOutputStream(socket.getOutputStream());
		}
		catch(IOException ioe)
		{
			clientGUI.append("Exception creating IO stream \n");
			return false;
		}
		
		//listen to server
		new ListenServer().start();
		
		//tell the server who we are
		try
		{
			outputStream.writeObject(username);
		}
		catch(IOException eio)
		{
			clientGUI.append("Exception logging in");
			disconnect();
			return false;
		}
		
		//return success
		return true;
	}
	
	public static void main(String[] args)
	{
		//default
		int portNum = 1500;
		String serverAdrs = "localhost";
		String userName = "Anonymous";
		
		//create client
		Client client = new Client(serverAdrs, portNum, userName, clientGUI);
		
		//correct number of args?
		switch(args.length)
		{
		case 0:
			break;
		case 1:
			userName = args[0];
		case 2:
			try
			{
				portNum = Integer.parseInt(args[1]);
			}
			catch(Exception e)
			{
				System.out.println("Invalid port number");
				return;
			}
		case 3:
			serverAdrs = args[2];
			
		default:
			System.out.println("Invalid number of arguments");
			return;
		}
		
		
		
		//test connection to server
		if(!(client.start())) return;
		
		//wait for message
		Scanner scan = new Scanner(System.in);

		while(true)
		{
			//read message
			String msg = scan.nextLine();
			
			//logout
			if(msg.equalsIgnoreCase("LOGOUT"))
			{
				client.sendMessage(new Chat(Chat.LOGOUT, ""));
				//disconnect
				break;
			}
			//user message
			else client.sendMessage(new Chat(Chat.MESSAGE, msg));
		}
		
		//disconnect
		client.disconnect();		
		
	}	
	
}
