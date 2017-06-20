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
	public volatile boolean shouldRun = true;
	
	@Override
	public void run()
	{
		while(this.shouldRun)
		{
			ServerSocket serverSocket = null;
			Socket s = null;
			try
			{
				serverSocket = new ServerSocket(Main.getPort());
				System.out.println("starting server");
				s = serverSocket.accept();
				DataInputStream is = new DataInputStream(s.getInputStream());
				String mt = is.readUTF();
				if(ClipHandlerRegistry.isMimeTypeSupported(mt))
				{
					ClipHandlerRegistry.getHandlerFor(mt).receiveClip(is, Toolkit.getDefaultToolkit().getSystemClipboard());
				}
				System.out.println("data received");
			} catch(IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					if(serverSocket != null && s != null)
					{
						serverSocket.close();
						s.close();
					}
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
