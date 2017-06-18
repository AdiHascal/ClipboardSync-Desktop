package com.adihascal.clipboardsync;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SyncClient extends Thread
{
	private final Transferable object;
	private final RLECompressor compressor = new RLECompressor();
	
	SyncClient(Transferable t)
	{
		this.object = t;
	}
	
	@Override
	public void run()
	{
		Socket s = null;
		try
		{
			if(this.object.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				s = new Socket("10.0.0.36", Main.getPort());
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				String toSend = (String) this.object.getTransferData(DataFlavor.stringFlavor);
				out.writeUTF(toSend);
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
