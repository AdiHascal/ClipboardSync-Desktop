package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;
import com.adihascal.clipboardsync.handler.IClipHandler;
import com.adihascal.clipboardsync.handler.IntentHandler;

import java.awt.datatransfer.Transferable;
import java.io.IOException;

import static com.adihascal.clipboardsync.network.SocketHolder.out;

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
			try
			{
				switch(this.command)
				{
					case "disconnect":
						out().writeUTF(this.command);
						System.out.println("disconnected from " + phoneAddress);
						phoneAddress = null;
						break;
					case "announce":
						Main.getServer().setTransferable(this.object);
						out().writeUTF("announce");
						IClipHandler handler = ClipHandlerRegistry.getHandlerFor(ClipHandlerRegistry
								.getSuitableFlavor(this.object.getTransferDataFlavors()).getMimeType());
						out().writeBoolean(handler instanceof IntentHandler);
						System.out.println("remote aware of pending local data");
						break;
				}
			}
			catch(IOException e)
			{
				System.out.println("unable to connect");
				e.printStackTrace();
			}
		}
	}
}
