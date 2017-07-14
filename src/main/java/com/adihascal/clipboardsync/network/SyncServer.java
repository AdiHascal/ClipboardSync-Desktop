package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncServer extends Thread
{
	private ServerSocket serverSocket;
	
	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(Main.getPort());
			while(true)
			{
				System.out.println("starting server");
				Socket s = serverSocket.accept();
				DataInputStream socketIn = new DataInputStream(s.getInputStream());
				String command = socketIn.readUTF();
				switch(command)
				{
					case "receive":
						ClipHandlerRegistry.getHandlerFor(socketIn.readUTF())
								.receiveClip(socketIn, Toolkit.getDefaultToolkit().getSystemClipboard());
						System.out.println("data received");
						break;
					case "connect":
						SyncClient.phoneAddress = socketIn.readUTF();
						System.out.println(SyncClient.phoneAddress + " connected");
						break;
					case "disconnect":
						System.out.println(SyncClient.phoneAddress + " disconnected");
						SyncClient.phoneAddress = null;
						break;
				}
				s.close();
			}
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void interrupt()
	{
		try
		{
			serverSocket.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		super.interrupt();
	}
}
