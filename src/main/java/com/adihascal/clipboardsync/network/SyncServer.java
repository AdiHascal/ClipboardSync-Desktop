package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncServer extends Thread
{
	public volatile boolean shouldRun = true;
	private ServerSocket serverSocket;
	
	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(Main.getPort());
			while(this.shouldRun)
			{
				System.out.println("starting server");
				Socket s = serverSocket.accept();
				DataInputStream socketIn = new DataInputStream(s.getInputStream());
				ByteArrayOutputStream baos = new ByteArrayOutputStream(104857600);
				int count;
				byte[] buffer = new byte[s.getReceiveBufferSize()];
				while((count = socketIn.read(buffer)) > 0)
				{
					baos.write(buffer, 0, count);
				}
				DataInputStream is = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
				ClipHandlerRegistry.getHandlerFor(is.readUTF()).receiveClip(is, Toolkit.getDefaultToolkit().getSystemClipboard());
				System.out.println("data received");
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
