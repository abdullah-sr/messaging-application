package chat;

import java.io.*;

/*
 * The chat object is the message that the clients send 
 * to each other through the server
 * Takes a type(of message) and the message string to be sent
 */
public class Chat implements Serializable
{
	protected static final long serialVersionUID = 1101223674;
	
	//types of messages
	//MESSAGE format
	static final int MESSAGE = 1;
	//LOGOUT to disconnect from server
	static final int LOGOUT = 2;
	
	private int type;
	private String message;
	
	Chat(int initType, String initMessage)
	{
		type = initType;
		message = initMessage;
	}//ctor
	
	public int getType()
	{
		return type;
	}
	
	public String getMessage()
	{
		return message;
	}
	
}
