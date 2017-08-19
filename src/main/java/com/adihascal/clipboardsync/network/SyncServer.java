package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;
import com.adihascal.clipboardsync.handler.IClipHandler;
import com.adihascal.clipboardsync.handler.TaskHandler;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import static com.adihascal.clipboardsync.network.SocketHolder.in;
import static com.adihascal.clipboardsync.network.SocketHolder.out;

public class SyncServer extends Thread
{
	private Transferable temp;
	
	@Override
	public void run()
	{
		try
		{
			SocketHolder.init();
			
			while(true)
			{
				System.out.println("waiting for input");
				String command = in().readUTF();
				switch(command)
				{
					case "receive":
						Main.isBusy = true;
						IClipHandler handler =
								ClipHandlerRegistry.getHandlerFor(in().readUTF());
						handler.receiveClip(Toolkit.getDefaultToolkit().getSystemClipboard());
						System.out.println("data received");
						Main.isBusy = false;
						break;
					case "connect":
						SyncClient.phoneAddress = in().readUTF();
						System.out.println(SyncClient.phoneAddress + " connected");
						break;
					case "disconnect":
						System.out.println(SyncClient.phoneAddress + " disconnected");
						SyncClient.phoneAddress = null;
						SocketHolder.invalidate();
						Main.restart();
						return;
					case "accept":
						DataFlavor flavor = ClipHandlerRegistry.getSuitableFlavor(this.temp.getTransferDataFlavors());
						if(flavor != null)
						{
							Main.isBusy = true;
							out().writeUTF("receive");
							ClipHandlerRegistry.getHandlerFor(flavor.getMimeType())
									.sendClip(this.temp);
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
					case "resume_transfer":
						TaskHandler.resume();
						break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("IO Error. Restarting server.");
			Main.restart();
		}
	}
	
	@Override
	public void interrupt()
	{
		try
		{
			SocketHolder.invalidate();
		}
		catch(IOException e)
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
