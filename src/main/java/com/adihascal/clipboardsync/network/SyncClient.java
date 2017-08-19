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
	private final Object args;
	private final String command;
	
	public SyncClient(String comm, Object args)
	{
		this.args = args;
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
						Main.getServer().setTransferable((Transferable) this.args);
						out().writeUTF("announce");
						IClipHandler handler = ClipHandlerRegistry.getHandlerFor(ClipHandlerRegistry
								.getSuitableFlavor(((Transferable) this.args).getTransferDataFlavors()).getMimeType());
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
