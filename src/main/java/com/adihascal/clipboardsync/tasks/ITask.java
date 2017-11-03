package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.handler.TaskHandler;

public interface ITask extends Runnable
{
	default void finish()
	{
		TaskHandler.INSTANCE.pop();
	}
}
