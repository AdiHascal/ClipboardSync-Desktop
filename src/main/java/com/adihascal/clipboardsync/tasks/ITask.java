package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.GuiHandler.ProgramState;
import com.adihascal.clipboardsync.handler.TaskHandler;
import com.adihascal.clipboardsync.network.SocketHolder;

import java.net.SocketException;

public interface ITask extends Runnable
{
	default void finish()
	{
		try
		{
			SocketHolder.getSocket().setSoTimeout(0);
			TaskHandler.INSTANCE.pop();
			Main.getGuiHandler().setStatus(ProgramState.IDLE);
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
	}
}
