package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.ServerSocket;

import static com.adihascal.clipboardsync.network.SocketHolder.*;

public class SyncServer extends Thread
{
	private ServerSocket serverSocket;
	private Transferable temp;
	
	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(Main.getPort());
			setSocket(serverSocket.accept());
			while(true)
			{
				System.out.println("starting server");
				String command = getInputStream().readUTF();
				switch(command)
				{
					case "receive":
						Main.isBusy = true;
						ClipHandlerRegistry.getHandlerFor(getInputStream().readUTF())
								.receiveClip(getInputStream(), Toolkit.getDefaultToolkit().getSystemClipboard());
						System.out.println("data received");
						Main.isBusy = false;
						break;
					case "connect":
						SyncClient.phoneAddress = getInputStream().readUTF();
						System.out.println(SyncClient.phoneAddress + " connected");
						break;
					case "disconnect":
						System.out.println(SyncClient.phoneAddress + " disconnected");
						SyncClient.phoneAddress = null;
						break;
					case "accept":
						DataFlavor flavor = ClipHandlerRegistry.getSuitableFlavor(this.temp.getTransferDataFlavors());
						if(flavor != null)
						{
							Main.isBusy = true;
							getOutputStream().writeUTF("receive");
							ClipHandlerRegistry.getHandlerFor(flavor.getMimeType())
									.sendClip(getOutputStream(), this.temp);
							System.out.println("data sent");
							Main.isBusy = false;
						}
						break;
					case "refuse":
						System.out.println("remote refused local data");
						break;
					case "pause":
						System.out.println("paused");
						Main.isBusy = true;
						break;
					case "resume":
						System.out.println("resumed");
						Main.isBusy = false;
						break;
				}
			}
		}
		catch(IOException | UnsupportedFlavorException e)
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
			terminate();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		super.interrupt();
	}
	
	void setTransferable(Transferable temp)
	{
		this.temp = temp;
	}
}
