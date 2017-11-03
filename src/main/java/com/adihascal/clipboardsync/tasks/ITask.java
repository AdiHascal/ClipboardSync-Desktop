package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.handler.TaskHandler;

public interface ITask extends Runnable
{
	void run();
	
	default void finish()
	{
		TaskHandler.INSTANCE.pop();
	}
}
