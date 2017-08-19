package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.handler.TaskHandler;

public interface ITask
{
	void execute();
	
	default void finish()
	{
		TaskHandler.pop();
	}
}
