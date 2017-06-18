package com.adihascal.clipboardsync;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class SyncServer extends Thread
{
	volatile boolean shouldRun = true;
	
	private static void decompress(byte[] compress, byte[] out)
	{
		int opos = 0;
		int i = 0;
		
		while(i < compress.length)
		{
			if(opos >= out.length)
			{
				return;
			}
			
			int cmd = compress[i++] & 255;
			if((cmd & 128) == 0)
			{
				opos += cmd & 127;
			} else
			{
				int ln;
				int j;
				if(cmd == 255)
				{
					if(i + 2 > compress.length)
					{
						return;
					}
					
					ln = Math.min(compress[i] & 255, out.length - opos);
					
					for(j = 0; j < ln; ++j)
					{
						out[opos + j] = compress[i + 1];
					}
					
					opos += ln;
					i += 2;
				} else
				{
					ln = Math.min(Math.min(cmd & 127, out.length - opos), compress.length - i);
					
					for(j = 0; j < ln; ++j)
					{
						out[opos + j] = compress[i + j];
					}
					
					opos += ln;
					i += ln;
				}
			}
		}
	}
	
	@Override
	public void run()
	{
		while(this.shouldRun)
		{
			ServerSocket serverSocket = null;
			try
			{
				serverSocket = new ServerSocket(Main.getPort());
				System.out.println("starting server");
				DataInputStream is = new DataInputStream(serverSocket.accept().getInputStream());
				System.out.println("data received");
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(is.readUTF()), Main.INSTANCE);
			} catch(IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					if(serverSocket != null)
					{
						serverSocket.close();
					}
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
