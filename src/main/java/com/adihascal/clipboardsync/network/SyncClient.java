package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SyncClient extends Thread
{
	static volatile String phoneAddress;
	private final Transferable object;
	private final String command;
	
	public SyncClient(String comm, Transferable trans)
	{
		this.object = trans;
		this.command = comm;
	}
	
	@Override
	public void run()
	{
		if(phoneAddress != null)
		{
			Socket s = null;
			try
			{
				s = new Socket(phoneAddress, Main.getPort());
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				switch(this.command)
				{
					case "send":
						out.writeUTF("receive");
						DataFlavor flavor = ClipHandlerRegistry
								.getSuitableFlavor(this.object.getTransferDataFlavors());
						if(flavor != null)
						{
							ClipHandlerRegistry.getHandlerFor(flavor.getMimeType()).sendClip(out, this.object);
							System.out.println("data sent");
						}
						break;
					case "disconnect":
						out.writeUTF(this.command);
						System.out.println("disconnected from " + phoneAddress);
						phoneAddress = null;
						break;
				}
			}
			catch(IOException | UnsupportedFlavorException e)
			{
				System.out.println("unable to connect");
				e.printStackTrace();
			}
			finally
			{
				try
				{
					assert s != null;
					s.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
