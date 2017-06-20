package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.Socket;

public class SyncClient extends Thread
{
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
			if(ClipHandlerRegistry.isMimeTypeSupported(this.object.getTransferDataFlavors()[0].getMimeType()))
			{
				s = new Socket("10.0.0.36", Main.getPort());
				if(ClipHandlerRegistry.isMimeTypeSupported(this.object.getTransferDataFlavors()[0].getMimeType()))
				{
					ClipHandlerRegistry.getHandlerFor(this.object.getTransferDataFlavors()[0].getMimeType()).sendClip(s, this.object);
				}
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
