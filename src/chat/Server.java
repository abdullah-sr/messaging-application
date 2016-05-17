package chat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * Server does the networking
 * creates a socket to send data
 * broadcasts the data from the clients
 * sends the chat messages 
 * We based this code heavily off of the client and server classes given in lab 1 in CS445
 * Broadcast and ClientThread are both based off of the following code examples we found in research
 * This can also be seen in our references on the paper
 * http://www.java-forums.org/advanced-java/34258-udp-broadcast-receiving.html
 * http://forums.codeguru.com/showthread.php?515687-Broadcast-in-java
 */
public class Server 
{
	//unique ID each connection made
	private static int uId;
	//ArrayList to keep list of the clients
	private ArrayList<ClientThread> clientArray;
	private ServerGUI serverGUI;
	//display time
	private SimpleDateFormat simpleDate;
	//port num to listen for connection
	private int port;
	// the boolean to stop the server
	private boolean serverState;

	public Server(int initPort, ServerGUI initServerGUI)
	{
		port = initPort;
		serverGUI = initServerGUI;

		//display time (HH:MM:SS)
		simpleDate = new SimpleDateFormat("HH:mm:ss");
		clientArray = new ArrayList<ClientThread>();

	}//ctor

	protected void stop()
	{
		serverState = false;

		try
		{
			new Socket("localhost", port);
		}
		catch(Exception e){}
	}

	/*
	 * broadcast to all clients
	 */
	private synchronized void broadcast(String message)
	{
		//time stamp message
		String time = simpleDate.format(new Date());
		String message2 = time + "\n" + message + "\n";

		serverGUI.appendRoom(message2);

		for(int i = clientArray.size(); --i >= 0;) 
		{
			ClientThread ct = clientArray.get(i);
			//write to Client
			//if it fails remove from the list
			if(!ct.writeMsg(message2))
			{
				clientArray.remove(i);
			}
		}
	}

	/*
	 * for loggout
	 */
	synchronized void remove(int id)
	{
		//find id
		for(int i = 0; i < clientArray.size(); i++)
		{
			ClientThread thread = clientArray.get(i);
			if(thread.id == id)
			{
				clientArray.remove(i);
				return;
			}
		}
	}

	/*
	 * Start all threads
	 * create socket
	 * 
	 */
	public void start()
	{
		serverState = true;
		//create socket
		//wait for request
		ServerSocket serverSocket = null;
		try 
		{
			serverSocket = new ServerSocket(port);
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			//wait for connection
			while(serverState == true)
			{
				//send wait message
				Socket socket = serverSocket.accept();

				if(!serverState) break;

				ClientThread clientThread = new ClientThread(socket);
				clientArray.add(clientThread);
				clientThread.start();
			}

			//to stop
			try 
			{
				serverSocket.close();
				for(int i = 0; i < clientArray.size(); i++) 
				{
					ClientThread tc = clientArray.get(i);
					try
					{
						tc.inputStream.close();
						tc.outputStream.close();
						tc.socket.close();
					}
					catch(IOException ioE) {}
				}
			}
			catch(Exception e) {}
		}
		// something went wrong
		catch (IOException e)
		{
			
		}
	}

	
	/*
	 * Create the threads for each client connected
	 */
	class ClientThread extends Thread
	{	
		//client username
		String username;
		//messages
		Chat chatMessage;
		//time connected
		String date;
		//create socket
		//user id
		int id;
		
		Socket socket;
		
		ObjectInputStream inputStream;
		ObjectOutputStream outputStream;
		
		
		ClientThread(Socket initSocket)
		{
			id = uId + 1;
			socket = initSocket;

			//create datastream
			try
			{
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());

				//get username
				try 
				{
					username = (String) inputStream.readObject();
				} 
				catch (ClassNotFoundException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch(IOException e)
			{
				return;
			}
			date = new Date().toString() + "\n";
		}

		/*
		 * close
		 */
		private void close()
		{
			//try to close connection
			try 
			{
				if(outputStream != null) outputStream.close();
			}
			catch(Exception e) {}
			try 
			{
				if(inputStream != null) inputStream.close();
			}
			catch(Exception e) {};
			try 
			{
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * write message to stream
		 */
		private boolean writeMsg(String msg)
		{
			//send messge if Client is still connected
			if(!(socket.isConnected()))
			{
				close();
				return false;
			}
			//write message
			try
			{
				outputStream.writeObject(msg);
			}
			catch(IOException e)
			{}
			return true;
		}

		//run
		public void run()
		{
			boolean loop = true;
			while(loop)
			{
				//read string obj
				try
				{
					try
					{
						chatMessage = (Chat) inputStream.readObject();
					} 
					catch (ClassNotFoundException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				catch(IOException e)
				{
					break;
				}
		
				String message = chatMessage.getMessage();

				switch(chatMessage.getType())
				{
				case Chat.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case Chat.LOGOUT:
					loop = false;
					break;

				}
			}
			//remove me
			remove(id);
			close();
		}
	}

	public void main(String[] args)
	{
		//use port num 1500
		int portNum = 1500;
		//ensure correct args
		switch(args.length)
		{
		case 0: 
			break;
		case 1:
			try
			{
				portNum = Integer.parseInt(args[0]);
			}
			catch(Exception e)
			{
				System.out.println("Invalid port number");
				return;
			}
		default:
			System.out.println("Incorrect args");
			return;
		}

		//start server obj
		Server server = new Server(portNum, serverGUI);
		server.start();	
	}

}
