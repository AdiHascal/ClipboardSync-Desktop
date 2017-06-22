package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.Socket;

public class SyncClient extends Thread
{
	public static String phoneAddress;
	private final Transferable object;
	
	public SyncClient(Transferable t)
	{
		this.object = t;
	}
	
	@Override
	public void run()
	{
		Socket s = null;
		try
		{
			DataFlavor flavor = ClipHandlerRegistry.getFlavorFrom(this.object.getTransferDataFlavors());
			if(flavor != null)
			{
				s = new Socket(phoneAddress, Main.getPort());
				ClipHandlerRegistry.getHandlerFor(flavor.getMimeType()).sendClip(s, this.object);
				System.out.println("data sent");
			}
		} catch(IOException | UnsupportedFlavorException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if(s != null)
				{
					s.close();
				}
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
