package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.network.ResumeListener;
import com.adihascal.clipboardsync.network.SocketHolder;
import com.adihascal.clipboardsync.tasks.ITask;

import java.io.IOException;

public class TaskHandler
{
	public static final TaskHandler INSTANCE = new TaskHandler();
	private Thread current;
	
	void setAndRun(ITask task) throws Exception
	{
		if(current == null)
		{
			set(task);
			run();
		}
	}
	
	private void set(ITask task) throws Exception
	{
		if(current == null)
		{
			current = new Thread(task);
		}
		else
		{
			throw new Exception("cannot set task while another one is already running");
		}
	}
	
	private void run()
	{
		current.start();
	}
	
	public Thread get()
	{
		return current;
	}
	
	public void pop()
	{
		current = null;
		Main.isBusy = false;
	}
	
	public void pause() throws InterruptedException, IOException
	{
		synchronized(current)
		{
			SocketHolder.invalidate();
			new Thread(new ResumeListener()).start();
			current.wait();
		}
	}
}
