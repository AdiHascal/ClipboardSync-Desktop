package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketHolder
{
	private volatile static Socket socket;
	private volatile static DataInputStream socketIn;
	private volatile static DataOutputStream socketOut;
	private volatile static ServerSocket serverSocket;
	
	static
	{
		try
		{
			serverSocket = new ServerSocket(Main.getPort());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	static void init() throws IOException
	{
		socket = serverSocket.accept();
		socketIn = new DataInputStream(socket.getInputStream());
		socketOut = new DataOutputStream(socket.getOutputStream());
	}
	
	public static DataInputStream in()
	{
		return socketIn;
	}
	
	public static Socket getSocket()
	{
		return socket;
	}
	
	public static DataOutputStream out()
	{
		return socketOut;
	}
	
	public static void invalidate() throws IOException
	{
		if(socket != null)
		{
			socket.close();
		}
	}
}
