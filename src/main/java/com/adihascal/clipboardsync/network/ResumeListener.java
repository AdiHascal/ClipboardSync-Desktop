package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.handler.TaskHandler;

import java.io.IOException;

import static com.adihascal.clipboardsync.network.SocketHolder.in;

public class ResumeListener implements Runnable
{
	@Override
	public void run()
	{
		try
		{
			SocketHolder.init();
			in().readUTF();
			synchronized(TaskHandler.INSTANCE.get())
			{
				TaskHandler.INSTANCE.get().notify();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
